package com.nookit.modules.admin.feedback.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.feedback.UserFeedback;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.feedback.dto.FeedbackVO;
import com.nookit.modules.admin.feedback.mapper.FeedbackMapper;
import com.nookit.modules.admin.feedback.service.FeedbackService;
import com.nookit.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public PageResult<FeedbackVO> pageFeedbacks(int page, int pageSize, String search, String type, String status) {
        Page<FeedbackVO> pg = new Page<>(page, pageSize);
        feedbackMapper.pageFeedbacks(pg, search, type, status);
        return PageResult.from(pg);
    }

    @Override
    public FeedbackVO getById(Long id) {
        FeedbackVO vo = feedbackMapper.findById(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        return vo;
    }

    @Override
    public void updateStatus(Long id, String status) {
        UserFeedback fb = feedbackMapper.selectById(id);
        if (fb == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        UserFeedback upd = new UserFeedback();
        upd.setId(id);
        upd.setFeedbackStatus(status);
        feedbackMapper.updateById(upd);
    }

    @Override
    public void reply(Long id, String content, UserPrincipal operator) {
        UserFeedback fb = feedbackMapper.selectById(id);
        if (fb == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        fb.setReplyContent(content);
        fb.setRepliedBy(operator.getUserId());
        fb.setRepliedAt(LocalDateTime.now());
        fb.setFeedbackStatus("resolved");
        feedbackMapper.updateById(fb);
    }
}
