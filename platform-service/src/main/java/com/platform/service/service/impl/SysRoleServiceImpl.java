package com.platform.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.entity.admin.SysRole;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.RoleStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysRoleMapper;
import com.platform.service.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public SysRole findById(Long id) {
        SysRole sysRole = lambdaQuery()
                .eq(SysRole::getId, id)
                .eq(SysRole::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(sysRole, "无效的角色id:{}", id);
        return sysRole;
    }

    @Override
    public Map<Long, SysRole> findPartMap(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .select(SysRole::getId, SysRole::getRoleName, SysRole::getRoleCode)
                .in(SysRole::getId, ids)
                .eq(SysRole::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .list().stream()
                .collect(Collectors.toMap(SysRole::getId, o -> o));
    }

    @Override
    public List<SysMenu> listMenuByIds(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return sysRoleMapper.listMenuByIds(ids);
    }

    @Override
    public void paging(Paging<SysRole> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysRole::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysRole::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysRole::getRoleName, keyword)
                                .or()
                                .like(SysRole::getRoleCode, keyword))
                .orderByAsc(SysRole::getDisplayOrder)
                .orderByDesc(SysRole::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysRole> paging, String keyword) {
        lambdaQuery()
                .select(SysRole::getId, SysRole::getRoleName)
                .eq(SysRole::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysRole::getStatus, RoleStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysRole::getRoleName, keyword)
                .orderByDesc(SysRole::getId)
                .page(paging);
    }
}
