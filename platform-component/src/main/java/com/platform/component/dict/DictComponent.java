package com.platform.component.dict;

import com.platform.common.constant.RedisConstant;
import com.platform.framework.manager.AsyncManager;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 数据字典业务组合组件
 *
 * <p>封装字典相关的跨模块组合操作，供 Controller 层调用。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>事务包装：doSomethingInTransactional</li>
 *   <li>字典缓存：getDictItems / cacheDictItems / cleanDictCache / cleanAllDictCache</li>
 * </ul>
 *
 * <p>缓存策略：每个字典类型的字典项列表以 JSON 形式缓存到 Redis，
 * key 为 {@code sys:dict:} + dictType。字典类型或字典项增删改后异步清除对应缓存。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictComponent {

    private final AsyncManager asyncManager;
    private final RedisUtil redisUtil;

    /**
     * 在事务中运行
     *
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return 业务逻辑返回值
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 将字典项列表写入缓存
     *
     * @param dictType 字典类型编码
     * @param items    字典项列表
     */
    public void cacheDictItems(String dictType, Object items) {
        redisUtil.set(RedisConstant.SYS_DICT + dictType, items);
    }

    /**
     * 从缓存读取字典项列表
     *
     * @param dictType 字典类型编码
     * @return 字典项列表，缓存未命中返回 null
     */
    public Object getDictItems(String dictType) {
        return redisUtil.get(RedisConstant.SYS_DICT + dictType);
    }

    /**
     * 异步清除单个字典类型的缓存
     *
     * <p>在添加、编辑、删除字典类型或字典项等场景中复用。
     * 异步执行，不阻塞主流程。</p>
     *
     * @param dictType 字典类型编码
     */
    public void cleanDictCache(String dictType) {
        asyncManager.execute(() -> {
            redisUtil.delete(RedisConstant.SYS_DICT + dictType);
            log.info("[DictCache] 字典缓存已清除: {}", dictType);
        });
    }

    /**
     * 异步清除全部字典缓存
     *
     * <p>清除以 {@code sys:dict:} 为前缀的所有 key。
     * 异步执行，不阻塞主流程。</p>
     */
    public void cleanAllDictCache() {
        asyncManager.execute(() -> {
            Set<String> keys = redisUtil.keys(RedisConstant.SYS_DICT + "*");
            if (Objects.nonNull(keys) && !keys.isEmpty()) {
                redisUtil.delete(keys);
            }
            log.info("[DictCache] 全部字典缓存已清除");
        });
    }
}
