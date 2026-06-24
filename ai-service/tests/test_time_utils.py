from datetime import date

from app.agent.tools.time_utils import _resolve


def test_today_tomorrow():
    base = date(2026, 5, 16)  # Saturday
    assert _resolve("今天", base) == base
    assert _resolve("明天", base) == date(2026, 5, 17)
    assert _resolve("后天", base) == date(2026, 5, 18)
    assert _resolve("大后天", base) == date(2026, 5, 19)


def test_n_days_later():
    base = date(2026, 5, 16)
    assert _resolve("3天后", base) == date(2026, 5, 19)
    assert _resolve("10 天后", base) == date(2026, 5, 26)


def test_weekday_this_week():
    base = date(2026, 5, 16)  # Saturday -> weekday=5
    assert _resolve("本周一", base) == date(2026, 5, 11)
    assert _resolve("本周五", base) == date(2026, 5, 15)


def test_weekday_next_week():
    base = date(2026, 5, 16)
    assert _resolve("下周一", base) == date(2026, 5, 18)
    assert _resolve("下周五", base) == date(2026, 5, 22)
    assert _resolve("下下周一", base) == date(2026, 5, 25)


def test_bare_weekday_is_next_upcoming():
    base = date(2026, 5, 16)  # Saturday
    assert _resolve("周一", base) == date(2026, 5, 18)
    assert _resolve("周六", base) == date(2026, 5, 16)


def test_unknown_returns_none():
    assert _resolve("八月十五", date(2026, 5, 16)) is None
