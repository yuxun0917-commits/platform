package com.platform.admin.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 定时任务编辑入参 VO
 *
 * <p>在 SysJobSaveVO 基础上增加 id 字段，其他字段校验与 SysJobSaveVO 一致。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "定时任务编辑参数")
public class SysJobEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /** 任务名称 */
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称长度不能超过64个字符")
    private String jobName;

    /** 任务组名 */
    @Schema(description = "任务组名")
    @Size(max = 64, message = "任务组名长度不能超过64个字符")
    private String jobGroup;

    /** 调用目标字符串 */
    @Schema(description = "调用目标字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调用目标不能为空")
    @Size(max = 255, message = "调用目标长度不能超过255个字符")
    private String invokeTarget;

    /** cron执行表达式 */
    @Schema(description = "cron执行表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cron表达式不能为空")
    @Size(max = 128, message = "cron表达式长度不能超过128个字符")
    private String cronExpression;

    /** 错失触发策略（0不补跑 1补跑） */
    @Schema(description = "错失触发策略（0不补跑 1补跑）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "错失触发策略不能为空")
    private Integer misfirePolicy;

    /** 是否并发（0禁止 1允许） */
    @Schema(description = "是否并发（0禁止 1允许）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否并发不能为空")
    private Integer concurrent;

    /** 状态（1正常 0暂停） */
    @Schema(description = "状态（1正常 0暂停）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
