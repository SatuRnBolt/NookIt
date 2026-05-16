package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新建座位地图草稿请求体")
public class SeatMapCreateReq {

    @NotNull(message = "地图宽度不能为空")
    @Schema(description = "地图宽度（px）")
    private Integer mapWidth;

    @NotNull(message = "地图高度不能为空")
    @Schema(description = "地图高度（px）")
    private Integer mapHeight;

    @Schema(description = "背景图 URL")
    private String backgroundUrl;
}
