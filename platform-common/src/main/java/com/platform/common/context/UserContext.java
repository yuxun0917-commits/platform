package com.platform.common.context;

import lombok.Getter;

/**
 * 当前请求的用户上下文
 * <p>
 * 封装从 sa-token 会话解析出的用户标识与租户标识,供业务层通过 ThreadLocal 获取
 *
 * @author yuxun
 */
@Getter
public class UserContext {

    /** 登录账号 id */
    private final Long userId;

    /** 租户 id(多租户场景下用于数据隔离) */
    private final Long tenantId;

    private final String username;

    private final String nickname;

    public UserContext(Long userId, Long tenantId, String username, String nickname) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.nickname = nickname;
    }
}
