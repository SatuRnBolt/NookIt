package com.nookit.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解。基于 Redis + 滑动窗口（或定长窗口）实现。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流维度的 key 前缀，默认使用 method 全限定名 + IP */
    String key() default "";

    /** 时间窗口内允许的最大请求次数 */
    int limit();

    /** 时间窗口长度 */
    int window() default 1;

    /** 时间窗口单位 */
    TimeUnit unit() default TimeUnit.SECONDS;

    /** 限流策略：USER 按用户 ID，IP 按客户端 IP，GLOBAL 按 key */
    Dimension dimension() default Dimension.IP;

    enum Dimension {
        USER, IP, GLOBAL
    }
}
