package com.nookit.modules.admin.notice.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.notice.AdminNotice;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.notice.dto.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeMapper extends BaseMapperX<AdminNotice> {

    IPage<NoticeVO> pageNotices(Page<NoticeVO> page,
                                @Param("search") String search,
                                @Param("type") String type);

    NoticeVO findById(@Param("id") Long id);
}
