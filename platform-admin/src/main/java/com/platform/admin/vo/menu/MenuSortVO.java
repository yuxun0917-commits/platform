package com.platform.admin.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单排序入参 VO
 *
 * <p>仅支持<b>同级排序</b>：传入的所有菜单必须属于同一个父级（parentId）。
 * 前端拖拽调整同一层级内菜单的先后顺序后，传入该层级的 parentId 与菜单ID列表（按排序顺序），
 * 后端按 index + 1 依次为同级菜单赋值 displayOrder。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "菜单排序参数（仅同级）")
public class MenuSortVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父菜单ID（0=顶级）；用于校验所有菜单同级 */
    @Schema(description = "父菜单ID（0=顶级）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /** 菜单ID列表（按排序顺序排列，必须同属 parentId 层级） */
    @Schema(description = "菜单ID列表（按排序顺序，需同级）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> ids;
}
