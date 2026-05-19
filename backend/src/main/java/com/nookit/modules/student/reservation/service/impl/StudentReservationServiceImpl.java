package com.nookit.modules.student.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.domain.seat.Seat;
import com.nookit.common.domain.space.StudyRoom;
import com.nookit.common.exception.BusinessException;
import com.nookit.modules.admin.booking.mapper.BookingMapper;
import com.nookit.modules.admin.room.mapper.RoomMapper;
import com.nookit.modules.admin.room.mapper.SeatMapper;
import com.nookit.modules.student.reservation.dto.CreateReservationReq;
import com.nookit.modules.student.reservation.service.StudentReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentReservationServiceImpl implements StudentReservationService {

    private final BookingMapper bookingMapper;
    private final SeatMapper seatMapper;
    private final RoomMapper roomMapper;

    @Override
    public PageResult<Map<String, Object>> listMyReservations(Long userId, int page, int pageSize, String status) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(status != null && !status.isEmpty(), Reservation::getReservationStatus, status)
                .orderByDesc(Reservation::getCreatedAt);

        Page<Reservation> pg = bookingMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // collect room/seat ids for batch lookup
        Set<Long> roomIds = pg.getRecords().stream().map(Reservation::getStudyRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> seatIds = pg.getRecords().stream().map(Reservation::getSeatId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, StudyRoom> roomMap = roomIds.isEmpty() ? Map.of() :
                roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(StudyRoom::getId, r -> r));
        Map<Long, Seat> seatMap = seatIds.isEmpty() ? Map.of() :
                seatMapper.selectBatchIds(seatIds).stream().collect(Collectors.toMap(Seat::getId, s -> s));

        List<Map<String, Object>> records = pg.getRecords().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            StudyRoom room = roomMap.get(r.getStudyRoomId());
            m.put("roomName", room != null ? room.getRoomName() : null);
            m.put("room_name", room != null ? room.getRoomName() : null);
            Seat seat = seatMap.get(r.getSeatId());
            m.put("seatCode", seat != null ? seat.getSeatCode() : null);
            m.put("seat_code", seat != null ? seat.getSeatCode() : null);
            m.put("date", r.getReservationDate() != null ? r.getReservationDate().toString() : null);
            m.put("reservation_date", r.getReservationDate() != null ? r.getReservationDate().toString() : null);
            String st = r.getStartAt() != null ? r.getStartAt().toLocalTime().toString().substring(0, 5) : null;
            String et = r.getEndAt()   != null ? r.getEndAt().toLocalTime().toString().substring(0, 5)   : null;
            m.put("startTime",  st); m.put("start_time", st);
            m.put("endTime",    et); m.put("end_time",   et);
            m.put("status", r.getReservationStatus());
            m.put("reservation_status", r.getReservationStatus());
            m.put("checkinCode", r.getNotesText());
            m.put("checkin_code", r.getNotesText());
            m.put("code", r.getNotesText());
            return m;
        }).collect(Collectors.toList());

        return PageResult.of(records, pg.getTotal(), pg.getCurrent(), pg.getSize());
    }

    @Override
    @Transactional
    public Map<String, Object> createReservation(Long userId, CreateReservationReq req) {
        Seat seat = seatMapper.selectById(req.getSeatId());
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        if (!Boolean.TRUE.equals(seat.getIsBookable())) {
            throw new BusinessException(ResultCode.SEAT_UNAVAILABLE);
        }

        // 解析 HH:mm 时间字符串
        java.time.LocalTime startLocalTime = java.time.LocalTime.parse(req.getStartTime());
        java.time.LocalTime endLocalTime   = java.time.LocalTime.parse(req.getEndTime());
        if (!endLocalTime.isAfter(startLocalTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        long durationMinutes = java.time.Duration.between(startLocalTime, endLocalTime).toMinutes();
        if (durationMinutes > 240) {
            throw new BusinessException(ResultCode.RESERVATION_OUT_OF_RANGE);
        }

        LocalDate date = LocalDate.parse(req.getDate());
        LocalDateTime reqStart = date.atTime(startLocalTime);
        LocalDateTime reqEnd   = date.atTime(endLocalTime);

        // 用 startAt/endAt 做精确冲突检测，支持半小时粒度
        List<Reservation> conflicts = bookingMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getSeatId, req.getSeatId())
                        .eq(Reservation::getReservationDate, date)
                        .notIn(Reservation::getReservationStatus, "cancelled")
        );
        for (Reservation existing : conflicts) {
            LocalDateTime exStart = existing.getStartAt();
            LocalDateTime exEnd   = existing.getEndAt();
            if (exStart == null || exEnd == null) continue;
            if (reqStart.isBefore(exEnd) && reqEnd.isAfter(exStart)) {
                throw new BusinessException(ResultCode.RESERVATION_TIME_CONFLICT);
            }
        }

        Reservation r = new Reservation();
        r.setReservationNo("RSV" + System.currentTimeMillis());
        r.setUserId(userId);
        r.setCreatedByUserId(userId);
        r.setStudyRoomId(seat.getStudyRoomId());
        r.setSeatId(req.getSeatId());
        r.setReservationDate(date);
        r.setStartAt(reqStart);
        r.setEndAt(reqEnd);
        r.setReservationStatus("pending_checkin");
        r.setSourceChannel("web");
        r.setNotesText(String.format("%04d", new Random().nextInt(10000)));
        r.setCheckinDeadlineAt(reqStart.plusMinutes(15));
        bookingMapper.insert(r);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", r.getId());
        result.put("roomName", roomMapper.selectById(seat.getStudyRoomId()).getRoomName());
        result.put("seatCode", seat.getSeatCode());
        result.put("status", r.getReservationStatus());
        result.put("checkinCode", r.getNotesText());
        return result;
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation r = bookingMapper.selectById(reservationId);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if ("cancelled".equals(r.getReservationStatus())) {
            throw new BusinessException(ResultCode.RESOURCE_CONFLICT, "预约已取消");
        }
        r.setReservationStatus("cancelled");
        r.setCancelledAt(LocalDateTime.now());
        bookingMapper.updateById(r);
    }
}
