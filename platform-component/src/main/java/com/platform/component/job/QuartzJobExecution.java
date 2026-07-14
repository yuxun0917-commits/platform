package com.platform.component.job;

import cn.hutool.extra.spring.SpringUtil;
import com.platform.common.entity.admin.SysJob;
import com.platform.common.entity.admin.SysJobLog;
import com.platform.common.enums.JobLogStatusEnum;
import com.platform.service.service.SysJobLogService;
import com.platform.service.service.SysJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz 通用任务执行类
 *
 * <p>由 Quartz 反射实例化，非 Spring Bean，内部依赖（Service）通过 {@link SpringUtil} 获取。
 * 执行流程：从 JobDataMap 取 jobId → 查库拿任务定义 → 解析 invokeTarget 反射调用 → 写执行日志。</p>
 *
 * @author platform
 */
@Slf4j
public class QuartzJobExecution implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getJobDetail().getJobDataMap().getLong("jobId");
        SysJobService jobService = SpringUtil.getBean(SysJobService.class);
        SysJobLogService jobLogService = SpringUtil.getBean(SysJobLogService.class);

        SysJob job = jobService.getById(jobId);
        if (job == null) {
            log.warn("[QuartzJob] 任务不存在，已跳过 jobId={}", jobId);
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            JobInvokeUtil.invokeMethod(job);
            long cost = System.currentTimeMillis() - startTime;
            jobLogService.save(buildLog(job, "执行成功", JobLogStatusEnum.SUCCESS.getCode(), cost, null));
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[QuartzJob] 任务执行失败 jobId={}", jobId, e);
            jobLogService.save(buildLog(job, "执行失败", JobLogStatusEnum.FAIL.getCode(), cost, e.getMessage()));
        }
    }

    /**
     * 构建执行日志实体
     *
     * @param job      任务实体
     * @param message  日志信息
     * @param status   执行状态（1成功 0失败）
     * @param costTime 耗时（毫秒）
     * @param errorMsg 错误信息（失败时记录）
     * @return 日志实体
     */
    private SysJobLog buildLog(SysJob job, String message, Integer status, long costTime, String errorMsg) {
        SysJobLog log = new SysJobLog();
        log.setJobId(job.getId());
        log.setJobName(job.getJobName());
        log.setJobGroup(job.getJobGroup());
        log.setInvokeTarget(job.getInvokeTarget());
        log.setJobMessage(message);
        log.setStatus(status);
        log.setCostTime(costTime);
        log.setErrorMsg(errorMsg);
        return log;
    }
}
