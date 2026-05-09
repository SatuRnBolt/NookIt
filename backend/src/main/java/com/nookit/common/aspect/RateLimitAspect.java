package com.nookit.common.aspect;

import com.nookit.common.annotation.RateLimit;
import com.nookit.common.api.ResultCode;
import com.nookit.common.constant.CacheKeyConstants;
import com.nookit.common.exception.BusinessException;
import com.nookit.common.util.IpUtil;
import com.nookit.common.util.RedisUtil;
import com.nookit.common.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 基于 Redis 计数器的简单限流切面。
 * <p>
 * 高并发场景下建议替换为基于 Redis Lua 的滑动窗口或令牌桶。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.nookit.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimit limit = method.getAnnotation(RateLimit.class);

        String dimensionKey = resolveDimension(limit, method);
        String redisKey = CacheKeyConstants.RATE_LIMIT
                + (limit.key().isEmpty() ? method.getDeclaringClass().getSimpleName() + "." + method.getName() : limit.key())
                + ":" + dimensionKey;

        long ttlMs = limit.unit().toMillis(limit.window());
        Long count = redisUtil.incrementWithExpire(redisKey, Duration.ofMillis(ttlMs));
        if (count != null && count > limit.limit()) {
            log.warn("rate limited: key={}, count={}, limit={}", redisKey, count, limit.limit());
            throw new BusinessException(ResultCode.REQUEST_TOO_FREQUENT);
        }
        return pjp.proceed();
    }

    private String resolveDimension(RateLimit limit, Method method) {
        return switch (limit.dimension()) {
            case USER -> {
                Long userId = SecurityContextUtil.getCurrentUserId();
                yield userId != null ? userId.toString() : "anonymous";
            }
            case IP -> currentIp();
            case GLOBAL -> "global";
        };
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest req = attrs.getRequest();
            return IpUtil.getClientIp(req);
        }
        return "unknown";
    }
}
