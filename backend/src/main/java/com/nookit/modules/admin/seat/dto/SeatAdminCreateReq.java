package com.nookit.modules.admin.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "全局座位新增/编辑请求体")
public class SeatAdminCreateReq {

    @NotNull(message = "自习室 ID 不能为空")
    @Schema(description = "所属自习室 ID")
    private Long roomId;

    @NotBlank(message = "座位编号不能为空")
    @Schema(description = "座位编号")
    private String seatCode;

    @Schema(description = "是否有电源")
    private Boolean hasPower = false;

    @Schema(description = "是否靠窗")
    private Boolean isWindowSide = false;
}
