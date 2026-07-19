package com.platform.admin.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门编辑入参 VO
 *
 * <p>在 DeptSaveVO 基础上增加 id 字段，其他字段校验与 DeptSaveVO 一致。
 * 字段长度依据 dept 表定义。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "部门编辑参数")
public class DeptEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门ID不能为空")
    private Long id;

    /** 父部门ID（0表示顶级部门） */
    @Schema(description = "父部门ID（0表示顶级部门）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    /** 部门名称 */
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称长度不能超过64个字符")
    private String deptName;

    /** 负责人 */
    @Schema(description = "负责人")
    @Size(max = 64, message = "负责人长度不能超过64个字符")
    private String leader;

    /** 联系电话 */
    @Schema(description = "联系电话")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
