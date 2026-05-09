package com.nookit.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法参数上，由 {@code CurrentUserArgumentResolver} 注入登录用户。
 * <p>
 * 支持的参数类型：{@link com.nookit.security.UserPrincipal} 或 {@link Long}（用户 ID）。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {

    /**
     * 是否必须登录。{@code true} 时未登录抛出 {@code AuthException}；
     * {@code false} 时未登录注入 {@code null}。
     */
    boolean required() default true;
}
