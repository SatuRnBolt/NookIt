package com.nookit.common.exception;

import com.nookit.common.api.ResultCode;

import java.io.Serial;

/**
 * 资源不存在异常，对应 404。
 */
public class ResourceNotFoundException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(ResultCode resultCode) {
        super(resultCode);
    }

    public ResourceNotFoundException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ResourceNotFoundException(String message) {
        super(ResultCode.RESOURCE_NOT_FOUND, message);
    }
}
