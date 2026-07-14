package com.platform.component.admin.dept;

import com.platform.common.constant.RedisConstant;
import com.platform.framework.manager.AsyncManager;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 部门业务组合组件
 *
 * <p>封装部门相关的跨模块组合操作，供 Controller 层调用。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>事务包装：doSomethingInTransactional</li>
 *   <li>部门树缓存：getDeptTree / setDeptTree / cleanDeptCache</li>
 * </ul>
 *
 * <p>缓存策略：部门树以 JSON 字符串形式缓存到 Redis，
 * key 为 {@code dept:tree}。增删改部门后异步清除缓存，下次查询时重建。
 * Component 层只负责读写 JSON 字符串，序列化/反序列化由 Controller 层处理。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptComponent {

    private final AsyncManager asyncManager;
    private final RedisUtil redisUtil;

    /**
     * 在事务中运行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 从缓存读取部门树 JSON
     *
     * <p>缓存命中返回 JSON 字符串，未命中返回 null。
     * 调用方需自行用 JacksonUtil 反序列化为目标类型。</p>
     *
     * @return 部门树 JSON 字符串，缓存未命中时返回 null
     */
    public String getDeptTree() {
        Object cached = redisUtil.get(RedisConstant.DEPT_TREE);
        if (Objects.isNull(cached)) {
            return null;
        }
        return cached.toString();
    }

    /**
     * 将部门树 JSON 写入缓存
     *
     * @param json 部门树 JSON 字符串
     */
    public void setDeptTree(String json) {
        redisUtil.set(RedisConstant.DEPT_TREE, json);
    }

    /**
     * 异步清除部门树缓存
     *
     * <p>在添加、编辑、删除、切换状态等场景中复用。
     * 异步执行，不阻塞主流程。</p>
     */
    public void cleanDeptCache() {
        asyncManager.execute(() -> {
            redisUtil.delete(RedisConstant.DEPT_TREE);
            log.info("[DeptCache] 部门树缓存已清除");
        });
    }
}
