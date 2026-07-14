package com.platform.admin.vo.user;

import com.platform.admin.vo.menu.MenuTreeVO;
import com.platform.admin.vo.role.RoleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户详情聚合 VO
 *
 * <p>用于 /user/info 接口，聚合展示指定用户的基本信息、角色、权限与菜单树。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "用户详情（基本信息+角色+权限+菜单）")
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户基本信息（不含密码） */
    @Schema(description = "用户基本信息")
    private UserVO user;

    /** 角色列表 */
    @Schema(description = "角色列表")
    private List<RoleVO> roles;

    /** 权限标识集合（去重，来自角色关联的菜单 perms） */
    @Schema(description = "权限标识集合")
    private List<String> permissions;

    /** 菜单树（按角色关联的菜单递归构建） */
    @Schema(description = "菜单树")
    private List<MenuTreeVO> menus;
}
