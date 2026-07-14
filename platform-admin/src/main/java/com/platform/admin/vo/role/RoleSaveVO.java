package com.platform.admin.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色添加入参 VO
 *
 * <p>字段长度依据 rbac.sql 中 role 表定义：
 * role_name VARCHAR(64)、role_code VARCHAR(64)、remark VARCHAR(255)</p>
 *
 * @author platform
 */
@Data
@Schema(description = "角色添加参数")
public class RoleSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64个字符")
    private String roleName;

    /** 角色标识（如 admin/user/operator） */
    @Schema(description = "角色标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 64, message = "角色标识长度不能超过64个字符")
    private String roleCode;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
