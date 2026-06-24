package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "地图内座位新增/编辑请求体")
public class SeatInMapReq {

    @NotBlank(message = "座位编号不能为空")
    @Schema(description = "座位编号")
    private String seatCode;

    @NotBlank(message = "展示标签不能为空")
    @Schema(description = "展示标签")
    private String displayLabel;

    @Schema(description = "座位类型，默认 standard")
    private String seatType = "standard";

    @Schema(description = "行号")
    private Integer rowNo;

    @Schema(description = "列号")
    private Integer colNo;

    @Schema(description = "是否有电源")
    private Boolean hasPower = false;

    @Schema(description = "是否靠窗")
    private Boolean isWindowSide = false;

    @Schema(description = "是否无障碍")
    private Boolean isAccessible = false;

    @Schema(description = "座位状态：active / inactive / maintenance")
    private String seatStatus = "active";

    @Schema(description = "是否可预约")
    private Boolean isBookable = true;

    @Schema(description = "地图 X 坐标")
    private Double mapX;

    @Schema(description = "地图 Y 坐标")
    private Double mapY;

    @Schema(description = "地图宽度")
    private Double mapWidth;

    @Schema(description = "地图高度")
    private Double mapHeight;

    @Schema(description = "旋转角度")
    private Double mapRotation = 0.0;
}
