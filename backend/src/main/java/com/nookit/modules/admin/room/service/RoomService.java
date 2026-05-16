package com.nookit.modules.admin.room.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.room.dto.*;
import com.nookit.security.UserPrincipal;

import java.util.List;
import java.util.Map;

public interface RoomService {

    PageResult<RoomVO> pageRooms(RoomPageQuery query);

    RoomVO createRoom(RoomCreateReq req, UserPrincipal operator);

    RoomVO getRoomById(Long id);

    RoomVO updateRoom(Long id, RoomCreateReq req, UserPrincipal operator);

    void deleteRoom(Long id);

    void updateRoomStatus(Long id, String roomStatus);

    RoomStatsVO getStats();

    // Seat map
    SeatMapVO getSeatMap(Long roomId, Integer version);

    SeatMapVO createSeatMapDraft(Long roomId, SeatMapCreateReq req, UserPrincipal operator);

    void updateSeatMap(Long roomId, Long mapId, SeatMapCreateReq req);

    void publishSeatMap(Long roomId, Long mapId);

    // Seats inside a map
    SeatMapVO.SeatVO addSeat(Long roomId, Long mapId, SeatInMapReq req);

    void updateSeat(Long roomId, Long mapId, Long seatId, SeatInMapReq req);

    void deleteSeat(Long roomId, Long mapId, Long seatId);

    SeatMapVO.SeatVO duplicateSeat(Long roomId, Long mapId, Long seatId, Double mapX, Double mapY);

    // Dropdown helpers
    List<String> listCampuses();

    List<Map<String, Object>> listOrganizations();

    List<Map<String, Object>> listRoomTypes();

    List<Map<String, Object>> listRoomsSimple();
}
