package com.platform.component.job;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止并发执行的任务类
 *
 * <p>{@link DisallowConcurrentExecution} 必须标注在 Job 类上，无法动态添加，
 * 因此为禁止并发的任务单独提供一个子类，由 {@link ScheduleUtils} 按 concurrent 字段选择。</p>
 *
 * @author platform
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends QuartzJobExecution {
}
