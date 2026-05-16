package com.nookit.modules.admin.user.controller;

import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.user.dto.*;
import com.nookit.modules.admin.user.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin-User", description = "用户管理")
public class UserAdminController {

    private final UserAdminService userAdminService;

    // ---- 9.1 学生用户 ----

    @GetMapping("/students")
    @Operation(summary = "学生列表")
    public Result<PageResult<StudentUserVO>> pageStudents(UserPageQuery query) {
        return Result.success(userAdminService.pageStudents(query));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "学生详情")
    public Result<StudentUserVO> getStudentDetail(@PathVariable Long id) {
        return Result.success(userAdminService.getStudentDetail(id));
    }

    @PatchMapping("/students/{id}/status")
    @Operation(summary = "封号/解封学生")
    public Result<Void> updateStudentStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        userAdminService.updateStudentStatus(id, body.get("status"));
        return Result.success();
    }

    // ---- 9.2 管理员账号 ----

    @GetMapping("/admins")
    @Operation(summary = "管理员列表")
    public Result<PageResult<AdminUserVO>> pageAdmins(UserPageQuery query) {
        return Result.success(userAdminService.pageAdmins(query));
    }

    @PostMapping("/admins")
    @Operation(summary = "新增管理员")
    public Result<Void> createAdmin(@Valid @RequestBody AdminCreateReq req) {
        userAdminService.createAdmin(req);
        return Result.success();
    }

    @PutMapping("/admins/{id}")
    @Operation(summary = "编辑管理员")
    public Result<Void> updateAdmin(@PathVariable Long id,
                                    @Valid @RequestBody AdminCreateReq req) {
        userAdminService.updateAdmin(id, req);
        return Result.success();
    }

    @PatchMapping("/admins/{id}/role")
    @Operation(summary = "分配角色")
    public Result<Void> assignRole(@PathVariable Long id,
                                   @RequestBody Map<String, Long> body) {
        userAdminService.assignRole(id, body.get("roleId"));
        return Result.success();
    }

    @DeleteMapping("/admins/{id}")
    @Operation(summary = "删除管理员")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        userAdminService.deleteAdmin(id);
        return Result.success();
    }
}
