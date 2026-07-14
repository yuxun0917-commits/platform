package com.platform.service.service;

import com.platform.common.entity.admin.SysMenu;
import com.platform.common.entity.admin.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 通过id找到正常角色
     *
     * @param id 角色id
     * @return 角色实体
     */
    SysRole findById(Long id);

    Map<Long, SysRole> findPartMap(Set<Long> ids);

    /**
     * 找到角色的所有权限
     * @param ids   角色ids
     * @return  菜单权限集合
     */
    List<SysMenu> listMenuByIds(Set<Long> ids);

    /**
     * 分页查询角色
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数
     */
    void paging(Paging<SysRole> paging, Map<String, Object> paramsMap);

    /**
     * 角色选择列表（性能查询）
     *
     * <p>仅查询 id、角色名称两个字段，只返回未删除的正常状态角色。
     * 用于下拉选择等性能敏感场景。支持按角色名称模糊匹配。</p>
     *
     * @param paging    分页对象
     * @param keyword   模糊匹配关键词（匹配角色名称），可为null
     */
    void selectList(Paging<SysRole> paging, String keyword);
}
