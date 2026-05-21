from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Protocol, runtime_checkable

from app.http_client import JavaApiClient


@dataclass
class ToolContext:
    """Per-turn context handed to every tool invocation."""

    user_id: int
    java: JavaApiClient


@dataclass
class ToolResult:
    """Uniform tool result. ``data`` is serialized into the LLM ``tool`` message."""

    ok: bool
    data: Any
    error: str | None = None

    def to_payload(self) -> dict[str, Any]:
        if self.ok:
            return {"ok": True, "data": self.data}
        return {"ok": False, "error": self.error}


@runtime_checkable
class Tool(Protocol):
    name: str
    description: str
    parameters: dict[str, Any]  # JSON Schema
    # Whether the tool mutates server state. True triggers the confirm-required halt
    # in the agent loop. Write tools should also implement ``summarize``.
    side_effect: bool

    async def run(self, ctx: ToolContext, args: dict[str, Any]) -> ToolResult: ...


async def summarize_tool_call(tool: Tool, ctx: ToolContext, args: dict[str, Any]) -> str:
    """Build a human-readable description for the confirmation card.

    Calls ``tool.summarize(ctx, args)`` if defined; otherwise falls back to a
    generic ``"工具 X 参数: ..."`` line. Keeps the loop free of hasattr checks.
    """
    summarize = getattr(tool, "summarize", None)
    if summarize is None:
        return f"调用工具 {tool.name}，参数：{args}"
    return await summarize(ctx, args)
