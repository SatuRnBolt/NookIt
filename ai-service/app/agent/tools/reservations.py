from datetime import datetime
from typing import Any

from app.agent.tools.base import ToolContext, ToolResult
from app.http_client import JavaApiError


class ListMyReservationsTool:
    name = "list_my_reservations"
    description = (
        "查询当前学生自己的预约列表。可按状态过滤："
        "pending_checkin(待签到) / checked_in(已签到) / cancelled(已取消) / "
        "violated(违约) / completed(已完成)。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "status": {
                "type": "string",
                "description": "可选状态过滤",
                "enum": ["pending_checkin", "checked_in", "cancelled", "violated", "completed"],
            },
            "page": {"type": "integer", "minimum": 1, "default": 1},
            "pageSize": {"type": "integer", "minimum": 1, "maximum": 50, "default": 10},
        },
        "required": [],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        params = {
            "status": args.get("status"),
            "page": args.get("page", 1),
            "pageSize": args.get("pageSize", 10),
        }
        try:
            page = await ctx.java.get("/student/reservations", params=params)
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)

        records = (page or {}).get("records") or []
        # Field names match StudentReservationServiceImpl#listMyReservations Map keys.
        keep = ("id", "roomName", "seatCode", "date", "startTime", "endTime", "status", "checkinCode")
        slimmed = [{k: r.get(k) for k in keep if r.get(k) is not None} for r in records]
        return ToolResult(
            ok=True,
            data={"records": slimmed, "total": (page or {}).get("total", 0)},
        )


class CreateReservationTool:
    """Create a single reservation. side_effect=True, so the agent loop halts
    on the LLM's first invocation, surfaces a confirmation card to the frontend,
    and only executes after the user confirms via ``confirmed_action_id``."""

    name = "create_reservation"
    description = (
        "为当前学生创建一条新预约。**这是写操作，调用前请先在文本中向学生明确说明你要预约的"
        "座位、日期、时段，并等待学生确认。** 服务端会在前端弹卡片让学生最终二次确认。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "seatId": {"type": "integer", "description": "座位 ID（先用 search_rooms + get_room_detail 获得）"},
            "date": {"type": "string", "description": "ISO 日期 YYYY-MM-DD"},
            "startTime": {"type": "string", "description": "开始时间 HH:mm，30 分钟粒度，例如 14:00 或 14:30"},
            "endTime": {"type": "string", "description": "结束时间 HH:mm，30 分钟粒度，且须晚于 startTime"},
        },
        "required": ["seatId", "date", "startTime", "endTime"],
    }
    side_effect = True

    async def summarize(self, ctx: ToolContext, args: dict[str, Any]) -> str:
        duration_minutes = _duration_minutes(args.get("startTime"), args.get("endTime"))
        duration_text = _format_duration(duration_minutes)
        return (
            f"创建预约：座位 #{args.get('seatId')}，"
            f"{args.get('date')} {args.get('startTime')}-{args.get('endTime')}"
            f"（共 {duration_text}）"
        )

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        body = {
            "seatId": args.get("seatId"),
            "date": args.get("date"),
            "startTime": args.get("startTime"),
            "endTime": args.get("endTime"),
        }
        if not _is_valid_create(body):
            return ToolResult(ok=False, data=None, error="参数不完整或不合法")
        try:
            data = await ctx.java.post("/student/reservations", json=body)
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)
        return ToolResult(ok=True, data=data)


class CancelReservationTool:
    name = "cancel_reservation"
    description = (
        "取消当前学生的某条预约。**这是写操作，调用前请先在文本中向学生确认要取消的预约。** "
        "服务端会在前端弹卡片让学生最终二次确认。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "reservationId": {"type": "integer", "description": "预约 ID（先用 list_my_reservations 获取）"},
        },
        "required": ["reservationId"],
    }
    side_effect = True

    async def summarize(self, ctx: ToolContext, args: dict[str, Any]) -> str:
        return f"取消预约 #{args.get('reservationId')}"

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        rid = args.get("reservationId")
        if not isinstance(rid, int):
            return ToolResult(ok=False, data=None, error="reservationId 必须是整数")
        try:
            await ctx.java.post(f"/student/reservations/{rid}/cancel")
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)
        return ToolResult(ok=True, data={"reservationId": rid, "cancelled": True})


def _is_valid_create(body: dict) -> bool:
    if not all(body.get(k) is not None for k in ("seatId", "date", "startTime", "endTime")):
        return False
    start_time = _parse_half_hour_time(body["startTime"])
    end_time = _parse_half_hour_time(body["endTime"])
    if start_time is None or end_time is None:
        return False
    return start_time < end_time


def _parse_half_hour_time(value: Any):
    if not isinstance(value, str):
        return None
    try:
        parsed = datetime.strptime(value, "%H:%M").time()
    except ValueError:
        return None
    if parsed.minute not in (0, 30):
        return None
    total_minutes = parsed.hour * 60 + parsed.minute
    if total_minutes < 7 * 60 or total_minutes > 22 * 60:
        return None
    return parsed


def _duration_minutes(start_time: Any, end_time: Any) -> int:
    start = _parse_half_hour_time(start_time)
    end = _parse_half_hour_time(end_time)
    if start is None or end is None:
        return 0
    return (end.hour * 60 + end.minute) - (start.hour * 60 + start.minute)


def _format_duration(duration_minutes: int) -> str:
    hours, minutes = divmod(max(duration_minutes, 0), 60)
    if minutes == 0:
        return f"{hours} 小时"
    if hours == 0:
        return f"{minutes} 分钟"
    return f"{hours} 小时 {minutes} 分钟"
