package com.platform.common.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情聚合业务对象
 *
 * <p>用于 /user/info 接口的层间数据传递与缓存载体：聚合指定用户的基本信息、
 * 角色列表、权限标识集合以及菜单扁平列表。</p>
 *
 * <p>说明：本对象位于 common 模块，供 component 层聚合数据、读写缓存使用；
 * admin 层拿到本对象后再转换为对外的展示 VO（跨模块边界，VO 不能下沉到 component）。
 * 为避免污染 entity 结构，聚合数据使用内部 BO（{@link UserBO}/{@link RoleBO}/{@link MenuBO}）
 * 承载，而非直接引用 entity；菜单以未删除的扁平列表形式承载，由 Controller 构建为树形结构。</p>
 *
 * @author platform
 */
@Data
public class UserInfoBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户基本信息（不含密码由展示层控制） */
    private UserBO user;

    /** 角色列表 */
    private List<RoleBO> roles;

    /** 权限标识集合（去重，来自角色关联的菜单 perms） */
    private List<String> permissions;

    /** 菜单扁平列表（未删除，供展示层构建树形结构） */
    private List<MenuBO> menus;

    /**
     * 用户基本信息 BO（字段镜像 {@code sys_user}，不含密码）
     */
    @Data
    public static class UserBO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private Integer gender;
        private LocalDate birthday;
        private String email;
        private String phone;
        private Long deptId;
        private String deptName;
        /** 岗位ID列表（逗号分隔，冗余，供展示层拆分） */
        private String postIds;
        /** 角色ID列表（逗号分隔，冗余，供展示层拆分） */
        private String roleIds;
        private String remark;
        private Integer status;
        private String loginIp;
        private LocalDateTime loginDate;
        private LocalDateTime createTime;
    }

    /**
     * 角色 BO（字段镜像 {@code sys_role}）
     */
    @Data
    public static class RoleBO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;
        private String roleName;
        private String roleCode;
        private Integer displayOrder;
        private Integer status;
        private String remark;
        private LocalDateTime createTime;
    }

    /**
     * 菜单 BO（字段镜像 {@code sys_menu}，扁平结构，不承载树形 children）
     */
    @Data
    public static class MenuBO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long parentId;
        private String menuName;
        private Integer menuType;
        private String path;
        private String component;
        private String redirect;
        private String icon;
        private String perms;
        private Integer displayOrder;
        private Integer isHidden;
        private Integer isCache;
        private Integer isExternal;
        private Integer status;
        /** 子菜单列表（树形构建用，非数据库字段） */
        private List<MenuBO> children;
    }
}
