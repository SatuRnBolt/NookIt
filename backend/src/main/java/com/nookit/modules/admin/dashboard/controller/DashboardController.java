package com.nookit.modules.admin.dashboard.controller;

import com.nookit.common.api.Result;
import com.nookit.modules.admin.dashboard.mapper.DashboardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin-Dashboard", description = "数据看板")
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    @GetMapping("/summary")
    @Operation(summary = "看板核心指标")
    public Result<Map<String, Object>> summary() {
        return Result.success(dashboardMapper.querySummary());
    }

    @GetMapping("/weekly-trend")
    @Operation(summary = "本周 7 天预约量趋势")
    public Result<List<Map<String, Object>>> weeklyTrend() {
        return Result.success(dashboardMapper.queryWeeklyTrend());
    }

    @GetMapping("/room-occupancy")
    @Operation(summary = "各自习室占用率排名")
    public Result<List<Map<String, Object>>> roomOccupancy(
            @RequestParam(defaultValue = "4") int top) {
        return Result.success(dashboardMapper.queryRoomOccupancy(top));
    }

    @GetMapping("/heatmap")
    @Operation(summary = "近 7 天每小时预约热力图")
    public Result<List<Map<String, Object>>> heatmap() {
        return Result.success(dashboardMapper.queryHeatmapData());
    }

    @GetMapping("/todos")
    @Operation(summary = "待办中心统计")
    public Result<Map<String, Object>> todos() {
        return Result.success(dashboardMapper.queryTodos());
    }
}
