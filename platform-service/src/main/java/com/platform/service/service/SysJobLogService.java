package com.platform.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.entity.admin.SysJobLog;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 定时任务日志表 服务类
 * </p>
 *
 * @author platform
 * @since 2026-07-13
 */
public interface SysJobLogService extends IService<SysJobLog> {

    /**
     * 根据ID查询日志详情
     *
     * @param id 日志ID
     * @return 日志实体
     */
    SysJobLog findById(Long id);

    /**
     * 分页查询任务日志
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status）
     */
    void paging(Paging<SysJobLog> paging, Map<String, Object> paramsMap);

    /**
     * 清空全部任务日志
     */
    void clean();
}
