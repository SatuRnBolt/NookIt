package com.nookit.modules.admin.feedback.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.feedback.UserFeedback;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.feedback.dto.FeedbackVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FeedbackMapper extends BaseMapperX<UserFeedback> {

    IPage<FeedbackVO> pageFeedbacks(Page<FeedbackVO> page,
                                    @Param("search") String search,
                                    @Param("type") String type,
                                    @Param("status") String status);

    FeedbackVO findById(@Param("id") Long id);
}
