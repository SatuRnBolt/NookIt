package com.nookit.common.exception;

import com.nookit.common.api.ResultCode;

import java.io.Serial;

/**
 * 业务校验异常，对应 422。用于表达「参数本身合法，但不满足业务约束」。
 */
public class ValidationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(ResultCode.PARAM_VALIDATE_FAILED, message);
    }

    public ValidationException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
