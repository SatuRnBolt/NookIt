package com.nookit.modules.student.room.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.domain.seat.Seat;
import com.nookit.modules.admin.booking.mapper.BookingMapper;
import com.nookit.modules.admin.room.dto.RoomPageQuery;
import com.nookit.modules.admin.room.dto.RoomVO;
import com.nookit.modules.admin.room.dto.SeatMapVO;
import com.nookit.modules.admin.room.mapper.SeatMapper;
import com.nookit.modules.admin.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student-Room", description = "学生端自习室浏览")
public class StudentRoomController {

    private final RoomService roomService;
    private final SeatMapper seatMapper;
    private final BookingMapper bookingMapper;

    @GetMapping("/rooms")
    @Operation(summary = "自习室列表（仅开放中）")
    public Result<PageResult<RoomVO>> listRooms(RoomPageQuery query) {
        return Result.success(roomService.pageRooms(query));
    }

    @GetMapping("/rooms/{id}")
    @Operation(summary = "自习室详情")
    public Result<RoomVO> getRoom(@PathVariable Long id) {
        return Result.success(roomService.getRoomById(id));
    }

    @GetMapping("/rooms/{roomId}/seatmap")
    @Operation(summary = "座位地图")
    public Result<SeatMapVO> getSeatMap(@PathVariable Long roomId) {
        SeatMapVO map = roomService.getSeatMap(roomId, null);
        if (map == null) {
            return Result.success(null);
        }
        // merge real-time occupied status from today's reservations
        String today = LocalDate.now().toString();
        Set<Long> occupiedSeatIds = bookingMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStudyRoomId, roomId)
                        .eq(Reservation::getReservationDate, today)
                        .notIn(Reservation::getReservationStatus, "cancelled", "completed")
        ).stream().map(Reservation::getSeatId).collect(Collectors.toSet());

        if (map.getSeats() != null) {
            map.getSeats().forEach(s -> {
                if (occupiedSeatIds.contains(s.getId())) {
                    s.setSeatStatus("occupied");
                    s.setIsBookable(false);
                }
            });
        }
        return Result.success(map);
    }

    @GetMapping("/seats/{seatId}/slots")
    @Operation(summary = "查询座位已占用时段")
    public Result<Map<String, Object>> getOccupiedSlots(@PathVariable Long seatId,
                                                        @RequestParam String date) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            return Result.success(Map.of("occupiedSlots", List.of()));
        }

        List<Reservation> reservations = bookingMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getSeatId, seatId)
                        .eq(Reservation::getReservationDate, LocalDate.parse(date))
                        .notIn(Reservation::getReservationStatus, "cancelled")
        );

        Set<Integer> occupied = new TreeSet<>();
        for (Reservation r : reservations) {
            if (r.getStartHour() == null || r.getEndHour() == null) continue;
            for (int h = r.getStartHour(); h < r.getEndHour(); h++) {
                int slot = h - 6; // 7→1, 8→2, ..., 21→15
                if (slot >= 1 && slot <= 15) {
                    occupied.add(slot);
                }
            }
        }

        return Result.success(Map.of(
                "occupiedSlots", new ArrayList<>(occupied),
                "occupied", new ArrayList<>(occupied)
        ));
    }
}
