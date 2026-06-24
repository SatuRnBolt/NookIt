package com.nookit.modules.admin.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "全局座位列表 VO")
public class SeatAdminVO {

    private Long id;

    @Schema(description = "座位编号")
    private String seatNo;

    @Schema(description = "所属自习室名称")
    private String roomName;

    @Schema(description = "是否有电源")
    private Boolean hasPower;

    @Schema(description = "是否靠窗")
    private Boolean nearWindow;

    @Schema(description = "座位状态")
    private String status;

    private LocalDateTime createdAt;
}
