package com.nookit.modules.admin.violation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.org.User;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.violation.dto.ViolationStatsVO;
import com.nookit.modules.admin.violation.dto.ViolationVO;
import com.nookit.modules.admin.violation.mapper.ViolationMapper;
import com.nookit.modules.admin.violation.service.ViolationService;
import com.nookit.modules.admin.user.mapper.UserAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private final ViolationMapper violationMapper;
    private final UserAdminMapper userAdminMapper;

    @Override
    public PageResult<ViolationVO> pageViolations(int page, int pageSize, String search) {
        Page<ViolationVO> pg = new Page<>(page, pageSize);
        violationMapper.pageViolations(pg, search);
        return PageResult.from(pg);
    }

    @Override
    public ViolationStatsVO getStats() {
        return violationMapper.queryStats();
    }

    @Override
    public Map<String, Object> getStudentViolations(Long studentId) {
        List<ViolationVO> records = violationMapper.findByStudentId(studentId);
        int count = violationMapper.countByStudentId(studentId);
        return Map.of("records", records, "totalCount", count);
    }

    @Override
    public void suspendStudent(Long studentId, String reason, Integer days) {
        User user = userAdminMapper.selectById(studentId);
        if (user == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        User upd = new User();
        upd.setId(studentId);
        upd.setAccountStatus("suspended");
        userAdminMapper.updateById(upd);
    }

    @Override
    public byte[] exportExcel(String search) {
        // Hutool ExcelWriter 导出（简化版，返回 CSV 字节）
        List<ViolationVO> all = violationMapper.pageViolations(new Page<>(-1, -1), search).getRecords();
        StringBuilder sb = new StringBuilder("ID,学生姓名,学号,自习室,座位,日期,时间,原因,累计次数\n");
        for (ViolationVO v : all) {
            sb.append(String.join(",",
                    String.valueOf(v.getId()),
                    v.getStudentName(),
                    v.getStudentId(),
                    v.getRoomName(),
                    v.getSeatNo(),
                    v.getDate(),
                    v.getTime(),
                    v.getReason() != null ? v.getReason() : "",
                    String.valueOf(v.getCount())
            )).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
