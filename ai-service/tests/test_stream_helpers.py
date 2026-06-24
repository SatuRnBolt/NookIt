from app.agent.loop_stream import (
    _build_assistant_meta,
    _build_tool_calls,
    _err,
    _tool_result_event,
)


def test_build_tool_calls_orders_by_index():
    acc = {
        1: {"id": "call_b", "name": "tool_b", "arguments": '{"x":2}'},
        0: {"id": "call_a", "name": "tool_a", "arguments": '{"x":1}'},
    }
    out = _build_tool_calls(acc)
    assert [tc.id for tc in out] == ["call_a", "call_b"]
    assert [tc.function.name for tc in out] == ["tool_a", "tool_b"]


def test_build_tool_calls_skips_unnamed():
    acc = {0: {"id": "call_a", "name": "", "arguments": ""}}
    assert _build_tool_calls(acc) == []


def test_build_assistant_meta_none_when_no_calls():
    assert _build_assistant_meta([]) is None


def test_build_assistant_meta_shape():
    class _F:
        def __init__(self, name, args):
            self.name, self.arguments = name, args

    class _TC:
        def __init__(self, id_, name, args):
            self.id = id_
            self.function = _F(name, args)

    meta = _build_assistant_meta([_TC("call_1", "search_rooms", '{"k":1}')])
    assert meta == {
        "tool_calls": [
            {
                "id": "call_1",
                "type": "function",
                "function": {"name": "search_rooms", "arguments": '{"k":1}'},
            }
        ]
    }


def test_tool_result_event_success_excludes_error():
    ev = _tool_result_event("search_rooms", {"ok": True, "data": []}, "success")
    assert ev == {"type": "tool_result", "name": "search_rooms", "ok": True}


def test_tool_result_event_failure_includes_error():
    ev = _tool_result_event("create_reservation", {"ok": False, "error": "时段冲突"}, "failed")
    assert ev["ok"] is False
    assert ev["error"] == "时段冲突"


def test_err_shape():
    assert _err("boom") == {"type": "error", "message": "boom"}
