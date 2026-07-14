package com.platform.service.service;

import com.platform.common.entity.admin.SysLoginLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 登录日志表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-08
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、loginType、status）
     */
    void paging(Paging<SysLoginLog> paging, Map<String, Object> paramsMap);

    /**
     * 通过id查询登录日志详情
     *
     * @param id 日志id
     * @return 登录日志实体
     */
    SysLoginLog findById(Long id);

}
