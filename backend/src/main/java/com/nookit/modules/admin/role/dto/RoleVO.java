package com.nookit.modules.admin.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色 VO")
public class RoleVO {

    private Long id;
    private String name;
    private String description;
    private List<String> permissions;
    private long userCount;
}
