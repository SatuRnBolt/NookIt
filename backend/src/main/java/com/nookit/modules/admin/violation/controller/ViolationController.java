package com.nookit.modules.admin.violation.controller;

import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.violation.dto.ViolationStatsVO;
import com.nookit.modules.admin.violation.dto.ViolationVO;
import com.nookit.modules.admin.violation.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/violations")
@RequiredArgsConstructor
@Tag(name = "Admin-Violation", description = "违约记录管理")
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    @Operation(summary = "违约记录列表")
    public Result<PageResult<ViolationVO>> pageViolations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        return Result.success(violationService.pageViolations(page, pageSize, search));
    }

    @GetMapping("/stats")
    @Operation(summary = "违约统计摘要")
    public Result<ViolationStatsVO> getStats() {
        return Result.success(violationService.getStats());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "某学生全部违约记录")
    public Result<Map<String, Object>> getStudentViolations(@PathVariable Long studentId) {
        return Result.success(violationService.getStudentViolations(studentId));
    }

    @PostMapping("/student/{studentId}/suspend")
    @Operation(summary = "封禁学生账号")
    public Result<Void> suspendStudent(@PathVariable Long studentId,
                                       @RequestBody(required = false) Map<String, Object> body) {
        String reason = body != null ? (String) body.get("reason") : null;
        Integer days = body != null && body.get("days") != null
                ? ((Number) body.get("days")).intValue() : null;
        violationService.suspendStudent(studentId, reason, days);
        return Result.success();
    }

    @GetMapping("/export")
    @Operation(summary = "导出违约记录")
    public ResponseEntity<byte[]> exportViolations(@RequestParam(required = false) String search) {
        byte[] data = violationService.exportExcel(search);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=violations.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(data);
    }
}
