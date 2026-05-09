package com.nookit.common.util;

import com.nookit.common.api.ResultCode;
import com.nookit.common.exception.AuthException;
import com.nookit.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 从 {@link SecurityContextHolder} 中读取当前登录用户。所有业务侧获取登录态都应走本类，
 * 避免散落使用 {@code SecurityContextHolder} / ThreadLocal。
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static UserPrincipal requireCurrentUser() {
        return getCurrentUser().orElseThrow(() -> new AuthException(ResultCode.UNAUTHORIZED));
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getUserId).orElse(null);
    }

    public static Long requireCurrentUserId() {
        return requireCurrentUser().getUserId();
    }
}
