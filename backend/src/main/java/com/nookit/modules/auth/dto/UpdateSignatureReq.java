package com.nookit.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新个性签名请求")
public class UpdateSignatureReq {

    @Size(max = 60, message = "个性签名长度不能超过60个字符")
    @Schema(description = "个性签名", example = "保持专注，今天也认真学习。")
    private String signature;
}
