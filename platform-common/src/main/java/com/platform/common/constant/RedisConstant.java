package com.platform.common.constant;

/**
 * 通用常量池
 *
 * @author platform
 */
public final class RedisConstant {

    private RedisConstant() {
    }

    /**
     * 用户权限常量
     */
    public static final String USER_AUTH_PERM = "user:auth:perm:";

    /**
     * 用户角色常量
     */
    public static final String USER_AUTH_ROLE = "user:auth:role:";

    /**
     * 用户详情缓存常量（前缀，后接 userId）
     *
     * <p>缓存 /user/info 聚合结果（基本信息 + 角色 + 权限 + 菜单树）。
     * 用户角色/权限/状态变更时需失效。</p>
     */
    public static final String USER_INFO = "sys:user:info:";

    /**
     * 用户密码更新时间
     */
    public static final String USER_PWD_UPDATE_TIME = "sys:user:pwd:updateTime:";

    /**
     * 部门树缓存常量
     */
    public static final String DEPT_TREE = "sys:dept:tree";

    /**
     * 菜单树缓存常量
     */
    public static final String MENU_TREE = "sys:menu:tree";

    /**
     * 系统配置缓存常量（前缀，后接 config_key）
     */
    public static final String SYS_CONFIG = "sys:config:";

    /**
     * 系统字典缓存常量（前缀，后接 dict_type）
     */
    public static final String SYS_DICT = "sys:dict:";

    /**
     * 验证码缓存常量（前缀，后接 captcha_key）
     */
    public static final String CAPTCHA = "sys:captcha:";

    /**
     * 二次认证常量（前缀，后接 userId）
     */
    public static final String SECOND_AUTH_LOCK = "second:auth:lock:";
}
