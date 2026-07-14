package com.platform.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * 所有用户缓存删除事件
 *
 * <p>当发生影响全部用户的配置/权限变更（如全局权限规则调整、菜单结构变更等）时发布该事件，
 * 监听器据此清除 Redis 中所有用户的权限缓存、角色缓存与详情缓存：
 * {@code user:auth:perm:*}、{@code user:auth:role:*}、{@code sys:user:info:*}。</p>
 *
 * <p>由 {@link org.springframework.context.ApplicationEventPublisher} 发布，
 * 通过 {@code @EventListener} 订阅消费。</p>
 *
 * @author platform
 */
public class AllUserCacheDeleteEvent extends ApplicationEvent {

    public AllUserCacheDeleteEvent(Object source) {
        super(source);
    }
}
