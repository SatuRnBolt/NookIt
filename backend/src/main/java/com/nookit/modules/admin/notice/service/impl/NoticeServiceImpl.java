package com.nookit.modules.admin.notice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.notice.AdminNotice;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.notice.dto.NoticeCreateReq;
import com.nookit.modules.admin.notice.dto.NoticeVO;
import com.nookit.modules.admin.notice.mapper.NoticeMapper;
import com.nookit.modules.admin.notice.service.NoticeService;
import com.nookit.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public PageResult<NoticeVO> pageNotices(int page, int pageSize, String search, String type) {
        Page<NoticeVO> pg = new Page<>(page, pageSize);
        noticeMapper.pageNotices(pg, search, type);
        return PageResult.from(pg);
    }

    @Override
    public NoticeVO getById(Long id) {
        NoticeVO vo = noticeMapper.findById(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        return vo;
    }

    @Override
    public NoticeVO createNotice(NoticeCreateReq req, UserPrincipal operator) {
        AdminNotice notice = buildNotice(req, operator);
        if ("published".equals(req.getStatus())) {
            notice.setPublishedAt(LocalDateTime.now());
        }
        noticeMapper.insert(notice);
        return noticeMapper.findById(notice.getId());
    }

    @Override
    public void updateNotice(Long id, NoticeCreateReq req) {
        AdminNotice existing = noticeMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        existing.setTitle(req.getTitle());
        existing.setContent(req.getContent());
        existing.setNoticeType(req.getType());
        existing.setIsPinned(req.getPinned());
        existing.setNoticeStatus(req.getStatus());
        if ("published".equals(req.getStatus()) && existing.getPublishedAt() == null) {
            existing.setPublishedAt(LocalDateTime.now());
        }
        noticeMapper.updateById(existing);
    }

    @Override
    public void deleteNotice(Long id) {
        if (noticeMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        noticeMapper.deleteById(id);
    }

    @Override
    public void publishNotice(Long id) {
        AdminNotice notice = noticeMapper.selectById(id);
        if (notice == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        notice.setNoticeStatus("published");
        notice.setPublishedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
    }

    private AdminNotice buildNotice(NoticeCreateReq req, UserPrincipal operator) {
        AdminNotice n = new AdminNotice();
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setNoticeType(req.getType());
        n.setIsPinned(req.getPinned());
        n.setNoticeStatus(req.getStatus());
        n.setAuthorName(operator.getFullName() != null ? operator.getFullName() : operator.getUsername());
        n.setCreatedBy(operator.getUserId());
        n.setUpdatedBy(operator.getUserId());
        return n;
    }
}
