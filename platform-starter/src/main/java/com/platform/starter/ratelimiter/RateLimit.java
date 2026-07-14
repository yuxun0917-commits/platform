package com.platform.starter.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 *
 * <p>标注在 Controller 方法上，配合 {@link RateLimiterAspect} 实现 AOP 限流。
 * 基于 Redis 计数器实现，支持 IP 维度与全局维度。</p>
 *
 * @author platform
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流次数（单位时间内允许的请求次数） */
    int limit() default 100;

    /** 时间窗口（秒） */
    int timeout() default 60;

    /** 限流类型（IP / GLOBAL） */
    String type() default "IP";

    /** 提示信息 */
    String message() default "请求过于频繁，请稍后再试";
}
