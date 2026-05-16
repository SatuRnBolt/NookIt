package com.nookit.modules.admin.violation.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.violation.dto.ViolationStatsVO;
import com.nookit.modules.admin.violation.dto.ViolationVO;

import java.util.List;
import java.util.Map;

public interface ViolationService {

    PageResult<ViolationVO> pageViolations(int page, int pageSize, String search);

    ViolationStatsVO getStats();

    Map<String, Object> getStudentViolations(Long studentId);

    void suspendStudent(Long studentId, String reason, Integer days);

    byte[] exportExcel(String search);
}
