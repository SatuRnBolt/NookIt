from datetime import date, timedelta
from typing import Any
from zoneinfo import ZoneInfo

from app.agent.tools.base import ToolContext, ToolResult

_TZ = ZoneInfo("Asia/Shanghai")

_WEEKDAY_MAP = {
    "周一": 0, "星期一": 0, "礼拜一": 0,
    "周二": 1, "星期二": 1, "礼拜二": 1,
    "周三": 2, "星期三": 2, "礼拜三": 2,
    "周四": 3, "星期四": 3, "礼拜四": 3,
    "周五": 4, "星期五": 4, "礼拜五": 4,
    "周六": 5, "星期六": 5, "礼拜六": 5,
    "周日": 6, "周天": 6, "星期日": 6, "星期天": 6, "礼拜日": 6, "礼拜天": 6,
}


class ResolveDateTool:
    name = "resolve_date"
    description = (
        "把中文相对日期表述（如 今天/明天/后天/本周五/下周一/3 天后）转换为 ISO 格式 YYYY-MM-DD。"
        "对于学生口语中常见的时间词非常有用。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "expression": {
                "type": "string",
                "description": "中文日期表述，例如 '明天'、'本周五'、'下周一'、'3 天后'",
            },
        },
        "required": ["expression"],
    }
    side_effect = False

    async def run(self, _: ToolContext, args: dict[str, Any]) -> ToolResult:
        expr = (args.get("expression") or "").strip()
        if not expr:
            return ToolResult(ok=False, data=None, error="expression 不能为空")

        today = date.today()
        result = _resolve(expr, today)
        if result is None:
            return ToolResult(ok=False, data=None, error=f"无法识别日期表述：{expr}")
        return ToolResult(ok=True, data={"date": result.isoformat(), "weekday": result.isoweekday()})


def _resolve(expr: str, today: date) -> date | None:
    e = expr.strip()

    direct = {"今天": 0, "今日": 0, "明天": 1, "明日": 1, "后天": 2, "大后天": 3}
    if e in direct:
        return today + timedelta(days=direct[e])

    # "N 天后" / "N天后"
    e2 = e.replace(" ", "")
    if e2.endswith("天后") and e2[:-2].isdigit():
        return today + timedelta(days=int(e2[:-2]))

    # 本周X / 下周X / 下下周X
    week_offset = 0
    rest = e2
    for prefix, weeks in (("下下周", 2), ("下周", 1), ("本周", 0), ("这周", 0)):
        if rest.startswith(prefix):
            week_offset = weeks
            rest = rest[len(prefix):]
            break
    else:
        # Bare 周五 etc. -> nearest upcoming (today inclusive)
        if rest in _WEEKDAY_MAP:
            target_weekday = _WEEKDAY_MAP[rest]
            days_ahead = (target_weekday - today.weekday()) % 7
            return today + timedelta(days=days_ahead)
        return None

    if rest not in _WEEKDAY_MAP:
        return None
    target_weekday = _WEEKDAY_MAP[rest]
    monday_this_week = today - timedelta(days=today.weekday())
    return monday_this_week + timedelta(weeks=week_offset, days=target_weekday)
