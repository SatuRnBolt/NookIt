package com.nookit.modules.admin.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "新增/编辑公告请求体")
public class NoticeCreateReq {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "正文")
    private String content;

    @Schema(description = "类型：system / rule / event / maintenance")
    private String type = "system";

    @Schema(description = "是否置顶")
    private Boolean pinned = false;

    @Schema(description = "状态：draft / published")
    private String status = "draft";
}
