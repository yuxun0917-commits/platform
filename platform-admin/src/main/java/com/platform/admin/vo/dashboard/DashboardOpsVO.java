package com.platform.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘运维概览展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "仪表盘运维概览")
public class DashboardOpsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录失败")
    private Long loginFail;

    @Schema(description = "操作异常")
    private Long operateException;

    @Schema(description = "任务失败")
    private Long jobFail;

    @Schema(description = "暂停任务")
    private Long jobPause;
}
