package com.nookit.modules.admin.violation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "违约统计摘要")
public class ViolationStatsVO {

    @Schema(description = "总违约记录数")
    private long total;

    @Schema(description = "涉及学生数")
    private long uniqueStudents;

    @Schema(description = "已封号学生数")
    private long suspendedStudents;

    @Schema(description = "本月新增违约数")
    private long newThisMonth;
}
