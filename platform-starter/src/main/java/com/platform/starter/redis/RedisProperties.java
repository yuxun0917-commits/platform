package com.platform.starter.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * Redis 相关配置属性
 *
 * <p>在 {@code application.yml} 中通过 {@code platform.redis} 前缀进行配置。</p>
 *
 * <p>示例：</p>
 * <pre>
 * platform:
 *   redis:
 *     enabled: true              # 是否启用 Redis，默认 true
 *     global-ttl: 30             # 全局默认过期时间，默认 30
 *     global-ttl-unit: MINUTES   # 时间单位，默认 MINUTES（分钟）
 *     key-prefix: "platform:"    # 全局 key 前缀，默认 "platform:"
 * </pre>
 *
 * @author platform
 */
@ConfigurationProperties(prefix = "platform.redis")
public class RedisProperties {

    /**
     * 是否启用 Redis，默认开启
     */
    @Setter
    @Getter
    private boolean enabled = true;

    /**
     * 全局默认过期时间
     * <p>当业务侧调用 {@code set(key, value)} 未显式指定过期时间时，使用此值；
     * 设为 0 或负数表示不过期（永久有效）。</p>
     */
    @Setter
    @Getter
    private long ttl = 30;

    /**
     * 全局默认过期时间单位，默认分钟
     */
    @Getter
    private final TimeUnit ttlUnit = TimeUnit.MINUTES;

    /**
     * 全局 key 前缀
     * <p>所有写入 Redis 的 key 会自动拼接此前缀，避免不同业务/模块的 key 冲突。
     * 建议以冒号结尾，如 "platform:"。</p>
     */
    @Setter
    @Getter
    private String prefix = "platform:";
}
