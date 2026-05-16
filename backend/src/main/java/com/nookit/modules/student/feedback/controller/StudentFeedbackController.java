package com.nookit.modules.student.feedback.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.student.feedback.dto.CreateFeedbackReq;
import com.nookit.modules.student.feedback.service.StudentFeedbackService;
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
@Tag(name = "Student-Feedback", description = "学生端问题反馈")
public class StudentFeedbackController {

    private final StudentFeedbackService studentFeedbackService;

    @GetMapping("/feedbacks")
    @Operation(summary = "我的反馈列表")
    public Result<PageResult<Map<String, Object>>> listMine(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(studentFeedbackService.listMyFeedbacks(user.getUserId(), page, pageSize));
    }

    @PostMapping("/feedbacks")
    @Operation(summary = "提交反馈")
    public Result<Map<String, Object>> create(@CurrentUser UserPrincipal user,
                                              @Valid @RequestBody CreateFeedbackReq req) {
        return Result.success(studentFeedbackService.createFeedback(user.getUserId(), req));
    }
}
