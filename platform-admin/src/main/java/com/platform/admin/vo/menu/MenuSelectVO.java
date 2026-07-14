package com.platform.admin.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单选择列表 VO
 *
 * <p>用于下拉选择、树形选择等性能敏感场景，仅返回必要的2个字段：id、菜单名称。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "菜单选择列表")
public class MenuSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @Schema(description = "菜单ID")
    private Long id;

    /** 菜单名称 */
    @Schema(description = "菜单名称")
    private String menuName;
}
