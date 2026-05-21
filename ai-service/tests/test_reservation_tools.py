import pytest

from app.agent.tools.reservations import (
    CancelReservationTool,
    CreateReservationTool,
    _is_valid_create,
)


def test_create_valid_args():
    assert _is_valid_create({"seatId": 1, "date": "2026-05-17", "startTime": "14:00", "endTime": "16:00"})
    assert _is_valid_create({"seatId": 1, "date": "2026-05-17", "startTime": "14:30", "endTime": "16:00"})


@pytest.mark.parametrize(
    "body",
    [
        {"seatId": 1, "date": "2026-05-17", "startTime": "14:00"},  # missing endTime
        {"seatId": 1, "date": "2026-05-17", "startTime": "16:00", "endTime": "14:00"},  # end <= start
        {"seatId": 1, "date": "2026-05-17", "startTime": "06:30", "endTime": "08:00"},  # too early
        {"seatId": 1, "date": "2026-05-17", "startTime": "21:30", "endTime": "22:30"},  # too late
        {"seatId": 1, "date": "2026-05-17", "startTime": "14:15", "endTime": "16:00"},  # wrong granularity
        {"seatId": 1, "date": "2026-05-17", "startTime": 14, "endTime": 16},  # wrong types
    ],
)
def test_create_invalid_args(body):
    assert not _is_valid_create(body)


@pytest.mark.asyncio
async def test_create_summary():
    tool = CreateReservationTool()
    summary = await tool.summarize(
        None,
        {"seatId": 5, "date": "2026-05-17", "startTime": "14:30", "endTime": "16:00"},
    )
    assert "座位 #5" in summary
    assert "2026-05-17" in summary
    assert "14:30-16:00" in summary
    assert "1 小时 30 分钟" in summary


@pytest.mark.asyncio
async def test_cancel_summary():
    tool = CancelReservationTool()
    summary = await tool.summarize(None, {"reservationId": 42})
    assert "#42" in summary
