package com.nookit.modules.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户分页查询参数")
public class UserPageQuery {

    private int page = 1;
    private int pageSize = 10;

    @Schema(description = "关键词（姓名/学号/邮箱）")
    private String search;

    @Schema(description = "账号状态：active / suspended / inactive")
    private String status;
}
