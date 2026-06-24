package com.nookit.modules.admin.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预约记录分页查询参数")
public class BookingPageQuery {

    private int page = 1;
    private int pageSize = 10;

    @Schema(description = "关键词（学生姓名/学号/座位）")
    private String search;

    @Schema(description = "状态过滤")
    private String status;

    @Schema(description = "日期，格式 yyyy-MM-dd")
    private String date;

    @Schema(description = "标签：today / week / all，默认 all")
    private String tab = "all";
}
