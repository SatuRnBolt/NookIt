package com.nookit.modules.admin.feedback.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.feedback.dto.FeedbackVO;
import com.nookit.modules.admin.feedback.service.FeedbackService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Admin-Feedback", description = "问题反馈管理")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    @Operation(summary = "反馈列表")
    public Result<PageResult<FeedbackVO>> pageFeedbacks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.success(feedbackService.pageFeedbacks(page, pageSize, search, type, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "反馈详情")
    public Result<FeedbackVO> getById(@PathVariable Long id) {
        return Result.success(feedbackService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新处理状态")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        feedbackService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "提交管理员回复")
    public Result<Void> reply(@PathVariable Long id,
                              @RequestBody Map<String, String> body,
                              @CurrentUser UserPrincipal operator) {
        feedbackService.reply(id, body.get("content"), operator);
        return Result.success();
    }
}
