package com.platform.admin.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位编辑入参 VO
 *
 * <p>在 PostSaveVO 基础上增加 id 字段，其他字段校验与 PostSaveVO 一致。
 * 字段长度依据 sys_post 表定义。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "岗位编辑参数")
public class PostEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 岗位ID */
    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "岗位ID不能为空")
    private Long id;

    /** 岗位编码（如：CEO、HR、DEV） */
    @Schema(description = "岗位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码长度不能超过64个字符")
    private String postCode;

    /** 岗位名称 */
    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 64, message = "岗位名称长度不能超过64个字符")
    private String postName;

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
