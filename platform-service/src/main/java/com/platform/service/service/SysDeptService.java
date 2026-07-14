package com.platform.service.service;

import com.platform.common.entity.admin.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.List;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-06
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 通过id找到正常部门
     *
     * @param id 部门id
     * @return 部门实体
     */
    SysDept findById(Long id);

    /**
     * 查询所有未删除部门（用于树形展示）
     *
     * <p>按 display_order 升序、id 倒序排序，返回扁平列表，
     * 由 Controller 层构建树形结构。</p>
     *
     * @return 部门列表
     */
    List<SysDept> listTree();

    /**
     * 部门选择列表（性能查询）
     *
     * <p>仅查询 id、部门名称两个字段，只返回未删除的正常状态部门。
     * 用于下拉选择等性能敏感场景。支持按部门名称模糊匹配。</p>
     *
     * @param paging    分页对象
     * @param keyword   模糊匹配关键词（匹配部门名称），可为null
     */
    void selectList(Paging<SysDept> paging, String keyword);

}
