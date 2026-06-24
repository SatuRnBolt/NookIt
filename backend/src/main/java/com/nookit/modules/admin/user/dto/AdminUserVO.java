package com.nookit.modules.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员用户 VO")
public class AdminUserVO {

    private Long id;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
