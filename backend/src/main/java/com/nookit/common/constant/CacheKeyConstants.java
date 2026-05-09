package com.nookit.common.constant;

/**
 * Redis Key 命名规范。统一前缀 {@code nookit:}，方便排查与隔离。
 * <p>
 * 命名约定：{@code nookit:<domain>:<purpose>:<id>}
 */
public final class CacheKeyConstants {

    private CacheKeyConstants() {
    }

    public static final String PREFIX = "nookit:";

    /** 用户相关 */
    public static final String USER_TOKEN = PREFIX + "user:token:";          // + userId
    public static final String USER_PERMISSION = PREFIX + "user:perm:";      // + userId
    public static final String LOGIN_FAIL_COUNT = PREFIX + "login:fail:";    // + account

    /** 预约 / 座位 */
    public static final String SEAT_LOCK = PREFIX + "lock:seat:";            // + seatId:date:hour
    public static final String ROOM_AVAILABILITY = PREFIX + "room:avail:";   // + roomId:date

    /** 签到码 */
    public static final String ROOM_CHECKIN_CODE = PREFIX + "checkin:code:"; // + roomId:date

    /** 限流 */
    public static final String RATE_LIMIT = PREFIX + "ratelimit:";           // + key

    /** 系统配置 */
    public static final String SYSTEM_SETTINGS = PREFIX + "system:settings";
}
