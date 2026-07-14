package com.platform.service.service;

import com.platform.common.entity.admin.SysPost;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 岗位表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
public interface SysPostService extends IService<SysPost> {

    /**
     * 通过 id 查询未删除的岗位
     *
     * @param id 岗位id
     * @return 岗位实体
     */
    SysPost findById(Long id);

    /**
     * 分页查询岗位
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status 等）
     */
    void paging(Paging<SysPost> paging, Map<String, Object> paramsMap);

    /**
     * 岗位选择列表（性能查询）
     *
     * <p>仅查询 id、岗位名称两个字段，只返回未删除的正常状态岗位。
     * 用于下拉选择等性能敏感场景。支持按岗位名称模糊匹配。</p>
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词（匹配岗位名称），可为null
     */
    void selectList(Paging<SysPost> paging, String keyword);

    /**
     * 通过岗位编码查询岗位
     *
     * @param postCode 岗位编码（如 CEO、HR、DEV）
     * @return 岗位实体，不存在返回 null
     */
    SysPost findByPostCode(String postCode);

    /**
     * 通过岗位ID查询岗位
     *
     * @param pIds 岗位集合
     * @return 岗位实体，不存在返回 null
     */
    Map<Long, SysPost> findPartMapByIds(Set<Long> pIds);
}
