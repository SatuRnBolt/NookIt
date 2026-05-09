package com.nookit.common.controller;

import com.nookit.common.util.IpUtil;
import com.nookit.common.util.SecurityContextUtil;
import com.nookit.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Controller 公共能力基类。<b>不要</b>把业务逻辑写在这里，仅提供上下文工具方法。
 */
public abstract class BaseController {

    protected Optional<UserPrincipal> currentUser() {
        return SecurityContextUtil.getCurrentUser();
    }

    protected UserPrincipal requireCurrentUser() {
        return SecurityContextUtil.requireCurrentUser();
    }

    protected Long currentUserId() {
        return SecurityContextUtil.getCurrentUserId();
    }

    protected Long requireCurrentUserId() {
        return SecurityContextUtil.requireCurrentUserId();
    }

    protected HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    protected String currentClientIp() {
        return IpUtil.getClientIp(currentRequest());
    }
}
