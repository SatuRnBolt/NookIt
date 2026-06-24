"""Tool-use loop.

One ``run_turn`` call drives a conversation by one user message:

    user msg -> LLM -> tool_calls? -> execute -> feed back -> LLM -> ... -> text

A side-effect tool (create/cancel reservation) halts the loop and the call returns
a ``PendingAction`` instead of a text reply. The frontend renders a confirm card;
on user confirm, the chat route calls ``resume_from_action`` to execute the
pending tool and let the LLM produce the final text.

No LangChain, no agent framework. The function below should remain readable end
to end.
"""

from __future__ import annotations

import json
import logging
from dataclasses import dataclass
from typing import Any

from openai import AsyncOpenAI
from sqlalchemy.ext.asyncio import AsyncSession

from app.agent import repo
from app.agent.prompts import build_system_prompt
from app.agent.registry import to_openai_tool_schemas
from app.agent.summarizer import maybe_summarize
from app.agent.tools.base import Tool, ToolContext, summarize_tool_call
from app.config import get_settings
from app.http_client import JavaApiClient
from app.models import AiActionLog, AiConversation, AiMessage

logger = logging.getLogger(__name__)


class AgentLoopError(Exception):
    pass


@dataclass
class PendingAction:
    """Returned to the frontend when an LLM-chosen write tool needs user confirmation."""

    action_id: int
    tool_name: str
    summary: str
    params: dict[str, Any]


@dataclass
class ChatResult:
    """Either ``reply`` (final text from assistant) OR ``pending_action`` is set."""

    reply: str | None
    pending_action: PendingAction | None


_SKIPPED_BATCH_PAYLOAD = {
    "ok": False,
    "error": "skipped: a sibling tool call is awaiting user confirmation",
}
_USER_CANCELLED_PAYLOAD = {"ok": False, "error": "用户未确认此操作，已自动取消"}


# --- public entry points --------------------------------------------------


async def run_turn(
    *,
    session: AsyncSession,
    conv: AiConversation,
    user_message: str,
    tools: dict[str, Tool],
    java: JavaApiClient,
) -> ChatResult:
    """Advance the conversation by one user message."""
    await _clear_dangling_pending(session, conv.id)

    await repo.append_message(
        session, conversation_id=conv.id, role="user", content=user_message
    )
    await repo.set_conversation_title_if_missing(session, conv.id, user_message)
    await repo.touch_conversation_last_message_at(session, conv.id)
    await session.commit()

    result = await _drive_loop(session=session, conv=conv, tools=tools, java=java)
    await maybe_summarize(session, conv, _build_llm_client())
    return result


async def resume_from_action(
    *,
    session: AsyncSession,
    conv: AiConversation,
    action: AiActionLog,
    tools: dict[str, Tool],
    java: JavaApiClient,
) -> ChatResult:
    """User confirmed a previously-proposed write action. Execute it and continue."""
    if action.action_status != "pending":
        raise AgentLoopError(f"action {action.id} is not pending (status={action.action_status})")

    tool = tools.get(action.action_type)
    if tool is None:
        raise AgentLoopError(f"unknown tool referenced by action: {action.action_type}")

    ctx = ToolContext(user_id=conv.user_id, java=java)
    args = action.request_json or {}
    result_payload, action_status = await _safe_run_tool(tool, ctx, args)

    await repo.update_action_result(session, action, status=action_status, response=result_payload)
    await repo.append_message(
        session,
        conversation_id=conv.id,
        role="tool",
        content=json.dumps(result_payload, ensure_ascii=False),
        metadata={"tool_call_id": action.idempotency_key, "tool_name": tool.name},
    )
    await session.commit()

    result = await _drive_loop(session=session, conv=conv, tools=tools, java=java)
    await maybe_summarize(session, conv, _build_llm_client())
    return result


async def ignore_pending_action(
    *,
    session: AsyncSession,
    conv: AiConversation,
    action: AiActionLog,
) -> None:
    """Mark a pending action ignored and persist a synthetic tool result."""
    if action.action_status != "pending":
        raise AgentLoopError(f"action {action.id} is not pending (status={action.action_status})")

    await repo.update_action_result(
        session, action, status="ignored", response=_USER_CANCELLED_PAYLOAD
    )
    if action.idempotency_key:
        await _persist_tool_result(
            session,
            conv.id,
            action.idempotency_key,
            action.action_type,
            _USER_CANCELLED_PAYLOAD,
        )


# --- loop core ------------------------------------------------------------


async def _drive_loop(
    *, session: AsyncSession, conv: AiConversation, tools: dict[str, Tool], java: JavaApiClient
) -> ChatResult:
    settings = get_settings()
    client = _build_llm_client()
    tool_schemas = to_openai_tool_schemas(tools)
    ctx = ToolContext(user_id=conv.user_id, java=java)

    for step in range(settings.llm_max_tool_steps):
        summary, history = await _load_history_with_summary(session, conv)
        messages = _messages_to_openai_format(history, conv.user_id, summary)

        try:
            resp = await client.chat.completions.create(
                model=settings.llm_model,
                messages=messages,
                tools=tool_schemas,
                tool_choice="auto",
            )
        except Exception as e:
            logger.exception("LLM call failed at step %d", step)
            final = f"抱歉，AI 服务暂时不可用：{e}"
            await repo.append_message(
                session,
                conversation_id=conv.id,
                role="assistant",
                content=final,
                metadata={"error": str(e)},
            )
            await session.commit()
            return ChatResult(reply=final, pending_action=None)

        choice = resp.choices[0].message
        tool_calls = choice.tool_calls or []

        assistant_meta: dict[str, Any] = {}
        if tool_calls:
            assistant_meta["tool_calls"] = [
                {
                    "id": tc.id,
                    "type": "function",
                    "function": {"name": tc.function.name, "arguments": tc.function.arguments},
                }
                for tc in tool_calls
            ]
        assistant_msg = await repo.append_message(
            session,
            conversation_id=conv.id,
            role="assistant",
            content=choice.content or "",
            metadata=assistant_meta or None,
        )

        if not tool_calls:
            await session.commit()
            return ChatResult(reply=choice.content or "", pending_action=None)

        pending = await _process_tool_calls(
            session=session, conv=conv, assistant_msg_id=assistant_msg.id,
            tool_calls=tool_calls, tools=tools, ctx=ctx,
        )
        await session.commit()
        if pending is not None:
            return ChatResult(reply=None, pending_action=pending)
        # else: loop back to let the LLM react to the tool results

    final = "对话步数已达上限，请重新发起一次请求或换一种说法。"
    await repo.append_message(session, conversation_id=conv.id, role="assistant", content=final)
    await session.commit()
    return ChatResult(reply=final, pending_action=None)


async def _process_tool_calls(
    *,
    session: AsyncSession,
    conv: AiConversation,
    assistant_msg_id: int,
    tool_calls: list,
    tools: dict[str, Tool],
    ctx: ToolContext,
) -> PendingAction | None:
    """Execute read-only calls in order; halt at first side_effect; placeholder the rest."""
    pending: PendingAction | None = None

    for tc in tool_calls:
        if pending is not None:
            # Already halted — emit synthetic tool result so OpenAI history stays valid.
            await repo.append_message(
                session,
                conversation_id=conv.id,
                role="tool",
                content=json.dumps(_SKIPPED_BATCH_PAYLOAD, ensure_ascii=False),
                metadata={"tool_call_id": tc.id, "tool_name": tc.function.name},
            )
            continue

        name = tc.function.name
        try:
            args = json.loads(tc.function.arguments or "{}")
        except json.JSONDecodeError:
            args = {}

        tool = tools.get(name)
        if tool is None:
            payload = {"ok": False, "error": f"unknown tool: {name}"}
            await _persist_tool_result(session, conv.id, tc.id, name, payload)
            await repo.log_action(
                session, conversation_id=conv.id, message_id=assistant_msg_id,
                action_type=name, request=args, response=payload, status="failed",
                idempotency_key=tc.id,
            )
            continue

        if tool.side_effect:
            summary = await summarize_tool_call(tool, ctx, args)
            action = await repo.log_action(
                session, conversation_id=conv.id, message_id=assistant_msg_id,
                action_type=tool.name, request=args, response=None, status="pending",
                idempotency_key=tc.id,
            )
            pending = PendingAction(
                action_id=action.id, tool_name=tool.name, summary=summary, params=args
            )
            continue

        # read-only
        payload, status = await _safe_run_tool(tool, ctx, args)
        await _persist_tool_result(session, conv.id, tc.id, name, payload)
        await repo.log_action(
            session, conversation_id=conv.id, message_id=assistant_msg_id,
            action_type=tool.name, request=args, response=payload, status=status,
            idempotency_key=tc.id,
        )

    return pending


# --- helpers --------------------------------------------------------------


async def _safe_run_tool(
    tool: Tool, ctx: ToolContext, args: dict[str, Any]
) -> tuple[dict[str, Any], str]:
    try:
        result = await tool.run(ctx, args)
    except Exception as e:
        logger.exception("tool %s raised", tool.name)
        return {"ok": False, "error": str(e)}, "failed"
    return result.to_payload(), ("success" if result.ok else "failed")


async def _persist_tool_result(
    session: AsyncSession,
    conv_id: int,
    tool_call_id: str,
    tool_name: str,
    payload: dict[str, Any],
) -> None:
    await repo.append_message(
        session,
        conversation_id=conv_id,
        role="tool",
        content=json.dumps(payload, ensure_ascii=False),
        metadata={"tool_call_id": tool_call_id, "tool_name": tool_name},
    )


async def _clear_dangling_pending(session: AsyncSession, conv_id: int) -> None:
    """If a previous turn left a write action pending (user never confirmed), mark it
    ignored and inject a synthetic tool result so the OpenAI history is valid for the
    next LLM call."""
    pending_actions = await repo.find_pending_actions(session, conv_id)
    conv = await session.get(AiConversation, conv_id)
    if conv is None:
        return
    for action in pending_actions:
        await ignore_pending_action(session=session, conv=conv, action=action)


_llm_client_singleton: AsyncOpenAI | None = None


def _build_llm_client() -> AsyncOpenAI:
    global _llm_client_singleton
    if _llm_client_singleton is not None:
        return _llm_client_singleton
    settings = get_settings()
    if not settings.llm_api_key:
        raise AgentLoopError("LLM_API_KEY 未配置")
    _llm_client_singleton = AsyncOpenAI(
        api_key=settings.llm_api_key,
        base_url=settings.llm_base_url,
        timeout=settings.llm_timeout_seconds,
    )
    return _llm_client_singleton


async def _load_history_with_summary(
    session: AsyncSession, conv: AiConversation
) -> tuple[str | None, list[AiMessage]]:
    """Load only messages newer than the summarization high-water mark, plus the
    rolling summary text (if any). Capped at ~2x keep_recent for safety in case
    summarization hasn't caught up yet."""
    settings = get_settings()
    ctx = conv.context_json or {}
    summary = ctx.get("summary")
    summarized_through = int(ctx.get("summarized_through_id", 0) or 0)
    msgs = await repo.load_messages_after_id(session, conv.id, summarized_through)
    cap = max(settings.summarize_keep_recent * 2, 40)
    if len(msgs) > cap:
        msgs = msgs[-cap:]
    return summary, msgs


def _messages_to_openai_format(
    history: list, user_id: int, summary: str | None = None
) -> list[dict[str, Any]]:
    """Replay DB-persisted messages into OpenAI chat format.

    System prompt is added fresh each turn so the embedded current time stays accurate.
    A rolling summary, when present, is injected as a second system message ahead of
    the surviving recent messages.
    """
    out: list[dict[str, Any]] = [
        {"role": "system", "content": build_system_prompt(user_id)},
    ]
    if summary:
        out.append({"role": "system", "content": f"# 历史对话摘要\n{summary}"})
    for m in history:
        if m.message_role == "system":
            continue
        if m.message_role == "tool":
            meta = m.metadata_json or {}
            out.append({
                "role": "tool",
                "tool_call_id": meta.get("tool_call_id", ""),
                "content": m.content_text,
            })
            continue
        if m.message_role == "assistant":
            meta = m.metadata_json or {}
            tool_calls = meta.get("tool_calls")
            entry: dict[str, Any] = {"role": "assistant", "content": m.content_text or None}
            if tool_calls:
                entry["tool_calls"] = tool_calls
            out.append(entry)
            continue
        out.append({"role": "user", "content": m.content_text})
    return out
