package com.platform.admin.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户 VO
 *
 * <p>返回当前登录用户的基本信息，不含敏感字段（密码）。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "用户返回对象")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 昵称 */
    @Schema(description = "用户昵称", example = "超级管理员")
    private String nickname;

    /** 头像附件ID（关联 sys_attachment.id，0=未设置） */
    @Schema(description = "头像附件ID（关联 sys_attachment.id，0=未设置）", example = "0")
    private Long avatarId;

    /** 头像预览地址 */
    @Schema(description = "头像预览地址")
    private String avatarPreviewUrl;

    /** 性别（0未知 1男 2女） */
    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    /** 出生日期 */
    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthday;

    /** 邮箱 */
    @Schema(description = "邮箱", example = "admin@platform.com")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号", example = "13800000001")
    private String phone;

    /** 部门ID */
    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    /** 部门名称 */
    @Schema(description = "部门名称", example = "技术研发部")
    private String deptName;

    /** 岗位ID列表 */
    @Schema(description = "岗位ID列表", example = "[1, 2, 3]")
    @JsonProperty("pIds")
    private Set<Long> pIds;

    /** 角色ID列表 */
    @Schema(description = "角色ID列表", example = "[1, 2, 3]")
    @JsonProperty("rIds")
    private Set<Long> rIds;

    /** 备注 */
    @Schema(description = "备注", example = "系统初始管理员")
    private String remark;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", example = "1")
    private Integer status;
    private String statusText;

    /** 最后登录IP */
    @Schema(description = "最后登录IP", example = "127.0.0.1")
    private String loginIp;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间", example = "2026-06-28T10:00:00")
    private LocalDateTime loginDate;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2026-06-28T10:00:00")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间", example = "2026-06-28T10:00:00")
    private LocalDateTime updateTime;
}
