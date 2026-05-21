from typing import Any

from app.agent.tools.base import ToolContext, ToolResult
from app.http_client import JavaApiError


class ListNoticesTool:
    name = "list_notices"
    description = (
        "查询学生端可见的最新通知公告（仅已发布）。可按类型过滤："
        "system(系统) / rule(规则) / event(活动) / maintenance(维护)。"
    )
    parameters = {
        "type": "object",
        "properties": {
            "type": {
                "type": "string",
                "enum": ["system", "rule", "event", "maintenance"],
            },
            "page": {"type": "integer", "minimum": 1, "default": 1},
            "pageSize": {"type": "integer", "minimum": 1, "maximum": 50, "default": 10},
        },
        "required": [],
    }
    side_effect = False

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult:
        params = {
            "type": args.get("type"),
            "page": args.get("page", 1),
            "pageSize": args.get("pageSize", 10),
        }
        try:
            page = await ctx.java.get("/student/notices", params=params)
        except JavaApiError as e:
            return ToolResult(ok=False, data=None, error=e.message)

        records = (page or {}).get("records") or []
        keep = ("id", "title", "content", "type", "publishedAt", "authorName")
        slimmed = [{k: r.get(k) for k in keep if r.get(k) is not None} for r in records]
        return ToolResult(ok=True, data={"records": slimmed, "total": (page or {}).get("total", 0)})
