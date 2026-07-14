package com.platform.component.job;

import com.platform.common.entity.admin.SysJob;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

/**
 * 定时任务调度组件
 *
 * <p>封装 Quartz {@link Scheduler} 的注册、暂停、恢复、删除与立即执行，
 * 并在应用启动时将数据库中状态正常的任务恢复到调度器。</p>
 *
 * <p>调度器由 spring-boot-starter-quartz 自动配置（默认内存 RAMJobStore），
 * 进程重启后调度器为空，需由 {@code JobInitRunner} 调用 {@link #initAllRunningJobs()} 重新拉起。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleComponent {

    private final Scheduler scheduler;
    private final SysJobService sysJobService;
    private final AsyncManager asyncManager;

    /**
     * 在事务中执行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 注册任务到调度器（创建 JobDetail + CronTrigger）
     *
     * @param job 任务实体
     * @throws SchedulerException 调度异常
     */
    public void createScheduleJob(SysJob job) throws SchedulerException {
        scheduler.scheduleJob(ScheduleUtils.createJobDetail(job), ScheduleUtils.createTrigger(job));
        log.info("[Schedule] 任务已注册 jobId={}, cron={}", job.getId(), job.getCronExpression());
    }

    /**
     * 暂停任务（trigger 保留，仅停止触发）
     *
     * @param jobId 任务ID
     * @throws SchedulerException 调度异常
     */
    public void pauseJob(Long jobId) throws SchedulerException {
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId));
    }

    /**
     * 恢复任务
     *
     * @param jobId 任务ID
     * @throws SchedulerException 调度异常
     */
    public void resumeJob(Long jobId) throws SchedulerException {
        scheduler.resumeJob(ScheduleUtils.getJobKey(jobId));
    }

    /**
     * 从调度器彻底删除任务
     *
     * @param jobId 任务ID
     * @throws SchedulerException 调度异常
     */
    public void deleteJob(Long jobId) throws SchedulerException {
        scheduler.deleteJob(ScheduleUtils.getJobKey(jobId));
        log.info("[Schedule] 任务已删除 jobId={}", jobId);
    }

    /**
     * 立即执行一次（不影响 cron 计划）
     *
     * @param job 任务实体
     * @throws SchedulerException 调度异常
     */
    public void runOnce(SysJob job) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobId", job.getId());
        scheduler.triggerJob(ScheduleUtils.getJobKey(job.getId()), dataMap);
    }

    /**
     * 启动时将所有正常状态且未删除的任务恢复到调度器
     */
    public void initAllRunningJobs() {
        List<SysJob> jobs = sysJobService.listNormalRunningJobs();
        for (SysJob job : jobs) {
            try {
                createScheduleJob(job);
            } catch (SchedulerException e) {
                log.error("[Schedule] 任务恢复失败 jobId={}", job.getId(), e);
            }
        }
        log.info("[Schedule] 定时任务恢复完成，共 {} 个", jobs.size());
    }
}
