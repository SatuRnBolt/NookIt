package com.nookit.common.constant;

/**
 * 通用常量。
 */
public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_KEY = "traceId";

    public static final String REQUEST_ATTR_USER_ID = "nookit.userId";
    public static final String REQUEST_ATTR_USER = "nookit.currentUser";
}
