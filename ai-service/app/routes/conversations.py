from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field, model_validator
from sqlalchemy.ext.asyncio import AsyncSession

from app.agent import repo
from app.agent.registry import build_tool_registry
from app.auth import CurrentUser, get_current_user
from app.db import get_session

router = APIRouter()
_TOOLS = build_tool_registry()
_SIDE_EFFECT_TOOL_NAMES = {name for name, tool in _TOOLS.items() if tool.side_effect}


class ConversationVO(BaseModel):
    id: int
    title: str | None
    status: str
    is_pinned: bool
    last_message_at: datetime | None
    created_at: datetime


class ConversationsPage(BaseModel):
    records: list[ConversationVO]
    total: int
    page: int
    page_size: int


class ConversationUpdate(BaseModel):
    """Rename and/or (un)pin. At least one field must be provided."""

    title: str | None = Field(default=None, min_length=1, max_length=128)
    pinned: bool | None = None

    @model_validator(mode="after")
    def _at_least_one(self) -> "ConversationUpdate":
        if self.title is None and self.pinned is None:
            raise ValueError("title or pinned is required")
        return self


class MessageVO(BaseModel):
    id: int
    role: str
    content: str | None = None
    pending: dict | None = None
    created_at: datetime


class MessagesPage(BaseModel):
    records: list[MessageVO]
    has_more: bool
    next_before_id: int | None = None


@router.get("/conversations", response_model=ConversationsPage)
async def list_conversations(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    user: CurrentUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_session),
) -> ConversationsPage:
    items, total = await repo.list_user_conversations(
        session, user.user_id, page=page, page_size=page_size
    )
    fallback_titles = await repo.load_first_user_messages(session, [c.id for c in items])
    return ConversationsPage(
        records=[
            ConversationVO(
                id=c.id,
                title=c.conversation_title or fallback_titles.get(c.id),
                status=c.conversation_status,
                is_pinned=c.is_pinned,
                last_message_at=c.last_message_at,
                created_at=c.created_at,
            )
            for c in items
        ],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.patch("/conversations/{conversation_id}", response_model=ConversationVO)
async def update_conversation(
    conversation_id: int,
    body: ConversationUpdate,
    user: CurrentUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_session),
) -> ConversationVO:
    conv = await repo.get_conversation_for_user(session, conversation_id, user.user_id)
    if conv is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="conversation not found")

    await repo.update_conversation_meta(session, conv, title=body.title, pinned=body.pinned)
    await session.commit()
    await session.refresh(conv)

    fallback_titles = await repo.load_first_user_messages(session, [conv.id])
    return ConversationVO(
        id=conv.id,
        title=conv.conversation_title or fallback_titles.get(conv.id),
        status=conv.conversation_status,
        is_pinned=conv.is_pinned,
        last_message_at=conv.last_message_at,
        created_at=conv.created_at,
    )


@router.delete("/conversations/{conversation_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_conversation(
    conversation_id: int,
    user: CurrentUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_session),
) -> None:
    conv = await repo.get_conversation_for_user(session, conversation_id, user.user_id)
    if conv is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="conversation not found")

    await repo.archive_conversation(session, conv)
    await session.commit()


@router.get("/conversations/{conversation_id}/messages", response_model=MessagesPage)
async def get_messages(
    conversation_id: int,
    limit: int = Query(100, ge=1, le=200),
    before_id: int | None = Query(None, ge=1),
    user: CurrentUser = Depends(get_current_user),
    session: AsyncSession = Depends(get_session),
) -> MessagesPage:
    conv = await repo.get_conversation_for_user(session, conversation_id, user.user_id)
    if conv is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="conversation not found")

    msgs, has_more, next_before_id = await repo.load_message_page(
        session,
        conversation_id,
        limit=limit,
        before_id=before_id,
    )
    action_logs = await repo.load_action_logs_for_message_ids(
        session,
        conversation_id,
        [m.id for m in msgs if m.message_role == "assistant"],
    )
    records = await _build_message_records(msgs, action_logs)
    return MessagesPage(records=records, has_more=has_more, next_before_id=next_before_id)


async def _build_message_records(msgs: list, action_logs: list) -> list[MessageVO]:
    visible_msgs = [m for m in msgs if m.message_role in ("user", "assistant")]

    actions_by_message_id: dict[int, list] = {}
    for action in action_logs:
        if action.message_id is None or action.action_type not in _SIDE_EFFECT_TOOL_NAMES:
            continue
        actions_by_message_id.setdefault(action.message_id, []).append(action)

    records: list[MessageVO] = []
    for msg in visible_msgs:
        records.append(
            MessageVO(id=msg.id, role=msg.message_role, content=msg.content_text, created_at=msg.created_at)
        )
        if msg.message_role != "assistant":
            continue
        for action in actions_by_message_id.get(msg.id, []):
            records.append(
                MessageVO(
                    id=-action.id,
                    role="pending",
                    pending={
                        "action_id": action.id,
                        "tool_name": action.action_type,
                        "summary": await _summarize_action(action.action_type, action.request_json or {}),
                        "params": action.request_json or {},
                        "status": _to_pending_card_status(action.action_status),
                    },
                    created_at=action.created_at,
                )
            )
    return records


async def _summarize_action(action_type: str, args: dict) -> str:
    tool = _TOOLS.get(action_type)
    if tool is not None:
        try:
            return await tool.summarize(None, args)
        except Exception:
            pass
    if action_type == "create_reservation":
        return (
            f"创建预约：座位 #{args.get('seatId')}，"
            f"{args.get('date')} {args.get('startTime')}-{args.get('endTime')}"
        )
    if action_type == "cancel_reservation":
        return f"取消预约 #{args.get('reservationId')}"
    return f"执行操作：{action_type}"


def _to_pending_card_status(action_status: str) -> str:
    if action_status == "success":
        return "confirmed"
    if action_status == "ignored":
        return "cancelled"
    if action_status == "failed":
        return "failed"
    return "pending"
