package com.platform.component.job;

import com.platform.common.entity.admin.SysJob;
import com.platform.common.enums.MisfirePolicyEnum;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * Quartz 调度工具类
 *
 * <p>构建 {@link JobDetail} 与 {@link CronTrigger}，处理 misfire 策略映射与并发控制。</p>
 *
 * @author platform
 */
public class ScheduleUtils {

    /** 任务分组（所有平台任务归入同一组，便于统一管控） */
    public static final String JOB_GROUP = "PLATFORM_JOB";

    private ScheduleUtils() {
    }

    /**
     * 构建 JobDetail
     *
     * <p>根据 concurrent 字段决定使用允许并发还是禁止并发的 Job 实现类。
     * JobDataMap 中放入 jobId，供执行时回查任务定义。</p>
     *
     * @param job 任务实体
     * @return JobDetail
     */
    public static JobDetail createJobDetail(SysJob job) {
        Class<? extends org.quartz.Job> jobClass = (job.getConcurrent() != null && job.getConcurrent() == 0)
                ? QuartzDisallowConcurrentExecution.class
                : QuartzJobExecution.class;
        return JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(job.getId()))
                .usingJobData("jobId", job.getId())
                .build();
    }

    /**
     * 构建 CronTrigger
     *
     * <p>按 misfire_policy 设置错失触发处理策略：
     * 1=补跑（FireAndProceed），0=不补跑（DoNothing）。</p>
     *
     * @param job 任务实体
     * @return CronTrigger
     */
    public static CronTrigger createTrigger(SysJob job) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        if (job.getMisfirePolicy() != null && job.getMisfirePolicy() == MisfirePolicyEnum.FIRE_AND_PROCEED.getCode()) {
            scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else {
            scheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(job.getId()))
                .withSchedule(scheduleBuilder)
                .build();
    }

    /**
     * 获取任务 Key
     *
     * @param jobId 任务ID
     * @return JobKey
     */
    public static JobKey getJobKey(Long jobId) {
        return JobKey.jobKey(String.valueOf(jobId), JOB_GROUP);
    }

    /**
     * 获取触发器 Key
     *
     * @param jobId 任务ID
     * @return TriggerKey
     */
    public static TriggerKey getTriggerKey(Long jobId) {
        return TriggerKey.triggerKey(String.valueOf(jobId), JOB_GROUP);
    }
}
