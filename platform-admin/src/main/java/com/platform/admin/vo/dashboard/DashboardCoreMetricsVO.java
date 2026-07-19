package com.platform.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘核心指标展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "仪表盘核心指标")
public class DashboardCoreMetricsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "本周新增用户")
    private Long weekNewUser;

    @Schema(description = "今日登录")
    private Long todayLogin;

    @Schema(description = "在线用户")
    private Integer onlineUser;

    @Schema(description = "系统公告数")
    private Long noticeCount;
}
