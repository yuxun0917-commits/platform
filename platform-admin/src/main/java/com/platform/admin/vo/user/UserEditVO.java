package com.platform.admin.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 用户基本信息编辑 VO
 *
 * <p>仅允许编辑用户基本信息，不包括手机号、邮箱、密码、状态。
 * 可编辑字段：昵称、头像、性别、出生日期、部门、岗位、角色、备注。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "用户基本信息编辑参数")
public class UserEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String nickname;

    /** 头像附件ID（关联 sys_attachment.id，0=未设置） */
    @Schema(description = "头像附件ID（关联 sys_attachment.id，0=未设置）")
    @NotNull(message = "请上传头像")
    private Long avatarId;

    /** 性别（0未知 1男 2女） */
    @Schema(description = "性别（0未知 1男 2女）")
    private Integer gender;

    /** 出生日期 */
    @Schema(description = "出生日期")
    private LocalDate birthday;

    /** 部门ID */
    @Schema(description = "部门ID")
    private Long deptId;

    /** 部门名称（仅内部使用，不展示在文档中） */
    @Schema(hidden = true)
    private String deptName;

    /** 岗位ID列表 */
    @Schema(description = "岗位ID列表", example = "[1, 2, 3]")
    @JsonProperty("pIds")
    private Set<Long> pIds;

    /** 岗位ID 仅内部使用 */
    @Schema(hidden = true)
    private String postIds;

    /** 角色ID列表 */
    @Schema(description = "角色ID列表", example = "[1, 2, 3]")
    @JsonProperty("rIds")
    private Set<Long> rIds;

    /** 角色ID 仅内部使用 */
    @Schema(hidden = true)
    private String roleIds;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
