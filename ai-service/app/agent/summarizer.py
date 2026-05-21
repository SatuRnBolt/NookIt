"""Rolling conversation summarization.

When a conversation grows past ``SUMMARIZE_THRESHOLD_MESSAGES``, older messages
get folded into a single summary stored in ``ai_conversations.context_json``.
The agent loop then loads ``summary + recent N`` instead of the full history,
keeping prompt size bounded for arbitrarily long chats.

The trigger runs at most once per turn and only when at least
``SUMMARIZE_MIN_NEW_MESSAGES`` new old-messages have accrued since the previous
summary, so we don't burn an LLM call for every two messages.
"""

from __future__ import annotations

import logging
from datetime import datetime

from openai import AsyncOpenAI
from sqlalchemy.ext.asyncio import AsyncSession

from app.agent import repo
from app.config import get_settings
from app.models import AiConversation, AiMessage

logger = logging.getLogger(__name__)


async def maybe_summarize(
    session: AsyncSession, conv: AiConversation, llm_client: AsyncOpenAI
) -> None:
    """Fold older messages into ``conv.context_json['summary']`` when warranted."""
    settings = get_settings()
    total = await repo.count_messages(session, conv.id)
    if total < settings.summarize_threshold_messages:
        return

    cutoff_id = await repo.get_summary_cutoff_id(session, conv.id, settings.summarize_keep_recent)
    if cutoff_id is None:
        return

    ctx = dict(conv.context_json or {})
    summarized_through = int(ctx.get("summarized_through_id", 0) or 0)
    if cutoff_id - summarized_through < settings.summarize_min_new_messages:
        return

    msgs = await repo.load_messages_between(session, conv.id, summarized_through, cutoff_id)
    if not msgs:
        return

    prev_summary = ctx.get("summary", "") or ""
    try:
        new_summary = await _summarize_via_llm(prev_summary, msgs, llm_client, settings.llm_model)
    except Exception as e:
        logger.warning("summarization failed (will retry next turn): %s", e)
        return

    ctx["summary"] = new_summary
    ctx["summarized_through_id"] = int(cutoff_id)
    ctx["summarized_at"] = datetime.utcnow().isoformat()
    await repo.update_conversation_context(session, conv.id, ctx)
    await session.commit()
    logger.info(
        "summarized conv=%d through msg=%d (%d msgs folded)", conv.id, cutoff_id, len(msgs)
    )


_SUMMARIZE_USER_TEMPLATE = """请把以下对话片段压缩成 200 字以内的中文摘要。
要求：
- 保留学生的关键意图（查询哪间自习室、想要什么座位类型、想预约什么时段等）
- 保留 AI 已经查询/创建/取消的关键事实（具体的房间名、座位编号、预约编号、时段、违约记录等）
- 不要逐条复述消息；直接给一段陈述性文字
- 如果已有摘要，请把新片段合并进去后输出**一份新的滚动摘要**，不要分成两段

{prev_section}

新对话片段：
{messages_text}

直接输出新的滚动摘要文本，不要加 "摘要：" 之类前缀。"""


async def _summarize_via_llm(
    prev_summary: str, msgs: list[AiMessage], client: AsyncOpenAI, model: str
) -> str:
    lines: list[str] = []
    for m in msgs:
        text = (m.content_text or "").strip()
        if len(text) > 400:
            text = text[:400] + "…"
        # Tool messages are typically JSON payloads; render their role explicitly for the LLM.
        lines.append(f"[{m.message_role}] {text}")
    messages_text = "\n".join(lines)
    prev_section = f"已有滚动摘要：\n{prev_summary}" if prev_summary else "（这是第一次生成摘要）"
    prompt = _SUMMARIZE_USER_TEMPLATE.format(prev_section=prev_section, messages_text=messages_text)
    resp = await client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=600,
        temperature=0.3,
    )
    return (resp.choices[0].message.content or "").strip()
