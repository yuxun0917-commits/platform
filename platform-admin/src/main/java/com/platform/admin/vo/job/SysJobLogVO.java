package com.platform.admin.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务日志展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "定时任务日志信息")
public class SysJobLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Schema(description = "日志ID")
    private Long id;

    /** 任务ID（关联 sys_job.id） */
    @Schema(description = "任务ID")
    private Long jobId;

    /** 任务名称 */
    @Schema(description = "任务名称")
    private String jobName;

    /** 任务组名 */
    @Schema(description = "任务组名")
    private String jobGroup;

    /** 调用目标字符串 */
    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    /** 日志信息 */
    @Schema(description = "日志信息")
    private String jobMessage;

    /** 执行状态（1成功 0失败） */
    @Schema(description = "执行状态")
    private Integer status;

    /** 执行状态描述 */
    @Schema(description = "执行状态描述")
    private String statusText;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 耗时（毫秒） */
    @Schema(description = "耗时（毫秒）")
    private Long costTime;

    /** 执行时间 */
    @Schema(description = "执行时间")
    private LocalDateTime createTime;
}
