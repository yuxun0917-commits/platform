package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.MenuStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysMenuMapper;
import com.platform.service.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 菜单权限表（兼容Vue动态路由） 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public SysMenu findById(Long id) {
        SysMenu sysMenu = lambdaQuery()
                .eq(SysMenu::getId, id)
                .eq(SysMenu::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(sysMenu, "无效的菜单id:{}", id);
        return sysMenu;
    }

    @Override
    public List<SysMenu> listTree() {
        return lambdaQuery()
                .eq(SysMenu::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .orderByAsc(SysMenu::getDisplayOrder)
                .orderByDesc(SysMenu::getId)
                .list();
    }

    @Override
    public void selectList(Paging<SysMenu> paging, String keyword) {
        lambdaQuery()
                .select(SysMenu::getId, SysMenu::getMenuName)
                .eq(SysMenu::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysMenu::getStatus, MenuStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysMenu::getMenuName, keyword)
                .orderByDesc(SysMenu::getId)
                .page(paging);
    }
}
