package com.nookit.modules.admin.seat.controller;

import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.seat.dto.SeatAdminCreateReq;
import com.nookit.modules.admin.seat.dto.SeatAdminVO;
import com.nookit.modules.admin.seat.dto.SeatPageQuery;
import com.nookit.modules.admin.seat.service.SeatAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/seats")
@RequiredArgsConstructor
@Tag(name = "Admin-Seat", description = "座位管理（全局视图）")
public class SeatAdminController {

    private final SeatAdminService seatAdminService;

    @GetMapping
    @Operation(summary = "全局座位列表")
    public Result<PageResult<SeatAdminVO>> pageSeats(SeatPageQuery query) {
        return Result.success(seatAdminService.pageSeats(query));
    }

    @PostMapping
    @Operation(summary = "新增座位")
    public Result<SeatAdminVO> createSeat(@Valid @RequestBody SeatAdminCreateReq req) {
        return Result.success(seatAdminService.createSeat(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑座位")
    public Result<Void> updateSeat(@PathVariable Long id,
                                   @Valid @RequestBody SeatAdminCreateReq req) {
        seatAdminService.updateSeat(id, req);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "切换座位状态")
    public Result<Void> updateSeatStatus(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        seatAdminService.updateSeatStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "注销座位")
    public Result<Void> deleteSeat(@PathVariable Long id) {
        seatAdminService.deleteSeat(id);
        return Result.success();
    }
}
