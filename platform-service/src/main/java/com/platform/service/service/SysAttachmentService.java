package com.platform.service.service;

import com.platform.common.entity.admin.SysAttachment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 附件表 服务类
 * </p>
 *
 * @author platform
 * @since 2026-07-14
 */
public interface SysAttachmentService extends IService<SysAttachment> {

    /**
     * 根据ID查询（含未删除校验）
     *
     * @param id 附件ID
     * @return 附件
     */
    SysAttachment findById(Long id);

    /**
     * 分页查询
     *
     * @param paging     分页对象
     * @param paramsMap  查询参数（keyword 文件名模糊，bizType 业务类型，configId 存储配置ID）
     */
    void paging(Paging<SysAttachment> paging, Map<String, Object> paramsMap);

    /**
     * 逻辑删除附件
     *
     * @param id 附件ID
     */
    void removeAttachment(Long id);

    /**
     * 统计某存储配置下的未删除附件数量
     *
     * @param configId 存储配置ID
     * @return 数量
     */
    long countByConfigId(Long configId);
}
