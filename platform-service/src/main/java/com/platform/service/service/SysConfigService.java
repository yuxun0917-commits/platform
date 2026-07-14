package com.platform.service.service;

import com.platform.common.entity.admin.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 系统配置表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 通过id找到正常配置
     *
     * @param id 配置id
     * @return 配置实体
     */
    SysConfig findById(Long id);

    /**
     * 分页查询系统配置
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、configType 等）
     */
    void paging(Paging<SysConfig> paging, Map<String, Object> paramsMap);

    /**
     * 系统配置选择列表（性能查询）
     *
     * <p>仅查询 id、配置名称两个字段，只返回未删除的正常状态配置。
     * 用于下拉选择等性能敏感场景。支持按配置名称模糊匹配。</p>
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词（匹配配置名称），可为null
     */
    void selectList(Paging<SysConfig> paging, String keyword);

    /**
     * 通过配置键名查询配置值
     *
     * @param configKey 配置键名
     * @return 配置实体
     */
    SysConfig findByKey(String configKey);

}
