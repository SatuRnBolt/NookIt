package com.nookit.common.exception;

import com.nookit.common.api.ResultCode;

import java.io.Serial;

/**
 * 资源状态冲突异常，对应 409。例如座位已被预约、并发更新等。
 */
public class ConflictException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(ResultCode resultCode) {
        super(resultCode);
    }

    public ConflictException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
