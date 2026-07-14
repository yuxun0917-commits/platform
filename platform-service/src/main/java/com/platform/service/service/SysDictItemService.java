package com.platform.service.service;

import com.platform.common.entity.admin.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典项表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 通过 id 查询未删除的字典项
     *
     * @param id 字典项id
     * @return 字典项实体
     */
    SysDictItem findById(Long id);

    /**
     * 分页查询字典项
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（dictId、status、keyword 等）
     */
    void paging(Paging<SysDictItem> paging, Map<String, Object> paramsMap);

    /**
     * 根据字典类型编码查询全部正常状态的字典项
     *
     * <p>用于前端下拉选择，按 display_order 升序返回。</p>
     *
     * @param dictType 字典类型编码
     * @return 字典项列表
     */
    List<SysDictItem> listByDictType(String dictType);

    /**
     * 根据字典类型ID查询全部正常状态的字典项
     *
     * @param dictId 字典类型ID
     * @return 字典项列表
     */
    List<SysDictItem> listByDictId(Long dictId);

}
