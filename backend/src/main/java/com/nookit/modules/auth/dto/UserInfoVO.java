package com.nookit.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当前登录用户信息")
public class UserInfoVO {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户类型：student / staff")
    private String userType;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;
}
