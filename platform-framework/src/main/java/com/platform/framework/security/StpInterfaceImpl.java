package com.platform.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.StrUtil;
import com.platform.common.constant.RedisConstant;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.entity.admin.SysRole;
import com.platform.service.service.*;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final RedisUtil redisUtil;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String key = RedisConstant.USER_AUTH_PERM + loginId;
        if (!redisUtil.hasKey(key)) {
            Long id = Long.parseLong(String.valueOf(loginId));
            List<SysRole> sysRoles = sysUserService.listRoleById(id);
            Set<Long> ids = sysRoles.stream()
                    .map(SysRole::getId)
                    .collect(Collectors.toSet());
            List<SysMenu> sysMenus = sysRoleService.listMenuByIds(ids);
            sysMenus.forEach(menu -> {
                if (StrUtil.isNotBlank(menu.getPerms())) {
                    redisUtil.hSet(key, String.valueOf(menu.getId()), menu.getPerms());
                }
            });
        }
        return listPorRInRedis(key);
    }

    @NonNull
    private List<String> listPorRInRedis(String key) {
        Map<Object, Object> permMap = redisUtil.hGetAll(key);
        return Optional.ofNullable(permMap)
                .orElse(Map.of()).values().stream()
                .map(String::valueOf)
                .toList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String key = RedisConstant.USER_AUTH_ROLE + loginId;
        if (!redisUtil.hasKey(key)) {
            Long id = Long.parseLong(String.valueOf(loginId));
            List<SysRole> sysRoles = sysUserService.listRoleById(id);
            sysRoles.forEach(role -> {
                redisUtil.hSet(key, String.valueOf(role.getId()), role.getRoleCode());
            });
        }
        return listPorRInRedis(key);
    }

}
