from datetime import datetime
from types import SimpleNamespace

import pytest

from app.agent import repo
from app.routes.conversations import _build_message_records, _to_pending_card_status


class _FakeScalarResult:
    def __init__(self, rows):
        self._rows = rows

    def all(self):
        return self._rows


class _FakeResult:
    def __init__(self, rows):
        self._rows = rows

    def scalars(self):
        return _FakeScalarResult(self._rows)


class _CapturingSession:
    def __init__(self, rows=None):
        self.rows = rows or []
        self.statement = None

    async def execute(self, statement):
        self.statement = statement
        return _FakeResult(self.rows)


@pytest.mark.asyncio
async def test_load_message_page_returns_chronological_records_and_cursor():
    session = _CapturingSession(
        [
            SimpleNamespace(id=9),
            SimpleNamespace(id=8),
            SimpleNamespace(id=7),
            SimpleNamespace(id=6),
        ]
    )

    records, has_more, next_before_id = await repo.load_message_page(
        session,
        conversation_id=12,
        limit=3,
        before_id=20,
    )

    assert [row.id for row in records] == [7, 8, 9]
    assert has_more is True
    assert next_before_id == 7


@pytest.mark.asyncio
async def test_touch_conversation_last_message_uses_database_timestamp():
    session = _CapturingSession()

    await repo.touch_conversation_last_message_at(session, conversation_id=42)

    sql = str(session.statement)
    assert "UPDATE ai_conversations" in sql
    assert "CURRENT_TIMESTAMP" in sql


@pytest.mark.asyncio
async def test_build_message_records_interleaves_pending_cards_with_statuses():
    created_at = datetime(2026, 5, 21, 10, 0, 0)
    assistant_at = datetime(2026, 5, 21, 10, 1, 0)
    action_at = datetime(2026, 5, 21, 10, 1, 30)
    msgs = [
        SimpleNamespace(id=1, message_role="user", content_text="帮我预约", created_at=created_at),
        SimpleNamespace(id=2, message_role="assistant", content_text="我来处理", created_at=assistant_at),
        SimpleNamespace(id=3, message_role="tool", content_text="{}", created_at=assistant_at),
    ]
    actions = [
        SimpleNamespace(
            id=11,
            message_id=2,
            action_type="create_reservation",
            request_json={"seatId": 8, "date": "2026-05-21", "startTime": "09:00", "endTime": "11:00"},
            action_status="success",
            created_at=action_at,
        )
    ]

    records = await _build_message_records(msgs, actions)

    assert [record.role for record in records] == ["user", "assistant", "pending"]
    assert records[2].pending == {
        "action_id": 11,
        "tool_name": "create_reservation",
        "summary": "创建预约：座位 #8，2026-05-21 09:00-11:00",
        "params": {
            "seatId": 8,
            "date": "2026-05-21",
            "startTime": "09:00",
            "endTime": "11:00",
        },
        "status": "confirmed",
    }


def test_pending_status_mapping():
    assert _to_pending_card_status("success") == "confirmed"
    assert _to_pending_card_status("ignored") == "cancelled"
    assert _to_pending_card_status("failed") == "failed"
    assert _to_pending_card_status("pending") == "pending"