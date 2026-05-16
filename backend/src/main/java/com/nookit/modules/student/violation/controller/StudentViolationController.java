package com.nookit.modules.student.violation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.Result;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.domain.seat.Seat;
import com.nookit.common.domain.space.StudyRoom;
import com.nookit.common.domain.violation.Violation;
import com.nookit.common.domain.violation.ViolationAppeal;
import com.nookit.common.exception.BusinessException;
import com.nookit.modules.admin.booking.mapper.BookingMapper;
import com.nookit.modules.admin.room.mapper.RoomMapper;
import com.nookit.modules.admin.room.mapper.SeatMapper;
import com.nookit.modules.admin.violation.mapper.ViolationMapper;
import com.nookit.modules.student.violation.dto.StudentViolationVO;
import com.nookit.modules.student.violation.mapper.ViolationAppealMapper;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student-Violation", description = "学生端违约记录")
public class StudentViolationController {

    private final ViolationMapper violationMapper;
    private final ViolationAppealMapper appealMapper;
    private final BookingMapper bookingMapper;
    private final RoomMapper roomMapper;
    private final SeatMapper seatMapper;

    @GetMapping("/violations")
    @Operation(summary = "我的违约记录")
    public Result<Map<String, Object>> listMine(@CurrentUser UserPrincipal user) {
        List<Violation> violations = violationMapper.selectList(
                new LambdaQueryWrapper<Violation>()
                        .eq(Violation::getUserId, user.getUserId())
                        .orderByDesc(Violation::getCreatedAt)
        );

        // collect reservation ids for batch lookup
        Set<Long> reservationIds = violations.stream()
                .map(Violation::getReservationId).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Reservation> resMap = reservationIds.isEmpty() ? Map.of() :
                bookingMapper.selectBatchIds(reservationIds).stream()
                        .collect(Collectors.toMap(Reservation::getId, r -> r));

        Set<Long> roomIds = resMap.values().stream()
                .map(Reservation::getStudyRoomId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> seatIds = resMap.values().stream()
                .map(Reservation::getSeatId).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, StudyRoom> roomMap = roomIds.isEmpty() ? Map.of() :
                roomMapper.selectBatchIds(roomIds).stream()
                        .collect(Collectors.toMap(StudyRoom::getId, r -> r));
        Map<Long, Seat> seatMap = seatIds.isEmpty() ? Map.of() :
                seatMapper.selectBatchIds(seatIds).stream()
                        .collect(Collectors.toMap(Seat::getId, s -> s));

        List<StudentViolationVO> records = violations.stream().map(v -> {
            StudentViolationVO vo = new StudentViolationVO();
            vo.setId(v.getId());
            vo.setViolationType(v.getViolationType());
            vo.setViolationStatus(v.getViolationStatus());
            vo.setDescriptionText(v.getDescriptionText());
            vo.setOccurredAt(v.getOccurredAt());
            vo.setCreatedAt(v.getCreatedAt());
            Reservation res = resMap.get(v.getReservationId());
            if (res != null) {
                StudyRoom room = roomMap.get(res.getStudyRoomId());
                vo.setRoomName(room != null ? room.getRoomName() : null);
                Seat seat = seatMap.get(res.getSeatId());
                vo.setSeatCode(seat != null ? seat.getSeatCode() : null);
            }
            return vo;
        }).collect(Collectors.toList());

        long activeCount = violations.stream()
                .filter(v -> "active".equals(v.getViolationStatus())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", activeCount);
        stats.put("accountStatus", "active");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("stats", stats);
        return Result.success(result);
    }

    @PostMapping("/violations/{id}/appeal")
    @Operation(summary = "提交违约申诉")
    public Result<Void> appeal(@CurrentUser UserPrincipal user,
                               @PathVariable Long id,
                               @RequestBody Map<String, String> body) {
        Violation v = violationMapper.selectById(id);
        if (v == null || !v.getUserId().equals(user.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "违约记录不存在");
        }
        ViolationAppeal appeal = new ViolationAppeal();
        appeal.setViolationId(id);
        appeal.setUserId(user.getUserId());
        appeal.setAppealStatus("pending");
        appeal.setAppealReason(body.get("reason"));
        appeal.setSubmittedAt(LocalDateTime.now());
        appealMapper.insert(appeal);

        v.setViolationStatus("pending_appeal");
        violationMapper.updateById(v);
        return Result.success();
    }
}
