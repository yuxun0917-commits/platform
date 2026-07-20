package com.platform.starter.ratelimiter;

import com.platform.starter.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 限流自动配置类
 *
 * <p>当 {@code platform.rate-limiter.enabled=true}（默认开启）且 Redis 可用时自动注入限流切面。</p>
 *
 * @author platform
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimiterProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "platform.rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimiterAutoConfiguration {

    /**
     * 注入限流切面
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RateLimiterAspect rateLimiterAspect(RedisTemplate<String, Object> redisTemplate) {
        return new RateLimiterAspect(redisTemplate);
    }
}
