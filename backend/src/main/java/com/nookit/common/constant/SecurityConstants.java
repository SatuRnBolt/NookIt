package com.nookit.common.constant;

/**
 * 安全相关常量。
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_USERNAME = "uname";
    public static final String CLAIM_USER_TYPE = "utype";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "perms";

    /** Spring Security 角色前缀 */
    public static final String ROLE_PREFIX = "ROLE_";
}
