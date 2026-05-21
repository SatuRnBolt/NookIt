from typing import Any

from app.agent.tools.base import ToolContext, ToolResult
from app.http_client import JavaApiError


class ListMyViolationsTool:
    name = "list_my_violations"
    description = "查询当前学生本人的违约记录（含未申诉/申诉中/已撤销），并返回当前账户状态摘要。"
    parameters = {
        "type": "object",
        "properties": {},
        "required": [],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        try:
            data = await ctx.java.get("/student/violations")
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)

        records = (data or {}).get("records") or []
        keep = ("id", "violationType", "violationStatus", "descriptionText",
                "occurredAt", "roomName", "seatCode")
        slimmed = [{k: r.get(k) for k in keep if r.get(k) is not None} for r in records]
        return ToolResult(
            ok=True,
            data={"records": slimmed, "stats": (data or {}).get("stats", {})},
        )
