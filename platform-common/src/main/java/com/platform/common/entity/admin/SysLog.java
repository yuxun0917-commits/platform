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
 * 操作日志表
 * </p>
 *
 * @author platform
 * @since 2026-07-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_log")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模块标题（如：用户管理）
     */
    @TableField("title")
    private String title;

    /**
     * 方法名称（类名.方法名）
     */
    @TableField("method")
    private String method;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方式（GET/POST）
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数（JSON）
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 返回结果（JSON）
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 操作人ID
     */
    @TableField("oper_id")
    private Long operId;

    /**
     * 操作人名称
     */
    @TableField("oper_name")
    private String operName;

    /**
     * 操作人IP
     */
    @TableField("oper_ip")
    private String operIp;

    /**
     * 操作地点（IP归属地）
     */
    @TableField("oper_location")
    private String operLocation;

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
     * 操作状态（1正常 0异常）
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误消息（操作异常时记录）
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 耗时（毫秒）
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * 操作时间
     */
    @TableField("oper_time")
    private LocalDateTime operTime;
}
