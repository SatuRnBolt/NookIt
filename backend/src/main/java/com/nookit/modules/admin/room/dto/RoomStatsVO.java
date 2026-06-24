package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "自习室概览统计")
public class RoomStatsVO {

    @Schema(description = "总数量")
    private long total;

    @Schema(description = "启用中数量")
    private long active;

    @Schema(description = "停用数量")
    private long inactive;

    @Schema(description = "总座位数")
    private long totalCapacity;
}
