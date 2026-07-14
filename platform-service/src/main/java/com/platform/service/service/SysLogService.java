package com.platform.service.service;

import com.platform.common.entity.admin.SysLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 操作日志表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-08
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 分页查询操作日志
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status）
     */
    void paging(Paging<SysLog> paging, Map<String, Object> paramsMap);

    /**
     * 通过id查询操作日志详情
     *
     * @param id 日志id
     * @return 操作日志实体
     */
    SysLog findById(Long id);

}
