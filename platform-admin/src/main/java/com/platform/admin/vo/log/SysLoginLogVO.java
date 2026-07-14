package com.platform.admin.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "登录日志信息")
public class SysLoginLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Schema(description = "日志ID")
    private Long id;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 登录类型（1登录 2登出 3踢下线） */
    @Schema(description = "登录类型")
    private Integer loginType;

    /** 登录类型描述 */
    @Schema(description = "登录类型描述")
    private String loginTypeText;

    /** 登录IP */
    @Schema(description = "登录IP")
    private String loginIp;

    /** 登录地点（IP归属地） */
    @Schema(description = "登录地点")
    private String loginLocation;

    /** 浏览器类型 */
    @Schema(description = "浏览器类型")
    private String browser;

    /** 操作系统 */
    @Schema(description = "操作系统")
    private String os;

    /** 登录状态（1成功 0失败） */
    @Schema(description = "登录状态")
    private Integer status;

    /** 状态描述 */
    @Schema(description = "状态描述")
    private String statusText;

    /** 错误消息（登录失败时记录） */
    @Schema(description = "错误消息")
    private String errorMsg;

    /** 登录时间 */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
