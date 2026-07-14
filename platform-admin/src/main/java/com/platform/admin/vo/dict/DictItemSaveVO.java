package com.platform.admin.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典项添加入参 VO
 *
 * <p>字段长度依据 sys_dict_item 表定义：
 * dict_id BIGINT、dict_type VARCHAR(128)、dict_label VARCHAR(128)、
 * dict_value VARCHAR(128)、css_class VARCHAR(128)、display_order INT、
 * status TINYINT、remark VARCHAR(255)</p>
 *
 * @author platform
 */
@Data
@Schema(description = "字典项添加参数")
public class DictItemSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典类型ID（关联 sys_dict.id） */
    @Schema(description = "字典类型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典类型ID不能为空")
    private Long dictId;

    /** 字典类型（冗余，避免联查） */
    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 128, message = "字典类型长度不能超过128个字符")
    private String dictType;

    /** 字典标签（如：男） */
    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签长度不能超过128个字符")
    private String dictLabel;

    /** 字典键值（如：1） */
    @Schema(description = "字典键值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典键值不能为空")
    @Size(max = 128, message = "字典键值长度不能超过128个字符")
    private String dictValue;

    /** 样式属性（如：danger/primary，前端标签颜色） */
    @Schema(description = "样式属性")
    @Size(max = 128, message = "样式属性长度不能超过128个字符")
    private String cssClass;

    /** 排序（升序） */
    @Schema(description = "排序（升序）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer displayOrder;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
