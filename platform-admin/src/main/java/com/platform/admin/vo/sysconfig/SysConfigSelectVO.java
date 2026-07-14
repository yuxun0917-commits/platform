package com.platform.admin.vo.sysconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置选择列表 VO
 *
 * <p>用于下拉选择等性能敏感场景，仅返回必要的2个字段：id、参数名称。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "系统配置选择列表")
public class SysConfigSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 参数主键ID */
    @Schema(description = "参数主键ID")
    private Long id;

    /** 参数名称 */
    @Schema(description = "参数名称")
    private String configName;
}
