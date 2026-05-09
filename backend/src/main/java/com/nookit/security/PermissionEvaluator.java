package com.nookit.security;

import com.nookit.common.util.SecurityContextUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 注册为 Spring Bean 名 {@code perm}，可在 {@code @PreAuthorize} 中使用：
 * <pre>
 *   &#64;PreAuthorize("@perm.has('reservation:write')")
 * </pre>
 */
@Component("perm")
public class PermissionEvaluator {

    public boolean has(String permission) {
        return SecurityContextUtil.getCurrentUser()
                .map(p -> p.hasPermission(permission))
                .orElse(false);
    }

    public boolean hasAny(String... permissions) {
        Optional<UserPrincipal> userOpt = SecurityContextUtil.getCurrentUser();
        if (userOpt.isEmpty() || permissions == null || permissions.length == 0) {
            return false;
        }
        UserPrincipal user = userOpt.get();
        for (String p : permissions) {
            if (user.hasPermission(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(String role) {
        return SecurityContextUtil.getCurrentUser()
                .map(p -> p.hasRole(role))
                .orElse(false);
    }
}
