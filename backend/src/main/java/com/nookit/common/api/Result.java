package com.nookit.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应包装。所有 Controller 出参都应包装为 {@link Result}。
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应体")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TRACE_ID_KEY = "traceId";

    @Schema(description = "业务码，0 表示成功")
    private int code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "服务器时间")
    private LocalDateTime timestamp;

    @Schema(description = "链路追踪 ID")
    private String traceId;

    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = build(ResultCode.SUCCESS, data);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return build(resultCode, null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = build(resultCode, null);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        result.setTraceId(MDC.get(TRACE_ID_KEY));
        return result;
    }

    private static <T> Result<T> build(ResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        result.setTraceId(MDC.get(TRACE_ID_KEY));
        return result;
    }

    public boolean isSuccess() {
        return code == ResultCode.SUCCESS.getCode();
    }
}
