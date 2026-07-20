package com.platform.starter.ratelimiter;

import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 *
 * <p>拦截标注了 {@link RateLimit} 的方法，基于 Redis 计数器实现限流。</p>
 *
 * @author platform
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 限流环绕通知
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        long count = redisTemplate.opsForValue().increment(key, 1L);
        if (count == 1L) {
            // 首次访问，设置过期时间
            redisTemplate.expire(key, rateLimit.timeout(), TimeUnit.SECONDS);
        }
        if (count > rateLimit.limit()) {
            log.warn("[限流拦截] key={}, count={}, limit={}", key, count, rateLimit.limit());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
        }
        return joinPoint.proceed();
    }

    /**
     * 构建限流 Redis Key
     *
     * <p>使用「目标类名#方法名」作为方法唯一标识，确保不同接口各自独立计数；
     * 旧实现用调用栈取方法名恒返回 "around"，会导致所有限流接口共用同一计数桶。</p>
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = "rate_limit:";
        String methodKey = joinPoint.getTarget().getClass().getName()
                + "#" + joinPoint.getSignature().getName();
        if ("GLOBAL".equalsIgnoreCase(rateLimit.type())) {
            return prefix + "global:" + methodKey;
        }
        // IP 维度
        return prefix + "ip:" + getClientIp() + ":" + methodKey;
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (Objects.isNull(ip) || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (Objects.isNull(ip) || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
