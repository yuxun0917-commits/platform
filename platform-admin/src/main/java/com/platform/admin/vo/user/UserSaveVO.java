package com.platform.admin.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 用户添加入参 VO
 *
 * @author platform
 */
@Data
@Schema(description = "用户添加参数")
public class UserSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在3~32个字符之间")
    private String username;

    /** 昵称 */
    @Schema(description = "用户昵称", example = "张三")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 头像附件ID（关联 sys_attachment.id，0=未设置） */
    @Schema(description = "头像附件ID（关联 sys_attachment.id，0=未设置）", example = "0")
    @NotNull(message = "请上传头像")
    private Long avatarId;

    /** 性别（0未知 1男 2女） */
    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    /** 出生日期 */
    @Schema(description = "出生日期", example = "1995-06-15")
    private LocalDate birthday;

    /** 邮箱 */
    @Schema(description = "邮箱", example = "zhangsan@platform.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号", example = "13800000002")
    @Size(max = 11, message = "手机号长度不能超过11个字符")
    private String phone;

    /** 部门ID */
    @Schema(description = "部门ID", example = "1")
    @NotNull(message = "请选择部门")
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
    @Schema(description = "备注", example = "后端开发工程师")
    private String remark;
}
