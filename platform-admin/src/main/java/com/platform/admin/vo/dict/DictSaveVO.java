package com.platform.admin.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典类型添加入参 VO
 *
 * <p>字段长度依据 sys_dict 表定义：
 * dict_name VARCHAR(64)、dict_type VARCHAR(128)、
 * status TINYINT、remark VARCHAR(255)</p>
 *
 * @author platform
 */
@Data
@Schema(description = "字典类型添加参数")
public class DictSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典名称（如：用户性别） */
    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称长度不能超过64个字符")
    private String dictName;

    /** 字典类型（如：sys_user_gender） */
    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 128, message = "字典类型长度不能超过128个字符")
    private String dictType;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
