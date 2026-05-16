package com.nookit.modules.student.reservation.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.student.reservation.dto.CreateReservationReq;
import com.nookit.modules.student.reservation.service.StudentReservationService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student-Reservation", description = "学生端预约管理")
public class StudentReservationController {

    private final StudentReservationService studentReservationService;

    @GetMapping("/reservations")
    @Operation(summary = "我的预约列表")
    public Result<PageResult<Map<String, Object>>> listMine(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(studentReservationService.listMyReservations(user.getUserId(), page, pageSize, status));
    }

    @PostMapping("/reservations")
    @Operation(summary = "创建预约")
    public Result<Map<String, Object>> create(@CurrentUser UserPrincipal user,
                                              @Valid @RequestBody CreateReservationReq req) {
        return Result.success(studentReservationService.createReservation(user.getUserId(), req));
    }

    @PostMapping("/reservations/{id}/cancel")
    @Operation(summary = "取消预约")
    public Result<Void> cancel(@CurrentUser UserPrincipal user,
                               @PathVariable Long id) {
        studentReservationService.cancelReservation(user.getUserId(), id);
        return Result.success();
    }
}
