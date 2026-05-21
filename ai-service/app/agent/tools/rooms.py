from typing import Any

from app.agent.tools.base import ToolContext, ToolResult
from app.http_client import JavaApiError


class SearchRoomsTool:
    name = "search_rooms"
    description = "分页查询自习室列表（仅返回开放中的自习室）。可按关键字、容量过滤。"
    parameters = {
        "type": "object",
        "properties": {
            "keyword": {"type": "string", "description": "房间名/编号关键字（可选）"},
            "page": {"type": "integer", "description": "页码，默认 1", "minimum": 1},
            "pageSize": {"type": "integer", "description": "每页条数，默认 10", "minimum": 1, "maximum": 50},
        },
        "required": [],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        params = {
            "keyword": args.get("keyword"),
            "page": args.get("page", 1),
            "pageSize": args.get("pageSize", 10),
        }
        try:
            data = await ctx.java.get("/student/rooms", params=params)
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)
        return ToolResult(ok=True, data=_compact_room_page(data))


class GetRoomDetailTool:
    name = "get_room_detail"
    description = "查询单个自习室的详细信息及座位地图（含每个座位的状态、是否窗边、是否有电源等）。"
    parameters = {
        "type": "object",
        "properties": {
            "roomId": {"type": "integer", "description": "自习室 ID"},
        },
        "required": ["roomId"],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        room_id = args.get("roomId")
        if not isinstance(room_id, int):
            return ToolResult(ok=False, data=None, error="roomId 必须是整数")
        try:
            detail = await ctx.java.get(f"/student/rooms/{room_id}")
            seatmap = await ctx.java.get(f"/student/rooms/{room_id}/seatmap")
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)
        return ToolResult(
            ok=True,
            data={
                "room": _compact_room(detail) if detail else None,
                "seats": _compact_seats(seatmap.get("seats") if seatmap else None),
            },
        )


class GetSeatAvailabilityTool:
    name = "get_seat_availability"
    description = (
        "查询某个座位在指定日期已经被占用的时段（按 1-15 的小时槽编号，1=07:00-08:00, ..., 15=21:00-22:00）。"
        "调用前确保 date 是 ISO 格式 YYYY-MM-DD（必要时先用 resolve_date 转换）。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "seatId": {"type": "integer", "description": "座位 ID"},
            "date": {"type": "string", "description": "ISO 日期 YYYY-MM-DD"},
        },
        "required": ["seatId", "date"],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        seat_id = args.get("seatId")
        d = args.get("date")
        if not isinstance(seat_id, int):
            return ToolResult(ok=False, data=None, error="seatId 必须是整数")
        if not isinstance(d, str) or len(d) != 10:
            return ToolResult(ok=False, data=None, error="date 必须是 YYYY-MM-DD 格式")
        try:
            data = await ctx.java.get(f"/student/seats/{seat_id}/slots", params={"date": d})
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)
        slots = (data or {}).get("occupiedSlots") or []
        return ToolResult(
            ok=True,
            data={
                "seatId": seat_id,
                "date": d,
                "occupiedSlots": slots,
                "occupiedRanges": _slots_to_ranges(slots),
            },
        )


# --- shaping helpers ------------------------------------------------------

# The Java VOs include several fields that aren't useful for the LLM; trim them to
# keep the model focused and the token bill small.

_ROOM_FIELDS = ("id", "roomCode", "roomName", "displayName", "totalCapacity",
                "locationDetail", "descriptionText", "roomStatus")


def _compact_room(r: dict | None) -> dict | None:
    if not r:
        return None
    return {k: r.get(k) for k in _ROOM_FIELDS if r.get(k) is not None}


def _compact_room_page(page: dict | None) -> dict:
    if not page:
        return {"records": [], "total": 0}
    return {
        "records": [_compact_room(x) for x in (page.get("records") or [])],
        "total": page.get("total", 0),
        "page": page.get("page"),
        "pageSize": page.get("pageSize"),
    }


def _compact_seats(seats: list | None) -> list:
    if not seats:
        return []
    keep = ("id", "seatCode", "displayLabel", "seatType", "hasPower",
            "isWindowSide", "isAccessible", "seatStatus", "isBookable")
    return [{k: s.get(k) for k in keep if s.get(k) is not None} for s in seats]


def _slots_to_ranges(slots: list[int]) -> list[str]:
    """[2,3,4,7] -> ['08:00-11:00', '13:00-14:00'] (slot N covers hour 6+N to 7+N)."""
    if not slots:
        return []
    sorted_slots = sorted(set(int(s) for s in slots))
    ranges: list[str] = []
    start = sorted_slots[0]
    prev = start
    for s in sorted_slots[1:]:
        if s == prev + 1:
            prev = s
            continue
        ranges.append(f"{6+start:02d}:00-{7+prev:02d}:00")
        start = prev = s
    ranges.append(f"{6+start:02d}:00-{7+prev:02d}:00")
    return ranges
