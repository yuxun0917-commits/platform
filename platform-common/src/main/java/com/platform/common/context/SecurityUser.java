package com.platform.common.context;

import cn.dev33.satoken.exception.NotLoginException;

import java.util.Objects;

/**
 * 安全用户工具类
 * <p>
 * 提供线程安全的当前登录用户信息读取能力,业务层统一通过此类获取用户/租户标识
 * <p>
 * 设计原则:
 * <ul>
 *     <li>业务层不直接依赖 sa-token API,通过本类隔离认证框架</li>
 *     <li>未登录时抛出 {@link NotLoginException},强制业务层感知登录态</li>
 *     <li>所有方法均为静态工具方法,无状态,线程安全</li>
 * </ul>
 *
 * @author yuxun
 */
public final class SecurityUser {

    private SecurityUser() {
    }

    // ==================== userId 相关 ====================

    /**
     * 获取当前登录用户 id
     *
     * @return 用户 id
     * @throws NotLoginException 未登录时抛出
     */
    public static Long getUserId() {
        UserContext ctx = requireContext();
        return ctx.getUserId();
    }

    /**
     * 获取当前登录用户 id(Long),安全模式
     * <p>
     * 未登录时返回 null 而非抛异常,适用于可选登录的场景(如公开页面获取可选用户信息)
     *
     * @return 用户 id,未登录返回 null
     */
    public static Long getUserIdAsLongOrNull() {
        UserContext ctx = UserContextHolder.get();
        return Objects.isNull(ctx) ? null : ctx.getUserId();
    }

    // ==================== tenantId 相关 ====================

    /**
     * 获取当前租户 id
     *
     * @return 租户 id
     * @throws NotLoginException 未登录时抛出
     */
    public static Long getTenantId() {
        UserContext ctx = requireContext();
        return ctx.getTenantId();
    }

    /**
     * 获取当前租户 id(Long),安全模式
     * <p>
     * 未登录时返回 null 而非抛异常
     *
     * @return 租户 id,未登录返回 null
     */
    public static Long getTenantIdAsLongOrNull() {
        UserContext ctx = UserContextHolder.get();
        return Objects.isNull(ctx) ? null : ctx.getTenantId();
    }

    public static String getUsername() {
        return requireContext().getUsername();
    }

    public static String getUsernameAsNull() {
        UserContext ctx = UserContextHolder.get();
        return Objects.isNull(ctx) ? null : ctx.getUsername();
    }

    public static String getNickname() {
        return requireContext().getNickname();
    }
    public static String getNicknameAsNull() {
        UserContext ctx = UserContextHolder.get();
        return Objects.isNull(ctx) ? null : ctx.getNickname();
    }

    // ==================== 上下文判断 ====================

    /**
     * 当前是否已登录
     *
     * @return true 已登录, false 未登录
     */
    public static boolean isLogin() {
        return Objects.nonNull(UserContextHolder.get());
    }

    // ==================== 内部方法 ====================

    /**
     * 强制获取上下文,未设置时抛出未登录异常
     *
     * @return 用户上下文
     * @throws NotLoginException 上下文为空时抛出
     */
    private static UserContext requireContext() {
        UserContext ctx = UserContextHolder.get();
        if (Objects.isNull(ctx)) {
            throw NotLoginException.newInstance(null, null, NotLoginException.NOT_TOKEN_MESSAGE, null);
        }
        return ctx;
    }
}
