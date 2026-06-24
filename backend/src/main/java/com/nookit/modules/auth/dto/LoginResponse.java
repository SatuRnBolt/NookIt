package com.nookit.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应体")
public class LoginResponse {

    @Schema(description = "JWT Access Token")
    private String token;

    @Schema(description = "登录用户信息")
    private UserInfoVO user;
}
