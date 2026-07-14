package com.platform.admin.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色编辑入参 VO
 *
 * <p>仅允许编辑角色基本信息：名称、标识、排序、状态、备注。
 * 字段长度依据 rbac.sql 中 role 表定义。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "角色编辑参数")
public class RoleEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long id;

    /** 角色名称 */
    @Schema(description = "角色名称")
    @Size(max = 64, message = "角色名称长度不能超过64个字符")
    private String roleName;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
