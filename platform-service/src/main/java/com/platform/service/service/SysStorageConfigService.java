package com.platform.service.service;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.result.Paging;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 文件存储配置表 服务类
 * </p>
 *
 * @author platform
 * @since 2026-07-14
 */
public interface SysStorageConfigService extends IService<SysStorageConfig> {

    /**
     * 根据ID查询（含未删除校验）
     *
     * @param id 配置ID
     * @return 存储配置
     */
    SysStorageConfig findById(Long id);

    /**
     * 查询当前默认存储配置（status=1 且 is_default=1）
     *
     * @return 默认存储配置
     */
    SysStorageConfig findDefault();

    /**
     * 设为默认存储（事务：清掉其它默认标记，仅保留当前一条）
     *
     * @param id 配置ID
     */
    void setDefault(Long id);

    /**
     * 分页查询
     *
     * @param paging     分页对象
     * @param paramsMap  查询参数（keyword 模糊匹配配置名称，status 状态筛选）
     */
    void paging(Paging<SysStorageConfig> paging, Map<String, Object> paramsMap);

    /**
     * 选择列表（仅返回 id + 配置名称，启用状态）
     *
     * @param paging   分页对象
     * @param keyword  模糊匹配配置名称
     */
    void selectList(Paging<SysStorageConfig> paging, String keyword);
}
