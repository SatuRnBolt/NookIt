package com.nookit.modules.admin.role.controller;

import com.nookit.common.api.Result;
import com.nookit.modules.admin.role.dto.RoleCreateReq;
import com.nookit.modules.admin.role.dto.RoleVO;
import com.nookit.modules.admin.role.service.RoleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin-Role", description = "角色与权限管理")
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    @GetMapping("/roles")
    @Operation(summary = "角色列表")
    public Result<List<RoleVO>> listRoles() {
        return Result.success(roleAdminService.listRoles());
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "角色详情+权限")
    public Result<RoleVO> getRoleDetail(@PathVariable Long id) {
        return Result.success(roleAdminService.getRoleDetail(id));
    }

    @PostMapping("/roles")
    @Operation(summary = "新增角色")
    public Result<Void> createRole(@Valid @RequestBody RoleCreateReq req) {
        roleAdminService.createRole(req);
        return Result.success();
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "编辑角色基本信息")
    public Result<Void> updateRole(@PathVariable Long id,
                                   @Valid @RequestBody RoleCreateReq req) {
        roleAdminService.updateRole(id, req);
        return Result.success();
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleAdminService.deleteRole(id);
        return Result.success();
    }

    @PutMapping("/roles/{id}/permissions")
    @Operation(summary = "保存角色权限配置（全量覆盖）")
    public Result<Void> savePermissions(@PathVariable Long id,
                                        @RequestBody Map<String, List<String>> body) {
        roleAdminService.savePermissions(id, body.get("permissions"));
        return Result.success();
    }

    @GetMapping("/permissions")
    @Operation(summary = "所有可用权限列表")
    public Result<List<Map<String, Object>>> listAllPermissions() {
        return Result.success(roleAdminService.listAllPermissions());
    }
}
