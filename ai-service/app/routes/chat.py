import json
from collections.abc import AsyncIterator
from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field, model_validator
from sqlalchemy.ext.asyncio import AsyncSession
from sse_starlette.sse import EventSourceResponse

from app.agent import repo
from app.agent.loop import ChatResult, ignore_pending_action, resume_from_action, run_turn
from app.agent.loop_stream import resume_from_action_stream, run_turn_stream
from app.agent.registry import build_tool_registry
from app.auth import CurrentUser, get_current_user
from app.db import SessionLocal, get_session
from app.http_client import JavaApiClient
from app.models import AiConversation
from app.rate_limit import enforce_chat_rate_limit

router = APIRouter()

_TOOLS = build_tool_registry()


class ChatRequest(BaseModel):
    conversation_id: int | None = Field(default=None, description="留空则新建会话")
    message: str | None = Field(default=None, min_length=1, max_length=4000)
    confirmed_action_id: int | None = Field(
        default=None,
        description="确认执行某条 pending 的写操作（卡片上点确认时由前端回传）",
    )

    @model_validator(mode="after")
    def _exactly_one_intent(self) -> "ChatRequest":
        has_msg = self.message is not None
        has_confirm = self.confirmed_action_id is not None
        if has_msg == has_confirm:
            raise ValueError("必须且只能提供 message 或 confirmed_action_id 之一")
        if has_confirm and self.conversation_id is None:
            raise ValueError("确认操作时必须提供 conversation_id")
        return self


class PendingActionVO(BaseModel):
    action_id: int
    tool_name: str
    summary: str
    params: dict[str, Any]


class ChatResponse(BaseModel):
    conversation_id: int
    reply: str | None = None
    pending_action: PendingActionVO | None = None


class CancelPendingActionResponse(BaseModel):
    conversation_id: int
    action_id: int
    status: str


def _serialize_pending_params(tool_name: str, params: dict[str, Any]) -> dict[str, Any]:
    payload = dict(params)
    if tool_name == "cancel_reservation" and payload.get("reservationId") is not None:
        payload["reservationId"] = str(payload["reservationId"])
    return payload


# --- non-streaming endpoint ------------------------------------------------


@router.post("/chat", response_model=ChatResponse)
async def chat(
    req: ChatRequest,
    user: CurrentUser = Depends(enforce_chat_rate_limit),
    session: AsyncSession = Depends(get_session),
) -> ChatResponse:
    async with JavaApiClient(user.raw_token) as java:
        if req.confirmed_action_id is not None:
            conv, result = await _handle_confirm_sync(session, req, user, java)
        else:
            conv, result = await _handle_message_sync(session, req, user, java)

    return _to_response(conv, result)


@router.post("/chat/actions/{action_id}/cancel", response_model=CancelPendingActionResponse)
async def cancel_pending_action(
    action_id: int,
    conversation_id: int,
    user: CurrentUser = Depends(enforce_chat_rate_limit),
    session: AsyncSession = Depends(get_session),
) -> CancelPendingActionResponse:
    conv = await _resolve_conversation_for_id(session, conversation_id, user)
    action = await repo.get_action_for_user(session, action_id, user.user_id)
    if action is None or action.conversation_id != conv.id:
        raise HTTPException(status.HTTP_404_NOT_FOUND, detail="action not found")
    if action.action_status != "pending":
        raise HTTPException(
            status.HTTP_409_CONFLICT, detail=f"action already {action.action_status}"
        )

    await ignore_pending_action(session=session, conv=conv, action=action)
    await session.commit()
    return CancelPendingActionResponse(
        conversation_id=conv.id,
        action_id=action.id,
        status="cancelled",
    )


async def _handle_message_sync(
    session: AsyncSession, req: ChatRequest, user: CurrentUser, java: JavaApiClient
) -> tuple[AiConversation, ChatResult]:
    conv = await _resolve_conversation(session, req, user, create_if_missing=True)
    result = await run_turn(
        session=session, conv=conv, user_message=req.message or "", tools=_TOOLS, java=java
    )
    return conv, result


async def _handle_confirm_sync(
    session: AsyncSession, req: ChatRequest, user: CurrentUser, java: JavaApiClient
) -> tuple[AiConversation, ChatResult]:
    conv, action = await _resolve_conv_and_action(session, req, user)
    result = await resume_from_action(
        session=session, conv=conv, action=action, tools=_TOOLS, java=java
    )
    return conv, result


def _to_response(conv: AiConversation, result: ChatResult) -> ChatResponse:
    pending_vo: PendingActionVO | None = None
    if result.pending_action is not None:
        p = result.pending_action
        pending_vo = PendingActionVO(
            action_id=p.action_id,
            tool_name=p.tool_name,
            summary=p.summary,
            params=_serialize_pending_params(p.tool_name, p.params),
        )
    return ChatResponse(conversation_id=conv.id, reply=result.reply, pending_action=pending_vo)


# --- streaming endpoint ----------------------------------------------------


@router.post("/chat/stream")
async def chat_stream(
    req: ChatRequest,
    user: CurrentUser = Depends(enforce_chat_rate_limit),
) -> EventSourceResponse:
    """SSE stream of events for the chat turn. The agent loop yields:
    ``token`` (deltas), ``tool_call``, ``tool_result``, ``confirm_required``,
    ``final``, ``error``. The frontend should treat ``final`` and
    ``confirm_required`` as terminal — the server closes the stream there.

    Note: we do **not** use ``Depends(get_session)`` here. EventSourceResponse
    runs the generator after the route handler returns, so a request-scoped
    session would already be closed. We open a fresh session inside the
    generator so it stays alive for the full stream.
    """

    token = user.raw_token

    async def event_gen() -> AsyncIterator[dict[str, str]]:
        async with SessionLocal() as session:
            async with JavaApiClient(token) as java:
                try:
                    if req.confirmed_action_id is not None:
                        conv, action = await _resolve_conv_and_action(session, req, user)
                        stream = resume_from_action_stream(
                            session=session, conv=conv, action=action, tools=_TOOLS, java=java
                        )
                    else:
                        conv = await _resolve_conversation(
                            session, req, user, create_if_missing=True
                        )
                        stream = run_turn_stream(
                            session=session, conv=conv, user_message=req.message or "",
                            tools=_TOOLS, java=java,
                        )

                    async for event in stream:
                        yield {"data": json.dumps(event, ensure_ascii=False)}
                except HTTPException as e:
                    yield {"data": json.dumps({"type": "error", "message": e.detail},
                                              ensure_ascii=False)}
                except Exception as e:
                    yield {"data": json.dumps({"type": "error", "message": str(e)},
                                              ensure_ascii=False)}

    return EventSourceResponse(event_gen())


# --- shared resolvers ------------------------------------------------------


async def _resolve_conversation(
    session: AsyncSession,
    req: ChatRequest,
    user: CurrentUser,
    *,
    create_if_missing: bool,
) -> AiConversation:
    if req.conversation_id is None:
        if not create_if_missing:
            raise HTTPException(status.HTTP_400_BAD_REQUEST, detail="conversation_id required")
        conv = await repo.create_conversation(session, user_id=user.user_id, channel="web")
        await session.commit()
        return conv
    conv = await repo.get_conversation_for_user(session, req.conversation_id, user.user_id)
    if conv is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, detail="conversation not found")
    return conv


async def _resolve_conversation_for_id(
    session: AsyncSession,
    conversation_id: int,
    user: CurrentUser,
) -> AiConversation:
    conv = await repo.get_conversation_for_user(session, conversation_id, user.user_id)
    if conv is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, detail="conversation not found")
    return conv


async def _resolve_conv_and_action(
    session: AsyncSession, req: ChatRequest, user: CurrentUser
) -> tuple[AiConversation, Any]:
    assert req.conversation_id is not None and req.confirmed_action_id is not None
    conv = await _resolve_conversation(session, req, user, create_if_missing=False)
    action = await repo.get_action_for_user(session, req.confirmed_action_id, user.user_id)
    if action is None or action.conversation_id != conv.id:
        raise HTTPException(status.HTTP_404_NOT_FOUND, detail="action not found")
    if action.action_status != "pending":
        raise HTTPException(
            status.HTTP_409_CONFLICT, detail=f"action already {action.action_status}"
        )
    return conv, action
