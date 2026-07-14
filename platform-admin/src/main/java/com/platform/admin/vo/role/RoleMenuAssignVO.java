package com.platform.admin.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色分配权限入参 VO
 *
 * <p>传入角色ID和菜单ID列表，全量覆盖角色的权限关联。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "角色分配权限参数")
public class RoleMenuAssignVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 菜单ID列表 */
    @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> menuIds;
}
