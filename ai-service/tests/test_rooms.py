from app.agent.tools.rooms import (
    _compact_room,
    _compact_room_page,
    _compact_seats,
    _slots_to_ranges,
)


def test_compact_room_keeps_known_fields_and_drops_none():
    room = {
        "id": 1,
        "roomName": "三楼自习室",
        "totalCapacity": 40,
        "roomStatus": "open",
        "extraField": "ignored",
        "descriptionText": None,
    }
    out = _compact_room(room)
    assert out == {"id": 1, "roomName": "三楼自习室", "totalCapacity": 40, "roomStatus": "open"}
    assert "extraField" not in out
    assert "descriptionText" not in out  # None values are stripped


def test_compact_room_handles_none():
    assert _compact_room(None) is None


def test_compact_room_page_shape():
    page = {
        "records": [{"id": 1, "roomName": "A"}, {"id": 2, "roomName": "B"}],
        "total": 2,
        "page": 1,
        "pageSize": 10,
    }
    out = _compact_room_page(page)
    assert out["total"] == 2
    assert out["page"] == 1
    assert [r["id"] for r in out["records"]] == [1, 2]


def test_compact_room_page_handles_none():
    assert _compact_room_page(None) == {"records": [], "total": 0}


def test_compact_seats_filters_fields():
    seats = [{"id": 1, "seatCode": "A-01", "hasPower": True, "junk": "x"}]
    out = _compact_seats(seats)
    assert out == [{"id": 1, "seatCode": "A-01", "hasPower": True}]


def test_compact_seats_handles_empty():
    assert _compact_seats(None) == []
    assert _compact_seats([]) == []


def test_slots_to_ranges_merges_contiguous():
    # slot N covers hour 6+N to 7+N: 2->08:00-09:00 ... contiguous 2,3,4 merge.
    assert _slots_to_ranges([2, 3, 4, 7]) == ["08:00-11:00", "13:00-14:00"]


def test_slots_to_ranges_single_and_unsorted():
    assert _slots_to_ranges([1]) == ["07:00-08:00"]
    assert _slots_to_ranges([4, 2, 3]) == ["08:00-11:00"]  # sorted + deduped internally


def test_slots_to_ranges_empty():
    assert _slots_to_ranges([]) == []
