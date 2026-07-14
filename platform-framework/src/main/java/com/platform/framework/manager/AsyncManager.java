package com.platform.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * 异步任务管理器
 *
 * <p>封装平台异步线程池，提供统一的 fire-and-forget 异步执行入口。</p>
 *
 * <p>设计目的：</p>
 * <ul>
 *   <li>解决 {@code @Async} 自调用失效问题：同类内调用 {@code this.asyncMethod()} 不走代理，
 *       而 AsyncManager 是独立 Bean，任意位置注入调用均可生效</li>
 *   <li>统一异常处理：异步任务抛出的异常默认被静默吞掉，AsyncManager 自动捕获并记录日志，
 *       避免每个调用点重复写 try-catch</li>
 *   <li>调用简洁：{@code asyncManager.execute(() -> doSomething())} 一行搞定</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 删除用户后，异步踢下线 + 清缓存
 * asyncManager.execute(() -> {
 *     StpUtil.kickout(userId);
 *     redisUtil.delete("user:auth:perm:" + userId);
 *     redisUtil.delete("user:auth:role:" + userId);
 * });
 * }</pre>
 *
 * @author platform
 */
@Component
public class AsyncManager {

    private static final Logger log = LoggerFactory.getLogger(AsyncManager.class);

    private final Executor executor;

    /**
     * 构造方法，注入平台异步线程池
     *
     * @param executor 平台异步线程池（bean name: platformAsyncExecutor）
     */
    public AsyncManager(@Qualifier("platformAsyncExecutor") Executor executor) {
        this.executor = executor;
    }

    /**
     * 异步执行任务（fire-and-forget）
     *
     * <p>任务在平台异步线程池中执行，调用方无需等待。
     * 任务内部抛出的异常会被自动捕获并记录日志，不影响主流程。</p>
     *
     * @param task 待执行的任务
     */
    public void execute(Runnable task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("[异步任务] 执行异常", e);
            }
        });
    }
}
