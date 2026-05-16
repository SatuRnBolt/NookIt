package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "自习室分页查询参数")
public class RoomPageQuery {

    @Schema(description = "页码，默认 1")
    private int page = 1;

    @Schema(description = "每页数量，默认 10")
    private int pageSize = 10;

    @Schema(description = "关键词（编号/名称）")
    private String search;

    @Schema(description = "校区")
    private String campus;

    @Schema(description = "可见范围：public / organization / custom")
    private String visibilityScope;
}
