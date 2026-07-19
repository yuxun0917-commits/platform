package com.platform.admin.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "角色信息")
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Schema(description = "角色ID")
    private Long id;

    /** 角色名称 */
    @Schema(description = "角色名称")
    private String roleName;

    /** 角色标识 */
    @Schema(description = "角色标识")
    private String roleCode;

    /** 排序 */
    @Schema(description = "排序")
    private Integer displayOrder;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态")
    private Integer status;

    /** 状态描述 */
    @Schema(description = "状态描述")
    private String statusText;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间", example = "2026-06-28T10:00:00")
    private LocalDateTime updateTime;
}
