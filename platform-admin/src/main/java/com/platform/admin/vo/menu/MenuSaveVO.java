package com.platform.admin.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单添加入参 VO
 *
 * <p>字段长度依据 rbac.sql 中 menu 表定义：
 * menu_name VARCHAR(64)、path VARCHAR(255)、component VARCHAR(255)、
 * redirect VARCHAR(255)、icon VARCHAR(64)、perms VARCHAR(128)</p>
 *
 * @author platform
 */
@Data
@Schema(description = "菜单添加参数")
public class MenuSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父菜单ID（0=顶级） */
    @Schema(description = "父菜单ID（0=顶级）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /** 菜单名称（显示在侧边栏） */
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过64个字符")
    private String menuName;

    /** 类型（1目录 2菜单 3按钮） */
    @Schema(description = "类型（1目录 2菜单 3按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    /** 路由路径（Vue Router path） */
    @Schema(description = "路由路径", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "路由路径不能为空")
    @Size(max = 255, message = "路由路径长度不能超过255个字符")
    private String path;

    /** 组件路径（Vue component，如 system/user/index） */
    @Schema(description = "组件路径")
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    /** 重定向路径 */
    @Schema(description = "重定向路径")
    @Size(max = 255, message = "重定向路径长度不能超过255个字符")
    private String redirect;

    /** 菜单图标 */
    @Schema(description = "菜单图标")
    @Size(max = 64, message = "菜单图标长度不能超过64个字符")
    private String icon;

    /** 权限标识（如 system:user:list） */
    @Schema(description = "权限标识")
    @Size(max = 128, message = "权限标识长度不能超过128个字符")
    private String perms;

    /** 排序（升序） */
    @Schema(description = "排序（升序）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer displayOrder;

    /** 是否隐藏（0显示 1隐藏） */
    @Schema(description = "是否隐藏（0显示 1隐藏）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否隐藏不能为空")
    private Integer isHidden;

    /** 是否缓存（1缓存 0不缓存） */
    @Schema(description = "是否缓存（1缓存 0不缓存）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否缓存不能为空")
    private Integer isCache;

    /** 是否外链（1是 0否） */
    @Schema(description = "是否外链（1是 0否）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否外链不能为空")
    private Integer isExternal;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
