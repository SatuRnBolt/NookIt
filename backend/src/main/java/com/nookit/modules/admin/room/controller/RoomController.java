package com.nookit.modules.admin.room.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.room.dto.*;
import com.nookit.modules.admin.room.service.RoomService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin-Room", description = "自习室管理")
public class RoomController {

    private final RoomService roomService;

    // ---- 3.1 自习室 CRUD ----

    @GetMapping("/rooms")
    @Operation(summary = "自习室列表（分页+筛选）")
    public Result<PageResult<RoomVO>> pageRooms(RoomPageQuery query) {
        return Result.success(roomService.pageRooms(query));
    }

    @PostMapping("/rooms")
    @Operation(summary = "新增自习室")
    public Result<RoomVO> createRoom(@Valid @RequestBody RoomCreateReq req,
                                     @CurrentUser UserPrincipal operator) {
        return Result.success(roomService.createRoom(req, operator));
    }

    @GetMapping("/rooms/{id}")
    @Operation(summary = "自习室详情")
    public Result<RoomVO> getRoomById(@PathVariable Long id) {
        return Result.success(roomService.getRoomById(id));
    }

    @PutMapping("/rooms/{id}")
    @Operation(summary = "编辑自习室")
    public Result<RoomVO> updateRoom(@PathVariable Long id,
                                     @Valid @RequestBody RoomCreateReq req,
                                     @CurrentUser UserPrincipal operator) {
        return Result.success(roomService.updateRoom(id, req, operator));
    }

    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "删除自习室")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.success();
    }

    @PatchMapping("/rooms/{id}/status")
    @Operation(summary = "切换自习室启用/停用状态")
    public Result<Void> updateRoomStatus(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        roomService.updateRoomStatus(id, body.get("room_status"));
        return Result.success();
    }

    // ---- 3.2 统计 ----

    @GetMapping("/rooms/stats")
    @Operation(summary = "自习室概览统计")
    public Result<RoomStatsVO> getStats() {
        return Result.success(roomService.getStats());
    }

    // ---- 3.3 座位地图 ----

    @GetMapping("/rooms/{roomId}/seatmap")
    @Operation(summary = "获取座位地图")
    public Result<SeatMapVO> getSeatMap(@PathVariable Long roomId,
                                        @RequestParam(required = false) Integer version) {
        return Result.success(roomService.getSeatMap(roomId, version));
    }

    @PostMapping("/rooms/{roomId}/seatmap")
    @Operation(summary = "新建地图版本草稿")
    public Result<SeatMapVO> createSeatMapDraft(@PathVariable Long roomId,
                                                 @Valid @RequestBody SeatMapCreateReq req,
                                                 @CurrentUser UserPrincipal operator) {
        return Result.success(roomService.createSeatMapDraft(roomId, req, operator));
    }

    @PutMapping("/rooms/{roomId}/seatmap/{mapId}")
    @Operation(summary = "更新地图配置")
    public Result<Void> updateSeatMap(@PathVariable Long roomId,
                                      @PathVariable Long mapId,
                                      @RequestBody SeatMapCreateReq req) {
        roomService.updateSeatMap(roomId, mapId, req);
        return Result.success();
    }

    @PutMapping("/rooms/{roomId}/seatmap/{mapId}/publish")
    @Operation(summary = "发布地图版本")
    public Result<Void> publishSeatMap(@PathVariable Long roomId,
                                       @PathVariable Long mapId) {
        roomService.publishSeatMap(roomId, mapId);
        return Result.success();
    }

    // ---- 3.4 地图内座位 ----

    @PostMapping("/rooms/{roomId}/seatmap/{mapId}/seats")
    @Operation(summary = "在地图上新增座位")
    public Result<SeatMapVO.SeatVO> addSeat(@PathVariable Long roomId,
                                             @PathVariable Long mapId,
                                             @Valid @RequestBody SeatInMapReq req) {
        return Result.success(roomService.addSeat(roomId, mapId, req));
    }

    @PutMapping("/rooms/{roomId}/seatmap/{mapId}/seats/{seatId}")
    @Operation(summary = "更新座位配置")
    public Result<Void> updateSeat(@PathVariable Long roomId,
                                   @PathVariable Long mapId,
                                   @PathVariable Long seatId,
                                   @Valid @RequestBody SeatInMapReq req) {
        roomService.updateSeat(roomId, mapId, seatId, req);
        return Result.success();
    }

    @DeleteMapping("/rooms/{roomId}/seatmap/{mapId}/seats/{seatId}")
    @Operation(summary = "从地图删除座位")
    public Result<Void> deleteSeat(@PathVariable Long roomId,
                                   @PathVariable Long mapId,
                                   @PathVariable Long seatId) {
        roomService.deleteSeat(roomId, mapId, seatId);
        return Result.success();
    }

    @PostMapping("/rooms/{roomId}/seatmap/{mapId}/seats/{seatId}/duplicate")
    @Operation(summary = "复制座位")
    public Result<SeatMapVO.SeatVO> duplicateSeat(@PathVariable Long roomId,
                                                    @PathVariable Long mapId,
                                                    @PathVariable Long seatId,
                                                    @RequestBody Map<String, Double> body) {
        return Result.success(roomService.duplicateSeat(roomId, mapId, seatId,
                body.get("map_x"), body.get("map_y")));
    }

    // ---- 3.5 下拉选项 ----

    @GetMapping("/dict/campuses")
    @Operation(summary = "校区列表")
    public Result<List<String>> listCampuses() {
        return Result.success(roomService.listCampuses());
    }

    @GetMapping("/dict/organizations")
    @Operation(summary = "机构列表")
    public Result<List<Map<String, Object>>> listOrganizations() {
        return Result.success(roomService.listOrganizations());
    }

    @GetMapping("/dict/room-types")
    @Operation(summary = "自习室类型列表")
    public Result<List<Map<String, Object>>> listRoomTypes() {
        return Result.success(roomService.listRoomTypes());
    }

    @GetMapping("/rooms/simple")
    @Operation(summary = "自习室下拉列表（简化）")
    public Result<List<Map<String, Object>>> listRoomsSimple() {
        return Result.success(roomService.listRoomsSimple());
    }
}
