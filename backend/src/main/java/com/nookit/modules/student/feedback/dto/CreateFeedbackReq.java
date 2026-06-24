package com.nookit.modules.student.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "学生提交反馈请求")
public class CreateFeedbackReq {

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型：bug / suggestion / complaint")
    private String type;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "详细描述")
    private String content;
}
