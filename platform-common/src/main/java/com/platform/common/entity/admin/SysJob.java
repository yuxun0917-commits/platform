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
 * 定时任务表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_job")
public class SysJob implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 调用目标字符串（如：ryTask.ryParams('ry')）
     */
    @TableField("invoke_target")
    private String invokeTarget;

    /**
     * cron执行表达式（如：0/10 * * * * ?）
     */
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * 错失触发策略(0不补跑,1补跑)
     */
    @TableField("misfire_policy")
    private Integer misfirePolicy;

    /**
     * 是否并发执行（1允许 0禁止）
     */
    @TableField("concurrent")
    private Integer concurrent;

    /**
     * 状态（1正常 0暂停）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除（0未删除 1已删除）
     */
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 创建人（0=系统操作）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（0=系统操作）
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
