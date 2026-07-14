package com.platform.common.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 菜单权限表（兼容Vue动态路由）
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID（0=顶级）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单名称（显示在侧边栏）
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 类型（1目录 2菜单 3按钮）
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 路由路径（Vue Router path）
     */
    @TableField("path")
    private String path;

    /**
     * 组件路径（Vue component，如 system/user/index）
     */
    @TableField("component")
    private String component;

    /**
     * 重定向路径
     */
    @TableField("redirect")
    private String redirect;

    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 权限标识（如 system:user:list）
     */
    @TableField("perms")
    private String perms;

    /**
     * 排序（升序）
     */
    @TableField("display_order")
    private Integer displayOrder;

    /**
     * 是否隐藏（0显示 1隐藏）
     */
    @TableField("is_hidden")
    private Integer isHidden;

    /**
     * 是否缓存（1缓存 0不缓存）
     */
    @TableField("is_cache")
    private Integer isCache;

    /**
     * 是否外链（1是 0否）
     */
    @TableField("is_external")
    private Integer isExternal;

    /**
     * 状态（1正常 0禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除（0未删除 1已删除）
     */
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 创建人（0=系统操作）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（0=系统操作）
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
