package com.platform.common.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘运维概览聚合对象
 *
 * <p>承载首页运维概览（登录失败、操作异常、任务失败、暂停任务），
 * 由 {@code DashboardComponent} 聚合后返回，供 admin 层转 VO。</p>
 *
 * @author platform
 */
@Data
public class DashboardOpsBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 今日登录失败数（status=0 且 login_time >= 今日 00:00） */
    private Long loginFail;

    /** 操作异常数（sys_log.status=0） */
    private Long operateException;

    /** 今日定时任务失败数（status=0 且 create_time >= 今日 00:00） */
    private Long jobFail;

    /** 暂停任务数（sys_job.status=0） */
    private Long jobPause;
}
