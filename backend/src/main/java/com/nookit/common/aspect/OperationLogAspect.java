package com.nookit.common.aspect;

import com.nookit.common.annotation.OperationLog;
import com.nookit.common.util.IpUtil;
import com.nookit.common.util.JsonUtil;
import com.nookit.common.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * {@link OperationLog} 切面。当前实现仅落 INFO 日志，后续可扩展为写入操作日志表。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(com.nookit.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        Long userId = SecurityContextUtil.getCurrentUserId();
        String ip = currentIp();
        String paramsJson = annotation.recordParams() ? safeJson(pjp.getArgs()) : null;

        try {
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("[OpLog] module={}, action={}, desc={}, userId={}, ip={}, cost={}ms, params={}, result={}",
                    annotation.module(), annotation.action(), annotation.description(),
                    userId, ip, cost, paramsJson,
                    annotation.recordResult() ? safeJson(result) : "<omitted>");
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[OpLog][FAIL] module={}, action={}, userId={}, ip={}, cost={}ms, params={}, error={}",
                    annotation.module(), annotation.action(), userId, ip, cost, paramsJson, ex.getMessage());
            throw ex;
        }
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest req = attrs.getRequest();
            return IpUtil.getClientIp(req);
        }
        return null;
    }

    private String safeJson(Object obj) {
        try {
            return JsonUtil.toJson(obj);
        } catch (Exception e) {
            return "<unserializable>";
        }
    }
}
