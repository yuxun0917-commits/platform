package com.platform.admin.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户分配角色入参 VO
 *
 * <p>传入用户ID和角色ID列表，全量覆盖用户的角色关联。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "用户分配角色参数")
public class UserRoleAssignVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 角色ID列表 */
    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
