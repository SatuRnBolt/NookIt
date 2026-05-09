package com.nookit.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。打在 Controller 方法上，由 {@code OperationLogAspect} 切面落库 / 打印审计日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作模块，例如 "reservation"、"room"、"user" */
    String module();

    /** 操作动作，例如 "create"、"cancel"、"assignRole" */
    String action();

    /** 简要描述，会写入审计日志 */
    String description() default "";

    /** 是否记录入参 */
    boolean recordParams() default true;

    /** 是否记录出参 */
    boolean recordResult() default false;
}
