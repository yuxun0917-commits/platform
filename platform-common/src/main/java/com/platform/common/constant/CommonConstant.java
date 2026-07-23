package com.platform.common.constant;

/**
 * 通用常量池
 *
 * @author platform
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /** UTF-8 字符集 */
    public static final String UTF_8 = "UTF-8";

    /** 默认页码 */
    public static final long DEFAULT_PAGE_NUM = 1L;

    /** 默认每页条数 */
    public static final long DEFAULT_PAGE_SIZE = 10L;

    /** 状态：启用 */
    public static final Integer STATUS_ENABLE = 1;

    /** 状态：禁用 */
    public static final Integer STATUS_DISABLE = 0;

    /** 逻辑删除：未删除 */
    public static final Integer NOT_DELETED = 0;

    /** 逻辑删除：已删除 */
    public static final Integer DELETED = 1;

    /** 请求头 Authorization */
    public static final String AUTHORIZATION = "Authorization";

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 会话中存储租户 id 的 key */
    public static final String SESSION_TENANT_KEY = "tenantId";
    public static final String SESSION_USERNAME_KEY = "username";
    public static final String SESSION_NICKNAME_KEY = "nickname";
    public static final String SESSION_PWD_UPDATE_KEY = "pwdUpdateTime";

}
