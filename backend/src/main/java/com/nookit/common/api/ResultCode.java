package com.nookit.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局返回码枚举。
 * <p>
 * 编码规则：
 * <ul>
 *   <li>0          —— 成功</li>
 *   <li>10000-19999 —— 通用错误（参数、签名、限流等）</li>
 *   <li>20000-29999 —— 认证 / 鉴权</li>
 *   <li>30000-39999 —— 用户 / 账户域</li>
 *   <li>40000-49999 —— 自习室 / 座位域</li>
 *   <li>50000-59999 —— 预约 / 签到 / 违约域</li>
 *   <li>60000-69999 —— 通知 / 消息域</li>
 *   <li>70000-79999 —— 智能助手域</li>
 *   <li>90000-99999 —— 系统级 / 兜底</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),

    // 通用错误
    BAD_REQUEST(10001, "请求参数错误"),
    PARAM_VALIDATE_FAILED(10002, "参数校验失败"),
    METHOD_NOT_ALLOWED(10003, "请求方法不被支持"),
    REQUEST_TOO_FREQUENT(10004, "请求过于频繁，请稍后再试"),
    RESOURCE_NOT_FOUND(10005, "请求的资源不存在"),
    RESOURCE_CONFLICT(10006, "资源状态冲突"),

    // 认证 / 鉴权
    UNAUTHORIZED(20001, "未登录或登录已过期"),
    TOKEN_INVALID(20002, "令牌无效"),
    TOKEN_EXPIRED(20003, "令牌已过期"),
    FORBIDDEN(20004, "无访问权限"),
    LOGIN_FAILED(20005, "账号或密码错误"),
    ACCOUNT_DISABLED(20006, "账号已被停用"),

    // 用户域
    USER_NOT_FOUND(30001, "用户不存在"),
    USER_ALREADY_EXISTS(30002, "用户已存在"),
    STUDENT_NO_DUPLICATED(30003, "学号已被注册"),
    EMAIL_DUPLICATED(30004, "邮箱已被注册"),

    // 自习室 / 座位域
    ROOM_NOT_FOUND(40001, "自习室不存在"),
    ROOM_CLOSED(40002, "自习室当前不开放"),
    SEAT_NOT_FOUND(40101, "座位不存在"),
    SEAT_UNAVAILABLE(40102, "座位当前不可用"),

    // 预约 / 签到域
    RESERVATION_NOT_FOUND(50001, "预约记录不存在"),
    RESERVATION_TIME_CONFLICT(50002, "时间段已被预约"),
    RESERVATION_OUT_OF_RANGE(50003, "预约时长超出限制"),
    RESERVATION_PAST_TIME(50004, "不能预约已过去的时间段"),
    RESERVATION_DUPLICATED(50005, "同一时间段已存在有效预约"),
    CHECKIN_CODE_INVALID(50101, "签到码无效或已过期"),
    CHECKIN_NOT_IN_TIME(50102, "不在签到时间范围内"),

    // 系统级
    INTERNAL_ERROR(90000, "系统内部错误"),
    SERVICE_UNAVAILABLE(90001, "服务暂不可用");

    private final int code;
    private final String message;
}
