package com.platform.common.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 登录趋势聚合对象
 *
 * <p>承载近 7 天每日登录次数（按 {@code DATE(login_time)} 分组统计），
 * 由 {@code SysLoginLogMapper.selectLoginTrend} 查询后返回，供 admin 层转 VO。</p>
 *
 * @author platform
 */
@Data
public class LoginTrendBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录日期 */
    private LocalDate loginDate;

    /** 当日登录次数 */
    private Long count;
}
