package com.nookit.modules.admin.notice.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.notice.dto.NoticeCreateReq;
import com.nookit.modules.admin.notice.dto.NoticeVO;
import com.nookit.security.UserPrincipal;

public interface NoticeService {

    PageResult<NoticeVO> pageNotices(int page, int pageSize, String search, String type);

    NoticeVO getById(Long id);

    NoticeVO createNotice(NoticeCreateReq req, UserPrincipal operator);

    void updateNotice(Long id, NoticeCreateReq req);

    void deleteNotice(Long id);

    void publishNotice(Long id);
}
