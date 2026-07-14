package com.platform.component.admin.config;

import com.platform.common.constant.RedisConstant;
import com.platform.common.entity.admin.SysConfig;
import com.platform.common.utils.Assert;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysConfigService;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 系统配置业务组合组件
 *
 * <p>封装系统配置相关的跨模块组合操作，供 Controller 层调用。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>事务包装：doSomethingInTransactional</li>
 *   <li>配置缓存：getConfig / setConfig / cleanConfigCache / cleanAllConfigCache</li>
 * </ul>
 *
 * <p>缓存策略：每个配置项以 config_value 字符串形式缓存到 Redis，
 * key 为 {@code sys:config:} + configKey。增删改配置后异步清除对应缓存。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysConfigComponent {

    private final AsyncManager asyncManager;
    private final RedisUtil redisUtil;
    private final SysConfigService sysConfigService;

    /**
     * 在事务中运行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 从缓存读取配置值
     *
     * <p>缓存命中返回配置值字符串，未命中返回 null。</p>
     *
     * @param configKey 配置键名
     * @return 配置值字符串，缓存未命中时返回 null
     */
    public String getConfig(String configKey) {
        Object cached = redisUtil.get(RedisConstant.SYS_CONFIG + configKey);
        if (Objects.nonNull(cached)) {
            return cached.toString();
        }

        SysConfig config = sysConfigService.findByKey(configKey);
        Assert.notNull(config, "无效的配置值：{}", configKey);
        setConfig(configKey, config.getConfigValue());
        return config.getConfigValue();
    }

    /**
     * 将配置值写入缓存
     *
     * @param configKey   配置键名
     * @param configValue 配置值
     */
    public void setConfig(String configKey, String configValue) {
        redisUtil.set(RedisConstant.SYS_CONFIG + configKey, configValue, 3, TimeUnit.DAYS);
    }

    /**
     * 异步清除单个配置缓存
     *
     * <p>在添加、编辑、删除配置等场景中复用。
     * 异步执行，不阻塞主流程。</p>
     *
     * @param configKey 配置键名
     */
    public void cleanConfigCache(String configKey) {
        asyncManager.execute(() -> {
            redisUtil.delete(RedisConstant.SYS_CONFIG + configKey);
            log.info("[SysConfigCache] 配置缓存已清除: {}", configKey);
        });
    }

    /**
     * 异步清除全部配置缓存
     *
     * <p>清除以 {@code sys:config:} 为前缀的所有 key。
     * 异步执行，不阻塞主流程。</p>
     */
    public void cleanAllConfigCache() {
        asyncManager.execute(() -> {
            Set<String> keys = redisUtil.keys(RedisConstant.SYS_CONFIG + "*");
            if (Objects.nonNull(keys) && !keys.isEmpty()) {
                redisUtil.delete(keys);
            }
            log.info("[SysConfigCache] 全部配置缓存已清除");
        });
    }
}
