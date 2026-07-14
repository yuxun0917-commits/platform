package com.platform.admin.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色选择列表 VO
 *
 * <p>用于下拉选择等性能敏感场景，仅返回必要的2个字段：id、角色名称。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "角色选择列表")
public class RoleSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Schema(description = "角色ID")
    private Long id;

    /** 角色名称 */
    @Schema(description = "角色名称")
    private String roleName;
}
