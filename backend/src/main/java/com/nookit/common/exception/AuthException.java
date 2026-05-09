package com.nookit.common.exception;

import com.nookit.common.api.ResultCode;

import java.io.Serial;

/**
 * 认证 / 授权类异常，对应 401。
 */
public class AuthException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }

    public AuthException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
