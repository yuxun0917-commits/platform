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
 * 登录日志表
 * </p>
 *
 * @author platform
 * @since 2026-07-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录类型（1登录 2登出 3踢下线）
     */
    @TableField("login_type")
    private Integer loginType;

    /**
     * 登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录地点（IP归属地）
     */
    @TableField("login_location")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 登录状态（1成功 0失败）
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误消息（登录失败时记录）
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
}
