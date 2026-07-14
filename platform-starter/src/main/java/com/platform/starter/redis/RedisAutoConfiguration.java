package com.platform.starter.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * Redis 自动配置类
 *
 * <p>基于 Spring Boot 自动装配机制，满足以下全部条件时才注入 Redis 相关 Bean：</p>
 * <ol>
 *   <li>classpath 下存在 {@link RedisOperations} 类（即引入了 spring-boot-starter-data-redis）</li>
 *   <li>{@code platform.redis.enabled=true}（默认开启，设为 false 可完全禁用）</li>
 * </ol>
 *
 * <p>本配置会自动创建 {@link RedisTemplate}（若容器中不存在），采用以下序列化策略，
 * 确保在 Redis 可视化软件（如 RedisInsight、Another Redis Desktop Manager）中可读性良好：</p>
 * <ul>
 *   <li>Key / HashKey：{@link StringRedisSerializer}，存储为纯字符串</li>
 *   <li>Value / HashValue：{@link GenericJackson2JsonRedisSerializer}，存储为纯 JSON（不携带类名）</li>
 * </ul>
 *
 * <p>注意：由于 value 不保留类型信息，反序列化后为 {@code LinkedHashMap}，
 * 业务侧取值后需自行转换为目标类型（可借助 {@code RedisUtil.get(key, clazz)} 或 MapStruct）。</p>
 *
 * @author platform
 */
@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(prefix = "platform.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisAutoConfiguration.class);

    /**
     * 注入 RedisTemplate（若容器中不存在则创建）
     *
     * <p>使用 String 序列化 key，JSON 序列化 value，确保可视化软件中可读。</p>
     * <p>若业务模块已自定义 RedisTemplate，则跳过，不会覆盖。</p>
     *
     * @param connectionFactory Redis 连接工厂（由 spring-boot-starter-data-redis 自动注入）
     * @return RedisTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // key / hashKey 采用 String 序列化，可视化软件中直接可读
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value / hashValue 采用 JSON 序列化，不保留类型信息，可视化软件中显示为纯 JSON
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(buildObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("[Redis] RedisTemplate 初始化成功 | key: StringSerializer | value: GenericJackson2JsonRedisSerializer(纯JSON)");
        return template;
    }

    /**
     * 注入 Redis 工具类
     *
     * @param redisTemplate  Redis 操作模板
     * @param redisProperties Redis 配置属性（全局过期时间、key 前缀等）
     * @return Redis 工具类实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisUtil.class)
    public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
        RedisUtil redisUtil = new RedisUtil(redisTemplate, redisProperties);
        log.info("[Redis] RedisUtil 初始化成功 | prefix: {} | ttl: {} {}",
                redisProperties.getPrefix(),
                redisProperties.getTtl(),
                redisProperties.getTtlUnit());
        return redisUtil;
    }

    /**
     * 构建 ObjectMapper
     *
     * <p>注册 Java8 时间模块，禁用 default typing（不写入类名），
     * 使 Redis 中存储的 value 为纯 JSON，在可视化软件中可读性最佳。</p>
     *
     * @return ObjectMapper
     */
    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 支持 Java8 时间类型（LocalDateTime / LocalDate 等）
        objectMapper.registerModule(new JavaTimeModule());
        // 序列化时禁用将日期写为时间戳，使用 ISO-8601 格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 所有字段都参与序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 关键：不启用 default typing，不写入类名，value 存储为纯 JSON
        return objectMapper;
    }
}
