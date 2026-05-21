from typing import Any

from app.agent.tools.base import Tool
from app.agent.tools.notices import ListNoticesTool
from app.agent.tools.reservations import (
    CancelReservationTool,
    CreateReservationTool,
    ListMyReservationsTool,
)
from app.agent.tools.rooms import GetRoomDetailTool, GetSeatAvailabilityTool, SearchRoomsTool
from app.agent.tools.time_utils import ResolveDateTool
from app.agent.tools.violations import ListMyViolationsTool


def build_tool_registry() -> dict[str, Tool]:
    """Single source of truth for which tools the agent can use. Add new tools here."""
    tools: list[Tool] = [
        ResolveDateTool(),
        SearchRoomsTool(),
        GetRoomDetailTool(),
        GetSeatAvailabilityTool(),
        ListMyReservationsTool(),
        ListNoticesTool(),
        ListMyViolationsTool(),
        CreateReservationTool(),
        CancelReservationTool(),
    ]
    return {t.name: t for t in tools}


def to_openai_tool_schemas(tools: dict[str, Tool]) -> list[dict[str, Any]]:
    return [
        {
            "type": "function",
            "function": {
                "name": t.name,
                "description": t.description,
                "parameters": t.parameters,
            },
        }
        for t in tools.values()
    ]
