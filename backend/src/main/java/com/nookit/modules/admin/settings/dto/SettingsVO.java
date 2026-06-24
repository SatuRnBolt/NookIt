package com.nookit.modules.admin.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统参数配置 VO")
public class SettingsVO {

    @Schema(description = "单次最大预约时长（小时）")
    private Integer maxBookingHours;

    @Schema(description = "签到窗口（预约开始前 N 分钟）")
    private Integer checkInWindowBefore;

    @Schema(description = "签到窗口（预约开始后 N 分钟）")
    private Integer checkInWindowAfter;

    @Schema(description = "预约前 N 分钟提醒")
    private Integer reminderMinutesBefore;

    @Schema(description = "触发封号的违约次数阈值")
    private Integer violationLimit;

    @Schema(description = "封号持续天数")
    private Integer suspendDays;

    @Schema(description = "是否允许周末预约")
    private Boolean allowWeekend;
}
