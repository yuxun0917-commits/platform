package com.platform.service.service;

import com.platform.common.entity.admin.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.List;

/**
 * <p>
 * 菜单权限表（兼容Vue动态路由） 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 通过id找到正常菜单
     *
     * @param id 菜单id
     * @return 菜单实体
     */
    SysMenu findById(Long id);

    /**
     * 查询所有未删除菜单（用于树形展示）
     *
     * <p>按 display_order 升序、id 倒序排序，返回扁平列表，
     * 由 Controller 层构建树形结构。</p>
     *
     * @return 菜单列表
     */
    List<SysMenu> listTree();

    /**
     * 菜单选择列表（性能查询）
     *
     * <p>仅查询 id、菜单名称两个字段，只返回未删除的正常状态菜单。
     * 用于下拉选择等性能敏感场景。支持按菜单名称模糊匹配。</p>
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词（匹配菜单名称），可为null
     */
    void selectList(Paging<SysMenu> paging, String keyword);
}
