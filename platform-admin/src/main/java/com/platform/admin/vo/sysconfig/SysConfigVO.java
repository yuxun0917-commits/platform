package com.platform.admin.vo.sysconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "系统配置信息")
public class SysConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 参数主键ID */
    @Schema(description = "参数主键ID")
    private Long id;

    /** 参数名称 */
    @Schema(description = "参数名称")
    private String configName;

    /** 参数键名 */
    @Schema(description = "参数键名")
    private String configKey;

    /** 参数键值 */
    @Schema(description = "参数键值")
    private String configValue;

    /** 是否系统内置（1是 0否） */
    @Schema(description = "是否系统内置")
    private Integer configType;

    /** 是否系统内置描述 */
    @Schema(description = "是否系统内置描述")
    private String configTypeText;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
