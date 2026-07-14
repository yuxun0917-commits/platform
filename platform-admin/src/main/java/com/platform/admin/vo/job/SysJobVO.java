package com.platform.admin.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "定时任务信息")
public class SysJobVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @Schema(description = "任务ID")
    private Long id;

    /** 任务名称 */
    @Schema(description = "任务名称")
    private String jobName;

    /** 任务组名 */
    @Schema(description = "任务组名")
    private String jobGroup;

    /** 调用目标字符串 */
    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    /** cron执行表达式 */
    @Schema(description = "cron执行表达式")
    private String cronExpression;

    /** 错失触发策略（0不补跑 1补跑） */
    @Schema(description = "错失触发策略")
    private Integer misfirePolicy;

    /** 错失触发策略描述 */
    @Schema(description = "错失触发策略描述")
    private String misfirePolicyText;

    /** 是否并发（0禁止 1允许） */
    @Schema(description = "是否并发")
    private Integer concurrent;

    /** 是否并发描述 */
    @Schema(description = "是否并发描述")
    private String concurrentText;

    /** 状态（1正常 0暂停） */
    @Schema(description = "状态")
    private Integer status;

    /** 状态描述 */
    @Schema(description = "状态描述")
    private String statusText;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
