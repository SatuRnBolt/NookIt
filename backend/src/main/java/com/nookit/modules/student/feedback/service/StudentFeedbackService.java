package com.nookit.modules.student.feedback.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.student.feedback.dto.CreateFeedbackReq;

import java.util.Map;

public interface StudentFeedbackService {

    PageResult<Map<String, Object>> listMyFeedbacks(Long userId, int page, int pageSize);

    Map<String, Object> createFeedback(Long userId, CreateFeedbackReq req);
}
