package com.nookit.modules.admin.violation.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.violation.Violation;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.violation.dto.ViolationStatsVO;
import com.nookit.modules.admin.violation.dto.ViolationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ViolationMapper extends BaseMapperX<Violation> {

    IPage<ViolationVO> pageViolations(Page<ViolationVO> page, @Param("search") String search);

    ViolationStatsVO queryStats();

    List<ViolationVO> findByStudentId(@Param("userId") Long userId);

    int countByStudentId(@Param("userId") Long userId);
}
