package com.nookit.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求体")
public class LoginRequest {

    @NotBlank(message = "登录账号不能为空")
    @Schema(description = "登录账号（邮箱 / 学号 / 工号）")
    private String identity;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
