package com.platform.common.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘核心指标聚合对象
 *
 * <p>承载首页核心指标（本周新增用户、今日登录、在线用户、系统公告数），
 * 由 {@code DashboardComponent} 跨多 Service 聚合后返回，供 admin 层转 VO。</p>
 *
 * @author platform
 */
@Data
public class DashboardMetricsBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 本周新增用户（create_time >= 本周一） */
    private Long weekNewUser;

    /** 今日登录（login_time >= 今日 00:00） */
    private Long todayLogin;

    /** 在线用户数（Sa-Token 在线会话数） */
    private Integer onlineUser;

    /** 系统公告数（status=1 且未删除） */
    private Long noticeCount;
}
