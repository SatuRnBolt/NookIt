package com.nookit.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 业务枚举基础接口。约定枚举对外序列化为 {@link #getCode()}，便于前后端对齐。
 *
 * @param <T> 编码类型（通常为 String 或 Integer）
 */
public interface BaseEnum<T> {

    @JsonValue
    T getCode();

    String getDesc();
}
