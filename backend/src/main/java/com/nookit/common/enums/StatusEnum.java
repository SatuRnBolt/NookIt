package com.nookit.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用启停状态。
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements BaseEnum<String> {

    ACTIVE("active", "启用"),
    INACTIVE("inactive", "停用"),
    SUSPENDED("suspended", "暂停"),
    LOCKED("locked", "已锁定");

    private final String code;
    private final String desc;
}
