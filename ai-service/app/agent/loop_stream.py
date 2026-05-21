"""Streaming variant of the tool-use loop.

Mirrors ``loop.run_turn`` / ``loop.resume_from_action`` but yields a sequence of
SSE-ready event dicts instead of returning a single ``ChatResult``. The non-
streaming loop in ``loop.py`` remains the source of truth for shared helpers
(persistence, dangling-pending cleanup, OpenAI message replay, summarization
trigger); this file only reimplements the per-step LLM call so it can use
``stream=True`` and accumulate tool_call deltas.

Event shape (each yielded as the ``data`` field of an SSE message):
- ``{type: "token", text: "..."}``                 incremental assistant text
- ``{type: "tool_call", name, args}``              before a tool runs
- ``{type: "tool_result", name, ok, error?}``      after a read-only tool runs
- ``{type: "confirm_required", action_id, ...}``   write-tool halt; stream ends
- ``{type: "final", conversation_id}``             clean end of assistant turn
- ``{type: "error", message}``                     fatal error; stream ends
"""

from __future__ import annotations

import json
import logging
import time
from collections.abc import AsyncIterator
from types import SimpleNamespace
from typing import Any

from sqlalchemy.ext.asyncio import AsyncSession

from app.agent import repo
from app.agent.loop import (
    _SKIPPED_BATCH_PAYLOAD,
    _build_llm_client,
    _clear_dangling_pending,
    _load_history_with_summary,
    _messages_to_openai_format,
    _persist_tool_result,
    _safe_run_tool,
)
from app.agent.registry import to_openai_tool_schemas
from app.agent.summarizer import maybe_summarize
from app.agent.tools.base import Tool, ToolContext, summarize_tool_call
from app.config import get_settings
from app.http_client import JavaApiClient
from app.models import AiActionLog, AiConversation

logger = logging.getLogger(__name__)


# --- public entry points --------------------------------------------------


async def run_turn_stream(
    *,
    session: AsyncSession,
    conv: AiConversation,
    user_message: str,
    tools: dict[str, Tool],
    java: JavaApiClient,
) -> AsyncIterator[dict[str, Any]]:
    await _clear_dangling_pending(session, conv.id)
    await repo.append_message(
        session, conversation_id=conv.id, role="user", content=user_message
    )
    await repo.set_conversation_title_if_missing(session, conv.id, user_message)
    await repo.touch_conversation_last_message_at(session, conv.id)
    await session.commit()

    async for ev in _drive_loop_stream(session=session, conv=conv, tools=tools, java=java):
        yield ev

    await _post_turn_summarize(session, conv)


async def resume_from_action_stream(
    *,
    session: AsyncSession,
    conv: AiConversation,
    action: AiActionLog,
    tools: dict[str, Tool],
    java: JavaApiClient,
) -> AsyncIterator[dict[str, Any]]:
    if action.action_status != "pending":
        yield _err(f"action {action.id} is not pending (status={action.action_status})")
        return
    tool = tools.get(action.action_type)
    if tool is None:
        yield _err(f"unknown tool referenced by action: {action.action_type}")
        return

    ctx = ToolContext(user_id=conv.user_id, java=java)
    args = action.request_json or {}
    yield {"type": "tool_call", "name": tool.name, "args": args, "action_id": action.id}

    payload, status_str = await _safe_run_tool(tool, ctx, args)
    await repo.update_action_result(session, action, status=status_str, response=payload)
    await repo.append_message(
        session,
        conversation_id=conv.id,
        role="tool",
        content=json.dumps(payload, ensure_ascii=False),
        metadata={"tool_call_id": action.idempotency_key, "tool_name": tool.name},
    )
    await session.commit()
    yield _tool_result_event(tool.name, payload, status_str)

    async for ev in _drive_loop_stream(session=session, conv=conv, tools=tools, java=java):
        yield ev

    await _post_turn_summarize(session, conv)


# --- core stream loop -----------------------------------------------------


async def _drive_loop_stream(
    *, session: AsyncSession, conv: AiConversation, tools: dict[str, Tool], java: JavaApiClient
) -> AsyncIterator[dict[str, Any]]:
    settings = get_settings()
    client = _build_llm_client()
    tool_schemas = to_openai_tool_schemas(tools)
    ctx = ToolContext(user_id=conv.user_id, java=java)

    for step in range(settings.llm_max_tool_steps):
        summary, history = await _load_history_with_summary(session, conv)
        messages = _messages_to_openai_format(history, conv.user_id, summary)

        try:
            stream = await client.chat.completions.create(
                model=settings.llm_model,
                messages=messages,
                tools=tool_schemas,
                tool_choice="auto",
                stream=True,
            )
        except Exception as e:
            logger.exception("LLM stream open failed at step %d", step)
            yield _err(f"AI 服务暂时不可用：{e}")
            return

        content_acc = ""
        tool_calls_acc: dict[int, dict[str, Any]] = {}
        stream_error: Exception | None = None
        # Diagnostic: log the timing of token chunks so we can tell whether the
        # upstream LLM proxy is truly streaming or just batching. Enable by
        # setting log_level=DEBUG; first chunk + total at INFO either way.
        t_open = time.monotonic()
        first_token_at: float | None = None
        chunk_count = 0

        try:
            async for chunk in stream:
                if not chunk.choices:
                    continue
                choice = chunk.choices[0]
                delta = choice.delta
                if delta.content:
                    chunk_count += 1
                    if first_token_at is None:
                        first_token_at = time.monotonic()
                        logger.info(
                            "LLM first-token latency: %.2fs (step=%d)",
                            first_token_at - t_open, step,
                        )
                    logger.debug(
                        "LLM token chunk #%d t=%.3fs len=%d",
                        chunk_count, time.monotonic() - t_open, len(delta.content),
                    )
                    content_acc += delta.content
                    yield {"type": "token", "text": delta.content}
                if delta.tool_calls:
                    for tc in delta.tool_calls:
                        slot = tool_calls_acc.setdefault(
                            tc.index, {"id": None, "name": "", "arguments": ""}
                        )
                        if tc.id:
                            slot["id"] = tc.id
                        if tc.function:
                            if tc.function.name:
                                slot["name"] = tc.function.name
                            if tc.function.arguments:
                                slot["arguments"] += tc.function.arguments
                if choice.finish_reason:
                    break
        except Exception as e:
            stream_error = e

        if stream_error is not None:
            logger.exception("LLM stream chunk error at step %d", step, exc_info=stream_error)
            yield _err(f"AI 流式响应中断：{stream_error}")
            return

        if chunk_count > 0:
            logger.info(
                "LLM stream step=%d total_chunks=%d total_time=%.2fs avg_chunk_len=%.1f",
                step, chunk_count, time.monotonic() - t_open,
                len(content_acc) / chunk_count if chunk_count else 0,
            )

        tool_calls_list = _build_tool_calls(tool_calls_acc)
        assistant_msg = await repo.append_message(
            session,
            conversation_id=conv.id,
            role="assistant",
            content=content_acc,
            metadata=_build_assistant_meta(tool_calls_list),
        )

        if not tool_calls_list:
            await session.commit()
            yield {"type": "final", "conversation_id": conv.id}
            return

        halted = False
        for tc in tool_calls_list:
            if halted:
                # Synthetic result so the OpenAI history stays valid next turn.
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
            yield {"type": "tool_call", "name": name, "args": args}

            tool = tools.get(name)
            if tool is None:
                err_payload = {"ok": False, "error": f"unknown tool: {name}"}
                await _persist_tool_result(session, conv.id, tc.id, name, err_payload)
                await repo.log_action(
                    session, conversation_id=conv.id, message_id=assistant_msg.id,
                    action_type=name, request=args, response=err_payload, status="failed",
                    idempotency_key=tc.id,
                )
                yield {"type": "tool_result", "name": name, "ok": False, "error": err_payload["error"]}
                continue

            if tool.side_effect:
                summary_text = await summarize_tool_call(tool, ctx, args)
                action = await repo.log_action(
                    session, conversation_id=conv.id, message_id=assistant_msg.id,
                    action_type=tool.name, request=args, response=None, status="pending",
                    idempotency_key=tc.id,
                )
                await session.commit()
                yield {
                    "type": "confirm_required",
                    "conversation_id": conv.id,
                    "action_id": action.id,
                    "tool_name": tool.name,
                    "summary": summary_text,
                    "params": args,
                }
                halted = True
                continue

            tool_payload, status_str = await _safe_run_tool(tool, ctx, args)
            await _persist_tool_result(session, conv.id, tc.id, name, tool_payload)
            await repo.log_action(
                session, conversation_id=conv.id, message_id=assistant_msg.id,
                action_type=tool.name, request=args, response=tool_payload, status=status_str,
                idempotency_key=tc.id,
            )
            yield _tool_result_event(name, tool_payload, status_str)

        await session.commit()
        if halted:
            # The confirm_required event above already signalled the pause.
            # We end the stream without 'final' so the frontend stays in "awaiting
            # confirmation" state until the user confirms or sends a new message.
            return

    yield _err("对话步数已达上限，请重新发起一次请求或换一种说法。")


# --- helpers --------------------------------------------------------------


def _build_tool_calls(acc: dict[int, dict[str, Any]]) -> list:
    """Index-keyed delta accumulator -> the SimpleNamespace shape the rest of the
    loop expects (mimics non-streaming choice.tool_calls)."""
    out = []
    for idx in sorted(acc.keys()):
        raw = acc[idx]
        if not raw["name"]:
            continue  # incomplete tool call; defensive skip
        out.append(
            SimpleNamespace(
                id=raw["id"] or "",
                function=SimpleNamespace(name=raw["name"], arguments=raw["arguments"]),
            )
        )
    return out


def _build_assistant_meta(tool_calls_list: list) -> dict[str, Any] | None:
    if not tool_calls_list:
        return None
    return {
        "tool_calls": [
            {
                "id": tc.id,
                "type": "function",
                "function": {"name": tc.function.name, "arguments": tc.function.arguments},
            }
            for tc in tool_calls_list
        ]
    }


def _tool_result_event(name: str, payload: dict, status_str: str) -> dict[str, Any]:
    ev: dict[str, Any] = {"type": "tool_result", "name": name, "ok": status_str == "success"}
    if status_str != "success" and isinstance(payload, dict):
        err = payload.get("error")
        if err:
            ev["error"] = err
    return ev


def _err(message: str) -> dict[str, Any]:
    return {"type": "error", "message": message}


async def _post_turn_summarize(session: AsyncSession, conv: AiConversation) -> None:
    try:
        await maybe_summarize(session, conv, _build_llm_client())
    except Exception as e:
        logger.warning("post-turn summarize failed: %s", e)
