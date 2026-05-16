package com.nookit.modules.admin.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "全局座位分页查询参数")
public class SeatPageQuery {

    private int page = 1;
    private int pageSize = 10;

    @Schema(description = "关键词（座位编号）")
    private String search;

    @Schema(description = "自习室 ID")
    private Long roomId;

    @Schema(description = "座位状态：active / inactive / maintenance")
    private String status;

    @Schema(description = "是否有电源：true / false")
    private Boolean hasPower;
}
