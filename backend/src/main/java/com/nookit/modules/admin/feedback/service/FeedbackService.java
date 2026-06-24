package com.nookit.modules.admin.feedback.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.feedback.dto.FeedbackVO;
import com.nookit.security.UserPrincipal;

public interface FeedbackService {

    PageResult<FeedbackVO> pageFeedbacks(int page, int pageSize, String search, String type, String status);

    FeedbackVO getById(Long id);

    void updateStatus(Long id, String status);

    void reply(Long id, String content, UserPrincipal operator);
}
