package com.nookit.modules.admin.settings.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.settings.dto.SettingsVO;
import com.nookit.modules.admin.settings.service.SettingsService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin-Settings", description = "系统参数设置")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "获取系统参数")
    public Result<SettingsVO> getSettings() {
        return Result.success(settingsService.getSettings());
    }

    @PutMapping
    @Operation(summary = "批量更新系统参数")
    public Result<Void> updateSettings(@RequestBody SettingsVO req,
                                       @CurrentUser UserPrincipal operator) {
        settingsService.updateSettings(req, operator);
        return Result.success();
    }

    @GetMapping("/logs")
    @Operation(summary = "参数修改历史")
    public Result<PageResult<Map<String, Object>>> getSettingsLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(settingsService.getSettingsLogs(page, pageSize));
    }
}
