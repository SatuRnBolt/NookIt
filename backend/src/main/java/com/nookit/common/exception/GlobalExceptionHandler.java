package com.nookit.common.exception;

import com.nookit.common.api.Result;
import com.nookit.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。统一把异常翻译为 {@link Result} 结构。
 * <p>
 * 注意：兜底分支不要把堆栈信息回显给前端，仅返回 traceId。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ------------------------------ 业务异常 ------------------------------ */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("[Business] {} {} -> code={}, message={}", request.getMethod(), request.getRequestURI(),
                ex.getCode(), ex.getMessage());
        HttpStatus status = mapStatus(ex);
        return ResponseEntity.status(status).body(Result.error(ex.getCode(), ex.getMessage()));
    }

    /* ------------------------------ 参数校验 ------------------------------ */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return badRequest(ResultCode.PARAM_VALIDATE_FAILED, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return badRequest(ResultCode.PARAM_VALIDATE_FAILED, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        return badRequest(ResultCode.PARAM_VALIDATE_FAILED, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return badRequest(ResultCode.BAD_REQUEST, "缺少必要参数：" + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest(ResultCode.BAD_REQUEST, "参数类型错误：" + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return badRequest(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    /* ------------------------------ 认证 / 授权 ------------------------------ */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("[Auth] authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[Auth] access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(ResultCode.FORBIDDEN, ResultCode.FORBIDDEN.getMessage()));
    }

    /* ------------------------------ HTTP / 路由 ------------------------------ */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.error(ResultCode.METHOD_NOT_ALLOWED, ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(ResultCode.RESOURCE_NOT_FOUND, "接口不存在：" + ex.getRequestURL()));
    }

    /* ------------------------------ 数据库 ------------------------------ */

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("[DB] duplicate key: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.error(ResultCode.RESOURCE_CONFLICT, "数据已存在或唯一约束冲突"));
    }

    /* ------------------------------ 兜底 ------------------------------ */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error("[Unhandled] {} {} -> {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.INTERNAL_ERROR));
    }

    /* ------------------------------ 工具方法 ------------------------------ */

    private ResponseEntity<Result<Void>> badRequest(ResultCode code, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(code, message));
    }

    private HttpStatus mapStatus(BusinessException ex) {
        if (ex instanceof AuthException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof ValidationException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
