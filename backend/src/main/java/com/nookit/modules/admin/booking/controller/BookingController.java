package com.nookit.modules.admin.booking.controller;

import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.booking.dto.BookingPageQuery;
import com.nookit.modules.admin.booking.dto.BookingStatsVO;
import com.nookit.modules.admin.booking.dto.BookingVO;
import com.nookit.modules.admin.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin-Booking", description = "预约记录管理")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "预约记录列表")
    public Result<PageResult<BookingVO>> pageBookings(BookingPageQuery query) {
        return Result.success(bookingService.pageBookings(query));
    }

    @GetMapping("/stats")
    @Operation(summary = "预约统计摘要")
    public Result<BookingStatsVO> getStats(@RequestParam(defaultValue = "all") String tab) {
        return Result.success(bookingService.getStats(tab));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "管理员取消预约")
    public Result<Void> cancelBooking(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        bookingService.cancelBooking(id, reason);
        return Result.success();
    }

    @PostMapping("/{id}/checkin")
    @Operation(summary = "手动签到")
    public Result<Void> checkinBooking(@PathVariable Long id) {
        bookingService.checkinBooking(id);
        return Result.success();
    }
}
