package com.platform.service.mapper;

import com.platform.common.bo.LoginTrendBO;
import com.platform.common.entity.admin.SysLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 登录日志表 Mapper 接口
 * </p>
 *
 * @author platform
 * @since 2026-07-08
 */
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 近 7 天登录趋势（按日期分组统计每日登录次数）
     *
     * @return 每日登录次数列表（按日期升序）
     */
    List<LoginTrendBO> selectLoginTrend();
}
