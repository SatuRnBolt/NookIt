package com.nookit.modules.auth.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.Result;
import com.nookit.modules.auth.dto.LoginRequest;
import com.nookit.modules.auth.dto.LoginResponse;
import com.nookit.modules.auth.dto.UserInfoVO;
import com.nookit.modules.auth.service.AuthService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "认证接口（学生/管理员通用）")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "账号密码登录（通用）")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        // JWT 无状态，客户端丢弃 token 即可；如需服务端黑名单可在此扩展 Redis
        return Result.success();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息")
    public Result<UserInfoVO> me(@CurrentUser UserPrincipal principal) {
        return Result.success(authService.me(principal));
    }
}
