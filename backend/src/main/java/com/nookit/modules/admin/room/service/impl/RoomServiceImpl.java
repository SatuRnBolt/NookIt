package com.nookit.modules.admin.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.seat.Seat;
import com.nookit.common.domain.seat.SeatMap;
import com.nookit.common.domain.space.StudyRoom;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.room.dto.*;
import com.nookit.modules.admin.room.mapper.DictMapper;
import com.nookit.modules.admin.room.mapper.RoomMapper;
import com.nookit.modules.admin.room.mapper.SeatMapMapper;
import com.nookit.modules.admin.room.mapper.SeatMapper;
import com.nookit.modules.admin.room.service.RoomService;
import com.nookit.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final SeatMapMapper seatMapMapper;
    private final SeatMapper seatMapper;
    private final DictMapper dictMapper;

    @Override
    public PageResult<RoomVO> pageRooms(RoomPageQuery query) {
        Page<RoomVO> page = new Page<>(query.getPage(), query.getPageSize());
        roomMapper.pageRooms(page, query);
        return PageResult.from(page);
    }

    @Override
    @Transactional
    public RoomVO createRoom(RoomCreateReq req, UserPrincipal operator) {
        StudyRoom room = buildStudyRoom(req, operator.getUserId());
        roomMapper.insert(room);
        return roomMapper.findRoomDetail(room.getId());
    }

    @Override
    public RoomVO getRoomById(Long id) {
        RoomVO vo = roomMapper.findRoomDetail(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.ROOM_NOT_FOUND);
        return vo;
    }

    @Override
    @Transactional
    public RoomVO updateRoom(Long id, RoomCreateReq req, UserPrincipal operator) {
        if (roomMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.ROOM_NOT_FOUND);
        StudyRoom room = buildStudyRoom(req, operator.getUserId());
        room.setId(id);
        roomMapper.updateById(room);
        return roomMapper.findRoomDetail(id);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (roomMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.ROOM_NOT_FOUND);
        roomMapper.deleteById(id);
    }

    @Override
    public void updateRoomStatus(Long id, String roomStatus) {
        if (roomMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.ROOM_NOT_FOUND);
        StudyRoom upd = new StudyRoom();
        upd.setId(id);
        upd.setRoomStatus(roomStatus);
        roomMapper.updateById(upd);
    }

    @Override
    public RoomStatsVO getStats() {
        return roomMapper.queryStats();
    }

    @Override
    public SeatMapVO getSeatMap(Long roomId, Integer version) {
        SeatMapVO map;
        if (version != null) {
            SeatMap sm = seatMapMapper.selectOne(
                    new LambdaQueryWrapper<SeatMap>()
                            .eq(SeatMap::getStudyRoomId, roomId)
                            .eq(SeatMap::getVersionNo, version));
            if (sm == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
            map = toSeatMapVO(sm);
        } else {
            map = seatMapMapper.findPublishedMap(roomId);
            if (map == null) return null;
        }
        map.setSeats(seatMapMapper.findSeatsByMapId(map.getId()));
        return map;
    }

    @Override
    @Transactional
    public SeatMapVO createSeatMapDraft(Long roomId, SeatMapCreateReq req, UserPrincipal operator) {
        int nextVersion = seatMapMapper.findNextVersionNo(roomId);
        SeatMap draft = new SeatMap();
        draft.setStudyRoomId(roomId);
        draft.setVersionNo(nextVersion);
        draft.setMapStatus("draft");
        draft.setMapWidth(req.getMapWidth());
        draft.setMapHeight(req.getMapHeight());
        draft.setBackgroundUrl(req.getBackgroundUrl());
        draft.setCreatedBy(operator.getUserId());
        seatMapMapper.insert(draft);
        SeatMapVO vo = toSeatMapVO(draft);
        vo.setSeats(List.of());
        return vo;
    }

    @Override
    public void updateSeatMap(Long roomId, Long mapId, SeatMapCreateReq req) {
        SeatMap sm = seatMapMapper.selectById(mapId);
        if (sm == null || !sm.getStudyRoomId().equals(roomId)) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        if (req.getMapWidth() != null) sm.setMapWidth(req.getMapWidth());
        if (req.getMapHeight() != null) sm.setMapHeight(req.getMapHeight());
        if (req.getBackgroundUrl() != null) sm.setBackgroundUrl(req.getBackgroundUrl());
        seatMapMapper.updateById(sm);
    }

    @Override
    public void publishSeatMap(Long roomId, Long mapId) {
        SeatMap sm = seatMapMapper.selectById(mapId);
        if (sm == null || !sm.getStudyRoomId().equals(roomId)) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        sm.setMapStatus("published");
        sm.setPublishedAt(LocalDateTime.now());
        seatMapMapper.updateById(sm);
    }

    @Override
    @Transactional
    public SeatMapVO.SeatVO addSeat(Long roomId, Long mapId, SeatInMapReq req) {
        Seat seat = buildSeat(roomId, req);
        seatMapper.insert(seat);
        return toSeatVO(seat);
    }

    @Override
    public void updateSeat(Long roomId, Long mapId, Long seatId, SeatInMapReq req) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || !seat.getStudyRoomId().equals(roomId)) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        applySeatReq(seat, req);
        seatMapper.updateById(seat);
    }

    @Override
    public void deleteSeat(Long roomId, Long mapId, Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || !seat.getStudyRoomId().equals(roomId)) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        seatMapper.deleteById(seatId);
    }

    @Override
    @Transactional
    public SeatMapVO.SeatVO duplicateSeat(Long roomId, Long mapId, Long seatId, Double mapX, Double mapY) {
        Seat original = seatMapper.selectById(seatId);
        if (original == null) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        Seat copy = new Seat();
        copy.setStudyRoomId(original.getStudyRoomId());
        copy.setSeatCode(original.getSeatCode() + "_" + System.currentTimeMillis());
        copy.setDisplayLabel(original.getDisplayLabel() + "(副本)");
        copy.setSeatType(original.getSeatType());
        copy.setHasPower(original.getHasPower());
        copy.setIsWindowSide(original.getIsWindowSide());
        copy.setIsAccessible(original.getIsAccessible());
        copy.setSeatStatus(original.getSeatStatus());
        copy.setIsBookable(original.getIsBookable());
        copy.setMapX(mapX != null ? BigDecimal.valueOf(mapX) : original.getMapX());
        copy.setMapY(mapY != null ? BigDecimal.valueOf(mapY) : original.getMapY());
        copy.setMapWidth(original.getMapWidth());
        copy.setMapHeight(original.getMapHeight());
        copy.setMapRotation(original.getMapRotation());
        seatMapper.insert(copy);
        return toSeatVO(copy);
    }

    @Override
    public List<String> listCampuses() {
        return dictMapper.listCampusNames();
    }

    @Override
    public List<Map<String, Object>> listOrganizations() {
        return dictMapper.listOrganizations();
    }

    @Override
    public List<Map<String, Object>> listRoomTypes() {
        return dictMapper.listRoomTypes();
    }

    @Override
    public List<Map<String, Object>> listRoomsSimple() {
        return roomMapper.listSimple();
    }

    // ---- helpers ----

    private StudyRoom buildStudyRoom(RoomCreateReq req, Long operatorId) {
        StudyRoom r = new StudyRoom();
        r.setRoomCode(req.getRoomCode());
        r.setRoomName(req.getRoomName());
        r.setDisplayName(req.getDisplayName());
        r.setCampusId(req.getCampusId());
        r.setBuildingId(req.getBuildingId());
        r.setFloorId(req.getFloorId());
        r.setOwnerOrganizationId(req.getOwnerOrganizationId());
        r.setRoomTypeId(req.getRoomTypeId());
        r.setTotalCapacity(req.getTotalCapacity());
        r.setVisibilityScope(req.getVisibilityScope());
        r.setOpenRuleType(req.getOpenRuleType());
        r.setRoomStatus(req.getRoomStatus());
        r.setLocationDetail(req.getLocationDetail());
        r.setDescriptionText(req.getDescriptionText());
        r.setCreatedBy(operatorId);
        r.setUpdatedBy(operatorId);
        return r;
    }

    private Seat buildSeat(Long roomId, SeatInMapReq req) {
        Seat s = new Seat();
        s.setStudyRoomId(roomId);
        applySeatReq(s, req);
        return s;
    }

    private void applySeatReq(Seat s, SeatInMapReq req) {
        s.setSeatCode(req.getSeatCode());
        s.setDisplayLabel(req.getDisplayLabel());
        s.setSeatType(req.getSeatType());
        s.setRowNo(req.getRowNo());
        s.setColNo(req.getColNo());
        s.setHasPower(req.getHasPower());
        s.setIsWindowSide(req.getIsWindowSide());
        s.setIsAccessible(req.getIsAccessible());
        s.setSeatStatus(req.getSeatStatus());
        s.setIsBookable(req.getIsBookable());
        s.setMapX(req.getMapX() != null ? BigDecimal.valueOf(req.getMapX()) : null);
        s.setMapY(req.getMapY() != null ? BigDecimal.valueOf(req.getMapY()) : null);
        s.setMapWidth(req.getMapWidth() != null ? BigDecimal.valueOf(req.getMapWidth()) : null);
        s.setMapHeight(req.getMapHeight() != null ? BigDecimal.valueOf(req.getMapHeight()) : null);
        s.setMapRotation(req.getMapRotation() != null ? BigDecimal.valueOf(req.getMapRotation()) : null);
    }

    private SeatMapVO toSeatMapVO(SeatMap sm) {
        SeatMapVO vo = new SeatMapVO();
        vo.setId(sm.getId());
        vo.setVersionNo(sm.getVersionNo());
        vo.setMapStatus(sm.getMapStatus());
        vo.setMapWidth(sm.getMapWidth());
        vo.setMapHeight(sm.getMapHeight());
        vo.setBackgroundUrl(sm.getBackgroundUrl());
        vo.setPublishedAt(sm.getPublishedAt());
        return vo;
    }

    private SeatMapVO.SeatVO toSeatVO(Seat s) {
        SeatMapVO.SeatVO vo = new SeatMapVO.SeatVO();
        vo.setId(s.getId());
        vo.setSeatCode(s.getSeatCode());
        vo.setDisplayLabel(s.getDisplayLabel());
        vo.setSeatType(s.getSeatType());
        vo.setRowNo(s.getRowNo());
        vo.setColNo(s.getColNo());
        vo.setHasPower(s.getHasPower());
        vo.setIsWindowSide(s.getIsWindowSide());
        vo.setIsAccessible(s.getIsAccessible());
        vo.setSeatStatus(s.getSeatStatus());
        vo.setIsBookable(s.getIsBookable());
        vo.setMapX(s.getMapX() != null ? s.getMapX().doubleValue() : null);
        vo.setMapY(s.getMapY() != null ? s.getMapY().doubleValue() : null);
        vo.setMapWidth(s.getMapWidth() != null ? s.getMapWidth().doubleValue() : null);
        vo.setMapHeight(s.getMapHeight() != null ? s.getMapHeight().doubleValue() : null);
        vo.setMapRotation(s.getMapRotation() != null ? s.getMapRotation().doubleValue() : null);
        return vo;
    }
}
