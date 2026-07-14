package com.platform.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 用户上下文 ThreadLocal 持有器
 * <p>
 * 使用 TransmittableThreadLocal 而非原生 ThreadLocal,
 * 保证在使用线程池(@Async、并行流等)时上下文能正确传递与清理
 *
 * @author yuxun
 */
public final class UserContextHolder {

    /** 上下文载体,线程隔离 */
    private static final TransmittableThreadLocal<UserContext> CONTEXT = new TransmittableThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置当前线程的用户上下文
     *
     * @param context 用户上下文
     */
    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前线程的用户上下文
     *
     * @return 用户上下文,未设置时返回 null
     */
    public static UserContext get() {
        return CONTEXT.get();
    }

    /**
     * 清理当前线程的用户上下文
     * <p>
     * 必须在请求结束时调用,防止线程复用导致的数据串读
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
