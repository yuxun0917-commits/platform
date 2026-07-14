package com.platform.common.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 定时任务日志表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_job_log")
public class SysJobLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID（关联 sys_job.id）
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * 任务名称
     */
    @TableField("job_name")
    private String jobName;

    /**
     * 任务组名
     */
    @TableField("job_group")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @TableField("invoke_target")
    private String invokeTarget;

    /**
     * 日志信息
     */
    @TableField("job_message")
    private String jobMessage;

    /**
     * 执行状态（1成功 0失败）
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息（执行失败时记录）
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 耗时（毫秒）
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * 执行时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
