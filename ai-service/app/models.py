from datetime import datetime
from decimal import Decimal

from sqlalchemy import (
    JSON,
    BigInteger,
    DateTime,
    Enum,
    ForeignKey,
    Numeric,
    String,
    Text,
    func,
)
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class AiConversation(Base):
    __tablename__ = "ai_conversations"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False)
    channel: Mapped[str] = mapped_column(
        Enum("web", "mini_program", "admin", name="ai_channel"), nullable=False
    )
    session_token: Mapped[str] = mapped_column(String(128), nullable=False, unique=True)
    conversation_title: Mapped[str | None] = mapped_column(String(128), nullable=True)
    conversation_status: Mapped[str] = mapped_column(
        Enum("active", "closed", "archived", name="ai_conv_status"),
        nullable=False,
        default="active",
    )
    context_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    last_message_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.current_timestamp()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False,
        server_default=func.current_timestamp(),
        onupdate=func.current_timestamp(),
    )


class AiMessage(Base):
    __tablename__ = "ai_messages"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    conversation_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("ai_conversations.id"), nullable=False
    )
    message_role: Mapped[str] = mapped_column(
        Enum("user", "assistant", "system", "tool", name="ai_msg_role"), nullable=False
    )
    content_text: Mapped[str] = mapped_column(Text, nullable=False)
    intent_code: Mapped[str | None] = mapped_column(String(64), nullable=True)
    referenced_entity_type: Mapped[str | None] = mapped_column(String(32), nullable=True)
    referenced_entity_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    metadata_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.current_timestamp()
    )


class AiIntent(Base):
    __tablename__ = "ai_intents"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    conversation_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("ai_conversations.id"), nullable=False
    )
    message_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("ai_messages.id"), nullable=False)
    intent_code: Mapped[str] = mapped_column(String(64), nullable=False)
    confidence_score: Mapped[Decimal] = mapped_column(Numeric(5, 4), nullable=False)
    extracted_slots_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    route_status: Mapped[str] = mapped_column(
        Enum("matched_rule", "llm_enhanced", "rejected", "executed", name="ai_route_status"),
        nullable=False,
        default="matched_rule",
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.current_timestamp()
    )


class AiActionLog(Base):
    __tablename__ = "ai_action_logs"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    conversation_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("ai_conversations.id"), nullable=False
    )
    message_id: Mapped[int | None] = mapped_column(
        BigInteger, ForeignKey("ai_messages.id"), nullable=True
    )
    action_type: Mapped[str] = mapped_column(String(64), nullable=False)
    target_type: Mapped[str | None] = mapped_column(String(32), nullable=True)
    target_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    action_status: Mapped[str] = mapped_column(
        Enum("pending", "success", "failed", "ignored", name="ai_action_status"),
        nullable=False,
        default="pending",
    )
    idempotency_key: Mapped[str | None] = mapped_column(String(64), nullable=True)
    request_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    response_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.current_timestamp()
    )
