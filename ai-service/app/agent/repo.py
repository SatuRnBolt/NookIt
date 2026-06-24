"""Persistence layer for the four ai_* tables.

Owns ALL writes to ai_conversations / ai_messages / ai_intents / ai_action_logs.
The Java backend does not touch these tables.
"""

from __future__ import annotations

import secrets
from typing import Any

from sqlalchemy import desc, func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models import AiActionLog, AiConversation, AiMessage


# --- conversations --------------------------------------------------------


async def create_conversation(
    session: AsyncSession,
    *,
    user_id: int,
    channel: str = "web",
    title: str | None = None,
) -> AiConversation:
    conv = AiConversation(
        user_id=user_id,
        channel=channel,
        session_token=secrets.token_urlsafe(24),
        conversation_title=title,
        conversation_status="active",
    )
    session.add(conv)
    await session.flush()
    return conv


async def get_conversation_for_user(
    session: AsyncSession, conversation_id: int, user_id: int
) -> AiConversation | None:
    stmt = select(AiConversation).where(
        AiConversation.id == conversation_id,
        AiConversation.user_id == user_id,
    )
    result = await session.execute(stmt)
    return result.scalar_one_or_none()


async def list_user_conversations(
    session: AsyncSession, user_id: int, *, page: int = 1, page_size: int = 20
) -> tuple[list[AiConversation], int]:
    offset = max(0, (page - 1) * page_size)
    # Archived conversations are soft-deleted and excluded from the list.
    # Pinned conversations float to the top, ordered by when they were pinned.
    visible = (
        AiConversation.user_id == user_id,
        AiConversation.conversation_status != "archived",
    )
    stmt = (
        select(AiConversation)
        .where(*visible)
        .order_by(
            desc(AiConversation.is_pinned),
            desc(AiConversation.pinned_at),
            desc(AiConversation.last_message_at),
            desc(AiConversation.id),
        )
        .offset(offset)
        .limit(page_size)
    )
    items = (await session.execute(stmt)).scalars().all()
    total = (
        await session.execute(select(func.count(AiConversation.id)).where(*visible))
    ).scalar_one()
    return list(items), int(total)


async def update_conversation_last_message_at(
    session: AsyncSession, conversation_id: int, ts
) -> None:
    conv = await session.get(AiConversation, conversation_id)
    if conv is not None:
        conv.last_message_at = ts


async def touch_conversation_last_message_at(
    session: AsyncSession, conversation_id: int
) -> None:
    await session.execute(
        update(AiConversation)
        .where(AiConversation.id == conversation_id)
        .values(last_message_at=func.current_timestamp())
    )


def normalize_conversation_title(text: str, max_len: int = 128) -> str:
    return " ".join(text.split())[:max_len]


async def set_conversation_title_if_missing(
    session: AsyncSession, conversation_id: int, title_source: str
) -> None:
    conv = await session.get(AiConversation, conversation_id)
    if conv is None or conv.conversation_title:
        return
    title = normalize_conversation_title(title_source)
    if title:
        conv.conversation_title = title


async def archive_conversation(session: AsyncSession, conv: AiConversation) -> None:
    """Soft-delete: mark as archived so it drops out of the user's list."""
    conv.conversation_status = "archived"
    await session.flush()


async def update_conversation_meta(
    session: AsyncSession,
    conv: AiConversation,
    *,
    title: str | None = None,
    pinned: bool | None = None,
) -> None:
    """Rename and/or (un)pin a conversation. Only the provided fields change."""
    if title is not None:
        conv.conversation_title = normalize_conversation_title(title)
    if pinned is not None:
        conv.is_pinned = pinned
        conv.pinned_at = func.current_timestamp() if pinned else None
    await session.flush()


# --- messages -------------------------------------------------------------


async def append_message(
    session: AsyncSession,
    *,
    conversation_id: int,
    role: str,
    content: str,
    intent_code: str | None = None,
    metadata: dict | None = None,
) -> AiMessage:
    msg = AiMessage(
        conversation_id=conversation_id,
        message_role=role,
        content_text=content,
        intent_code=intent_code,
        metadata_json=metadata,
    )
    session.add(msg)
    await session.flush()
    return msg


async def load_messages(
    session: AsyncSession, conversation_id: int, *, limit: int = 50
) -> list[AiMessage]:
    """Most recent ``limit`` messages, returned in chronological order."""
    stmt = (
        select(AiMessage)
        .where(AiMessage.conversation_id == conversation_id)
        .order_by(desc(AiMessage.id))
        .limit(limit)
    )
    rows = (await session.execute(stmt)).scalars().all()
    return list(reversed(rows))


async def load_message_page(
    session: AsyncSession,
    conversation_id: int,
    *,
    limit: int = 100,
    before_id: int | None = None,
) -> tuple[list[AiMessage], bool, int | None]:
    stmt = select(AiMessage).where(AiMessage.conversation_id == conversation_id)
    if before_id is not None:
        stmt = stmt.where(AiMessage.id < before_id)
    stmt = stmt.order_by(desc(AiMessage.id)).limit(limit + 1)

    rows = (await session.execute(stmt)).scalars().all()
    has_more = len(rows) > limit
    page_rows = rows[:limit]
    next_before_id = page_rows[-1].id if has_more and page_rows else None
    return list(reversed(page_rows)), has_more, next_before_id


async def load_action_logs_for_message_ids(
    session: AsyncSession,
    conversation_id: int,
    message_ids: list[int],
) -> list[AiActionLog]:
    if not message_ids:
        return []

    stmt = (
        select(AiActionLog)
        .where(
            AiActionLog.conversation_id == conversation_id,
            AiActionLog.message_id.in_(message_ids),
        )
        .order_by(AiActionLog.id)
    )
    return list((await session.execute(stmt)).scalars().all())


async def load_first_user_messages(
    session: AsyncSession, conversation_ids: list[int]
) -> dict[int, str]:
    if not conversation_ids:
        return {}

    stmt = (
        select(AiMessage.conversation_id, AiMessage.content_text)
        .where(
            AiMessage.conversation_id.in_(conversation_ids),
            AiMessage.message_role == "user",
        )
        .order_by(AiMessage.conversation_id, AiMessage.id)
    )
    rows = (await session.execute(stmt)).all()

    first_messages: dict[int, str] = {}
    for conversation_id, content_text in rows:
        if conversation_id in first_messages:
            continue
        title = normalize_conversation_title(content_text)
        if title:
            first_messages[conversation_id] = title
    return first_messages


async def load_messages_after_id(
    session: AsyncSession, conversation_id: int, min_id_exclusive: int
) -> list[AiMessage]:
    stmt = (
        select(AiMessage)
        .where(
            AiMessage.conversation_id == conversation_id,
            AiMessage.id > min_id_exclusive,
        )
        .order_by(AiMessage.id)
    )
    return list((await session.execute(stmt)).scalars().all())


async def load_messages_between(
    session: AsyncSession,
    conversation_id: int,
    min_id_exclusive: int,
    max_id_inclusive: int,
) -> list[AiMessage]:
    stmt = (
        select(AiMessage)
        .where(
            AiMessage.conversation_id == conversation_id,
            AiMessage.id > min_id_exclusive,
            AiMessage.id <= max_id_inclusive,
        )
        .order_by(AiMessage.id)
    )
    return list((await session.execute(stmt)).scalars().all())


async def count_messages(session: AsyncSession, conversation_id: int) -> int:
    stmt = select(func.count(AiMessage.id)).where(AiMessage.conversation_id == conversation_id)
    return int((await session.execute(stmt)).scalar_one())


async def get_summary_cutoff_id(
    session: AsyncSession, conversation_id: int, keep_recent: int
) -> int | None:
    """Return the id of the (keep_recent+1)-th newest message — i.e. the high-water
    mark such that messages with id <= cutoff should be folded into the summary while
    later messages are kept in full. Returns None if total messages <= keep_recent.
    """
    stmt = (
        select(AiMessage.id)
        .where(AiMessage.conversation_id == conversation_id)
        .order_by(desc(AiMessage.id))
        .offset(keep_recent)
        .limit(1)
    )
    return (await session.execute(stmt)).scalar_one_or_none()


async def update_conversation_context(
    session: AsyncSession, conversation_id: int, context: dict
) -> None:
    conv = await session.get(AiConversation, conversation_id)
    if conv is not None:
        conv.context_json = context


# --- action logs ----------------------------------------------------------


async def log_action(
    session: AsyncSession,
    *,
    conversation_id: int,
    message_id: int | None,
    action_type: str,
    request: dict[str, Any] | None,
    response: dict[str, Any] | None,
    status: str = "success",
    idempotency_key: str | None = None,
) -> AiActionLog:
    log = AiActionLog(
        conversation_id=conversation_id,
        message_id=message_id,
        action_type=action_type,
        action_status=status,
        idempotency_key=idempotency_key,
        request_json=request,
        response_json=response,
    )
    session.add(log)
    await session.flush()
    return log


async def get_action_for_user(
    session: AsyncSession, action_id: int, user_id: int
) -> AiActionLog | None:
    """Load an action log only if it belongs to a conversation owned by ``user_id``."""
    stmt = (
        select(AiActionLog)
        .join(AiConversation, AiConversation.id == AiActionLog.conversation_id)
        .where(AiActionLog.id == action_id, AiConversation.user_id == user_id)
    )
    return (await session.execute(stmt)).scalar_one_or_none()


async def update_action_result(
    session: AsyncSession,
    action: AiActionLog,
    *,
    status: str,
    response: dict[str, Any] | None,
) -> None:
    action.action_status = status
    action.response_json = response
    await session.flush()


async def find_pending_actions(
    session: AsyncSession, conversation_id: int
) -> list[AiActionLog]:
    stmt = (
        select(AiActionLog)
        .where(
            AiActionLog.conversation_id == conversation_id,
            AiActionLog.action_status == "pending",
        )
        .order_by(AiActionLog.id)
    )
    return list((await session.execute(stmt)).scalars().all())
