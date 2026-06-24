package com.nookit.modules.student.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.domain.feedback.UserFeedback;
import com.nookit.common.domain.org.User;
import com.nookit.modules.admin.feedback.mapper.FeedbackMapper;
import com.nookit.modules.admin.user.mapper.UserAdminMapper;
import com.nookit.modules.student.feedback.dto.CreateFeedbackReq;
import com.nookit.modules.student.feedback.service.StudentFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentFeedbackServiceImpl implements StudentFeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final UserAdminMapper userAdminMapper;

    @Override
    public PageResult<Map<String, Object>> listMyFeedbacks(Long userId, int page, int pageSize) {
        Page<UserFeedback> pg = feedbackMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFeedback>()
                        .eq(UserFeedback::getUserId, userId)
                        .orderByDesc(UserFeedback::getCreatedAt)
        );

        List<Map<String, Object>> records = pg.getRecords().stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("title", f.getTitle());
            m.put("content", f.getContent());
            m.put("feedback_type", f.getFeedbackType());
            m.put("type", f.getFeedbackType());
            m.put("feedback_status", f.getFeedbackStatus());
            m.put("status", f.getFeedbackStatus());
            m.put("reply_content", f.getReplyContent());
            m.put("replyContent", f.getReplyContent());
            m.put("repliedAt", f.getRepliedAt() != null ? f.getRepliedAt().toString() : null);
            m.put("created_at", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
            m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());

        return PageResult.of(records, pg.getTotal(), pg.getCurrent(), pg.getSize());
    }

    @Override
    @Transactional
    public Map<String, Object> createFeedback(Long userId, CreateFeedbackReq req) {
        User user = userAdminMapper.selectById(userId);

        UserFeedback fb = new UserFeedback();
        fb.setUserId(userId);
        fb.setStudentName(user != null ? user.getFullName() : null);
        fb.setStudentNo(user != null ? user.getStudentNo() : null);
        fb.setFeedbackType(req.getType());
        fb.setTitle(req.getTitle());
        fb.setContent(req.getContent());
        fb.setFeedbackStatus("pending");
        feedbackMapper.insert(fb);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", fb.getId());
        result.put("title", fb.getTitle());
        result.put("status", "pending");
        return result;
    }
}
