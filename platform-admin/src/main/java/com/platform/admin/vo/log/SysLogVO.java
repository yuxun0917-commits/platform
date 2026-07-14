package com.platform.admin.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "操作日志信息")
public class SysLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Schema(description = "日志ID")
    private Long id;

    /** 模块标题（如：用户管理） */
    @Schema(description = "模块标题")
    private String title;

    /** 方法名称（类名.方法名） */
    @Schema(description = "方法名称")
    private String method;

    /** 请求URL */
    @Schema(description = "请求URL")
    private String requestUrl;

    /** 请求方式（GET/POST） */
    @Schema(description = "请求方式")
    private String requestMethod;

    /** 请求参数（JSON） */
    @Schema(description = "请求参数")
    private String requestParam;

    /** 返回结果（JSON） */
    @Schema(description = "返回结果")
    private String responseData;

    /** 操作人ID */
    @Schema(description = "操作人ID")
    private Long operId;

    /** 操作人名称 */
    @Schema(description = "操作人名称")
    private String operName;

    /** 操作人IP */
    @Schema(description = "操作人IP")
    private String operIp;

    /** 操作地点（IP归属地） */
    @Schema(description = "操作地点")
    private String operLocation;

    /** 浏览器类型 */
    @Schema(description = "浏览器类型")
    private String browser;

    /** 操作系统 */
    @Schema(description = "操作系统")
    private String os;

    /** 操作状态（1正常 0异常） */
    @Schema(description = "操作状态")
    private Integer status;

    /** 状态描述 */
    @Schema(description = "状态描述")
    private String statusText;

    /** 错误消息（操作异常时记录） */
    @Schema(description = "错误消息")
    private String errorMsg;

    /** 耗时（毫秒） */
    @Schema(description = "耗时（毫秒）")
    private Long costTime;

    /** 操作时间 */
    @Schema(description = "操作时间")
    private LocalDateTime operTime;
}
