package com.platform.starter.ratelimiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * 限流配置属性类
 *
 * <p>对应 application.yml 中 {@code platform.rate-limiter} 前缀的配置项。</p>
 *
 * @author platform
 */
@Data
@ConfigurationProperties(prefix = "platform.rate-limiter")
public class RateLimiterProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否启用限流自动配置（默认开启） */
    private boolean enabled = true;

    /** 限流类型（IP / GLOBAL / USER），默认 IP */
    private String type = "IP";

    /** 默认限流次数（单位时间内允许的请求次数，默认 100） */
    private int defaultLimit = 100;

    /** 默认时间窗口（秒，默认 60） */
    private int defaultTimeout = 60;
}
