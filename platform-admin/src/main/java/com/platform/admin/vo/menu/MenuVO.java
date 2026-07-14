package com.platform.admin.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "菜单信息")
public class MenuVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @Schema(description = "菜单ID")
    private Long id;

    /** 父菜单ID（0=顶级） */
    @Schema(description = "父菜单ID")
    private Long parentId;

    /** 菜单名称 */
    @Schema(description = "菜单名称")
    private String menuName;

    /** 类型（1目录 2菜单 3按钮） */
    @Schema(description = "菜单类型")
    private Integer menuType;

    /** 路由路径 */
    @Schema(description = "路由路径")
    private String path;

    /** 组件路径 */
    @Schema(description = "组件路径")
    private String component;

    /** 重定向路径 */
    @Schema(description = "重定向路径")
    private String redirect;

    /** 菜单图标 */
    @Schema(description = "菜单图标")
    private String icon;

    /** 权限标识 */
    @Schema(description = "权限标识")
    private String perms;

    /** 排序 */
    @Schema(description = "排序")
    private Integer displayOrder;

    /** 是否隐藏（0显示 1隐藏） */
    @Schema(description = "是否隐藏")
    private Integer isHidden;

    /** 是否缓存（1缓存 0不缓存） */
    @Schema(description = "是否缓存")
    private Integer isCache;

    /** 是否外链（1是 0否） */
    @Schema(description = "是否外链")
    private Integer isExternal;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态")
    private Integer status;

    /** 状态描述（用于前端展示） */
    @Schema(description = "状态描述")
    private String statusText;

    /** 菜单类型描述（用于前端展示） */
    @Schema(description = "菜单类型描述")
    private String menuTypeText;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
