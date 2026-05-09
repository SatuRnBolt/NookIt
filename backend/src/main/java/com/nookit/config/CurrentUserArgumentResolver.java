package com.nookit.config;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.ResultCode;
import com.nookit.common.exception.AuthException;
import com.nookit.common.util.SecurityContextUtil;
import com.nookit.security.UserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * 解析 {@link CurrentUser} 注解，将登录用户注入到 Controller 方法参数。
 * <p>
 * 支持的参数类型：{@link UserPrincipal} 或 {@link Long}（用户 ID）。
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CurrentUser.class)) {
            return false;
        }
        Class<?> type = parameter.getParameterType();
        return UserPrincipal.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        Optional<UserPrincipal> userOpt = SecurityContextUtil.getCurrentUser();

        if (userOpt.isEmpty()) {
            if (annotation != null && annotation.required()) {
                throw new AuthException(ResultCode.UNAUTHORIZED);
            }
            return null;
        }

        UserPrincipal user = userOpt.get();
        if (Long.class.isAssignableFrom(parameter.getParameterType())) {
            return user.getUserId();
        }
        return user;
    }
}
