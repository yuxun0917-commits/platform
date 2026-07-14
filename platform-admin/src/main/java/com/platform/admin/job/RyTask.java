package com.platform.admin.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 定时任务示例 Bean
 *
 * <p>配合 sys_job 表预置数据，invoke_target 形如：
 * <ul>
 *   <li>{@code ryTask.ryNoParams} —— 无参示例</li>
 *   <li>{@code ryTask.ryParams('ry')} —— 有参示例</li>
 *   <li>{@code ryTask.cleanData} —— 数据清理示例</li>
 * </ul>
 * beanName 显式指定为 {@code ryTask}，与 invoke_target 前缀一致。</p>
 *
 * @author platform
 */
@Slf4j
@Component("ryTask")
public class RyTask {

    /**
     * 无参示例任务
     */
    public void ryNoParams() {
        log.info("[RyTask] ryNoParams 执行成功（无参示例）");
    }

    /**
     * 有参示例任务
     *
     * @param params 参数
     */
    public void ryParams(String params) {
        log.info("[RyTask] ryParams 执行成功，参数：{}", params);
    }

    /**
     * 数据清理示例任务
     */
    public void cleanData() {
        log.info("[RyTask] cleanData 执行成功（每日数据清理示例）");
    }

    /**
     * 耗时任务（用于测试并发策略）
     *
     * <p>执行约 5 秒，打印开始/结束与线程名。配合 cron 间隔小于 5s 的任务，
     * 可观察 concurrent=1（允许并发，多实例重叠）与 concurrent=0（禁止并发，跳过重叠触发）的差异。</p>
     */
    public void longTask() {
        String thread = Thread.currentThread().getName();
        log.info("[RyTask] longTask 开始 | 线程={} | 时间={}", thread, LocalTime.now());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[RyTask] longTask 结束 | 线程={} | 时间={}", thread, LocalTime.now());
    }
}
