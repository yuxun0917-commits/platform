package com.platform.component.admin.role;

import cn.hutool.extra.spring.SpringUtil;
import com.platform.common.event.AllUserCacheDeleteEvent;
import com.platform.framework.manager.AsyncManager;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * 角色业务组合组件
 *
 * <p>封装角色相关的跨模块组合操作，供 Controller 层调用。
 * 典型场景：禁用角色 / 删除角色后，需要清除所有用户的权限/角色缓存，
 * 因为权限和角色缓存是按用户ID存储的，角色变更会影响所有关联用户。</p>
 *
 * @author platform
 */
@Component
@RequiredArgsConstructor
public class RoleComponent {

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
     * 异步清除角色关联缓存
     *
     * <p>在禁用角色、删除角色等场景中复用：清除所有用户的权限缓存和角色缓存。
     * 异步执行，不阻塞主流程。</p>
     *
     * <p>清理内容：</p>
     * <ul>
     *   <li>清除所有用户的权限缓存（user:auth:perm:*）</li>
     *   <li>清除所有用户的角色缓存（user:auth:role:*）</li>
     * </ul>
     *
     * <p>角色缓存按用户ID存储，角色变更会影响所有关联用户，
     * 因此采用全量清除策略，用户下次访问时重建缓存。</p>
     */
    public void cleanRoleCache() {
        asyncManager.execute(() -> {
            // 清除所有用户的权限与角色缓存
            SpringUtil.getApplicationContext().publishEvent(new AllUserCacheDeleteEvent(this));
        });
    }
}
