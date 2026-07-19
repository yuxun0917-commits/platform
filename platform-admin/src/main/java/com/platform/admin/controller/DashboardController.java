package com.platform.admin.controller;

import com.platform.admin.vo.dashboard.DashboardCoreMetricsVO;
import com.platform.admin.vo.dashboard.DashboardHealthVO;
import com.platform.admin.vo.dashboard.DashboardOpsVO;
import com.platform.admin.vo.dashboard.LoginTrendItemVO;
import com.platform.admin.vo.log.SysLoginLogVO;
import com.platform.admin.vo.notice.NoticeVO;
import com.platform.common.bo.DashboardHealthBO;
import com.platform.common.bo.DashboardOpsBO;
import com.platform.common.bo.LoginTrendBO;
import com.platform.common.entity.admin.SysLoginLog;
import com.platform.common.entity.admin.SysNotice;
import com.platform.common.enums.LoginLogStatusEnum;
import com.platform.common.enums.LoginTypeEnum;
import com.platform.common.enums.SysNoticePositionEnum;
import com.platform.common.enums.SysNoticeStatusEnum;
import com.platform.common.result.Result;
import com.platform.component.dashboard.DashboardComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器
 *
 * <p>首页（仪表盘）各区块数据接口，每个区块一个接口，供前端分块加载/刷新。</p>
 *
 * <p>接口清单（均受 {@code monitor:dashboard:list} 权限保护）：</p>
 * <ul>
 *   <li>GET /dashboard/core-metrics  - 核心指标（本周新增/今日登录/在线用户/公告数）</li>
 *   <li>GET /dashboard/health        - 服务器健康度（CPU/内存/JVM/磁盘/Redis）</li>
 *   <li>GET /dashboard/login-trend   - 近 7 天登录趋势</li>
 *   <li>GET /dashboard/ops-overview  - 运维概览（登录失败/操作异常/任务失败/暂停任务）</li>
 *   <li>GET /dashboard/recent-logins - 最近登录</li>
 *   <li>GET /dashboard/notices       - 最新公告</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "仪表盘")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardComponent dashboardComponent;
    private final MapperFacade mapperFacade;

    /**
     * 核心指标
     */
    @Operation(summary = "仪表盘-核心指标")
    @GetMapping("/core-metrics")
    public Result coreMetrics() {
        return Result.success(mapperFacade.map(dashboardComponent.getCoreMetrics(), DashboardCoreMetricsVO.class));
    }

    /**
     * 服务器健康度
     */
    @Operation(summary = "仪表盘-服务器健康度")
    @GetMapping("/health")
    public Result health() {
        DashboardHealthBO healthBO = dashboardComponent.getHealth();
        return Result.success(mapperFacade.map(healthBO, DashboardHealthVO.class));
    }

    /**
     * 近 7 天登录趋势
     */
    @Operation(summary = "仪表盘-登录趋势")
    @GetMapping("/login-trend")
    public Result loginTrend() {
        List<LoginTrendBO> list = dashboardComponent.getLoginTrend();
        List<LoginTrendItemVO> voList = list.stream()
                .map(bo -> mapperFacade.map(bo, LoginTrendItemVO.class))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 运维概览
     */
    @Operation(summary = "仪表盘-运维概览")
    @GetMapping("/ops-overview")
    public Result opsOverview() {
        DashboardOpsBO opsBO = dashboardComponent.getOpsOverview();
        return Result.success(mapperFacade.map(opsBO, DashboardOpsVO.class));
    }

    /**
     * 最近登录
     */
    @Operation(summary = "仪表盘-最近登录")
    @GetMapping("/recent-logins")
    public Result recentLogins() {
        List<SysLoginLog> list = dashboardComponent.getRecentLogins();
        List<SysLoginLogVO> voList = list.stream().map(entity -> {
            SysLoginLogVO vo = mapperFacade.map(entity, SysLoginLogVO.class);
            vo.setLoginTypeText(LoginTypeEnum.getDescByCode(entity.getLoginType()));
            vo.setStatusText(LoginLogStatusEnum.getDescByCode(entity.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 最新公告
     */
    @Operation(summary = "仪表盘-最新公告")
    @GetMapping("/notices")
    public Result notices() {
        List<SysNotice> list = dashboardComponent.getNotices();
        List<NoticeVO> voList = list.stream().map(entity -> {
            NoticeVO vo = mapperFacade.map(entity, NoticeVO.class);
            vo.setPositionText(SysNoticePositionEnum.getByCode(entity.getPosition()).getDesc());
            vo.setStatusText(SysNoticeStatusEnum.getDescByCode(entity.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }
}
