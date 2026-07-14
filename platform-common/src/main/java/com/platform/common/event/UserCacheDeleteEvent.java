package com.platform.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * 用户缓存删除事件
 *
 * <p>当用户角色、权限或状态等发生变化时发布该事件，
 * 监听器据此清除该用户在 Redis 中的权限缓存与角色缓存：
 * {@code user:auth:perm:{userId}}、{@code user:auth:role:{userId}}。</p>
 *
 * <p>由 {@link org.springframework.context.ApplicationEventPublisher} 发布，
 * 通过 {@code @EventListener} 或 {@code ApplicationListener} 订阅消费。</p>
 *
 * @author platform
 */
public class UserCacheDeleteEvent extends ApplicationEvent {

    /** 用户ID 或者 *（缓存 key 维度：user:auth:perm:{userId} / user:auth:role:{userId}） */
    private final Long userId;

    public UserCacheDeleteEvent(Long userId) {
        super(userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
