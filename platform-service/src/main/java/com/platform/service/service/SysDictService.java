package com.platform.service.service;

import com.platform.common.entity.admin.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 字典类型表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 通过 id 查询未删除的字典类型
     *
     * @param id 字典id
     * @return 字典类型实体
     */
    SysDict findById(Long id);

    /**
     * 分页查询字典类型
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status 等）
     */
    void paging(Paging<SysDict> paging, Map<String, Object> paramsMap);

    /**
     * 字典类型选择列表（性能查询）
     *
     * <p>仅查询 id、字典名称两个字段，只返回未删除的正常状态字典类型。
     * 用于下拉选择等性能敏感场景。支持按字典名称模糊匹配。</p>
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词（匹配字典名称），可为null
     */
    void selectList(Paging<SysDict> paging, String keyword);

    /**
     * 通过字典类型编码查询字典类型
     *
     * @param dictType 字典类型编码（如 sys_user_gender）
     * @return 字典类型实体，不存在返回 null
     */
    SysDict findByDictType(String dictType);

}
