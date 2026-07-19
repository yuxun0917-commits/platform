package com.platform.component.dashboard;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.common.bo.DashboardHealthBO;
import com.platform.common.bo.DashboardMetricsBO;
import com.platform.common.bo.DashboardOpsBO;
import com.platform.common.bo.LoginTrendBO;
import com.platform.common.entity.admin.SysJob;
import com.platform.common.entity.admin.SysJobLog;
import com.platform.common.entity.admin.SysLoginLog;
import com.platform.common.entity.admin.SysLog;
import com.platform.common.entity.admin.SysNotice;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.JobLogStatusEnum;
import com.platform.common.enums.JobStatusEnum;
import com.platform.common.enums.LoginLogStatusEnum;
import com.platform.common.enums.OperLogStatusEnum;
import com.platform.common.enums.SysNoticeStatusEnum;
import com.platform.service.mapper.SysLoginLogMapper;
import com.platform.service.service.SysJobLogService;
import com.platform.service.service.SysJobService;
import com.platform.service.service.SysLoginLogService;
import com.platform.service.service.SysLogService;
import com.platform.service.service.SysNoticeService;
import com.platform.service.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘聚合组件
 *
 * <p>跨多个 Service 与 Sa-Token 聚合首页所需的各类数据，
 * Controller 层仅负责把这里返回的 BO / 实体转换为 VO，符合「Component 聚合、Controller 只转 VO」的分层约定。</p>
 *
 * <p>健康度（{@link #getHealth()}）使用 OSHI 独立采集，不依赖系统监控 {@code ServerComponent}，
 * 返回仪表盘专属的精简健康度结构。</p>
 *
 * @author platform
 */
@Component
@RequiredArgsConstructor
public class DashboardComponent {

    private final SysUserService userService;
    private final SysLoginLogService loginLogService;
    private final SysNoticeService noticeService;
    private final SysLogService sysLogService;
    private final SysJobService jobService;
    private final SysJobLogService jobLogService;
    private final SysLoginLogMapper loginLogMapper;

    /** CPU 负载采样等待时间（毫秒） */
    private static final long CPU_SAMPLE_INTERVAL = 1000L;

    /**
     * 核心指标：本周新增用户、今日登录、在线用户、系统公告数
     */
    public DashboardMetricsBO getCoreMetrics() {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long weekNewUser = userService.lambdaQuery()
                .ge(SysUser::getCreateTime, weekStart)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .count();
        Long todayLogin = loginLogService.lambdaQuery()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .count();
        List<String> onlineSessions = StpUtil.searchSessionId("", 0, -1, false);
        Integer onlineUser = onlineSessions == null ? 0 : onlineSessions.size();
        Long noticeCount = noticeService.lambdaQuery()
                .eq(SysNotice::getStatus, SysNoticeStatusEnum.NORMAL.getCode())
                .eq(SysNotice::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .count();
        DashboardMetricsBO bo = new DashboardMetricsBO();
        bo.setWeekNewUser(weekNewUser);
        bo.setTodayLogin(todayLogin);
        bo.setOnlineUser(onlineUser);
        bo.setNoticeCount(noticeCount);
        return bo;
    }

    /**
     * 服务器健康度（仪表盘独立采集，不依赖系统监控 ServerComponent）
     *
     * <p>返回 CPU / 系统内存 / 磁盘使用率（%）与网络利用率（%），供首页健康度展示。</p>
     */
    public DashboardHealthBO getHealth() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
        List<NetworkIF> networks = si.getHardware().getNetworkIFs();

        DashboardHealthBO bo = new DashboardHealthBO();

        // CPU 使用率（两次 tick 采样，存在约 1 秒阻塞）；同时记录合格网卡的初始收发字节数
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Map<String, Long> prevRecv = new HashMap<>(networks.size());
        Map<String, Long> prevSent = new HashMap<>(networks.size());
        for (NetworkIF net : networks) {
            if (!isEligibleNic(net)) {
                continue;
            }
            net.updateAttributes();
            prevRecv.put(net.getName(), net.getBytesRecv());
            prevSent.put(net.getName(), net.getBytesSent());
        }
        Util.sleep(CPU_SAMPLE_INTERVAL);

        // CPU 使用率
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        bo.setCpuUsage(round(cpuLoad * 100));

        // 系统内存使用率
        long memTotal = memory.getTotal();
        long memFree = memory.getAvailable();
        bo.setMemoryUsage(round((double) (memTotal - memFree) / memTotal * 100));

        // 磁盘最大使用率（遍历所有挂载点，跳过无容量虚拟盘）
        double maxDisk = 0.0;
        for (OSFileStore fs : fileSystem.getFileStores()) {
            long total = fs.getTotalSpace();
            if (total <= 0) {
                continue;
            }
            double usage = (double) (total - fs.getFreeSpace()) / total * 100;
            if (usage > maxDisk) {
                maxDisk = usage;
            }
        }
        bo.setDiskUsage(round(maxDisk));

        // 网络利用率（%）：在各物理网卡取「收发方向中的峰值」后，再取所有网卡的最大值；
        // 两次采样取字节 delta 算吞吐速率，除以网卡带宽（bits/s ÷ 8 → bytes/s）。跳过回环/未启用/速率未知网卡。
        double intervalSec = CPU_SAMPLE_INTERVAL / 1000.0;
        boolean hasEligible = false;
        double maxNetwork = 0.0;
        for (NetworkIF net : networks) {
            if (!isEligibleNic(net)) {
                continue;
            }
            hasEligible = true;
            net.updateAttributes();
            long curRecv = net.getBytesRecv();
            long curSent = net.getBytesSent();
            long dRecv = Math.max(0, curRecv - prevRecv.getOrDefault(net.getName(), curRecv));
            long dSent = Math.max(0, curSent - prevSent.getOrDefault(net.getName(), curSent));
            double bandwidth = (double) net.getSpeed() / 8.0;
            double rxUtil = ((double) dRecv / intervalSec) / bandwidth * 100;
            double txUtil = ((double) dSent / intervalSec) / bandwidth * 100;
            double ifaceUtil = Math.max(rxUtil, txUtil);
            if (ifaceUtil > maxNetwork) {
                maxNetwork = ifaceUtil;
            }
        }
        bo.setNetworkUsage(hasEligible ? round(maxNetwork) : null);

        return bo;
    }

    /**
     * 近 7 天登录趋势（按日期分组）
     */
    public List<LoginTrendBO> getLoginTrend() {
        return loginLogMapper.selectLoginTrend();
    }

    /**
     * 运维概览：登录失败、操作异常、任务失败、暂停任务
     */
    public DashboardOpsBO getOpsOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long loginFail = loginLogService.lambdaQuery()
                .eq(SysLoginLog::getStatus, LoginLogStatusEnum.FAIL.getCode())
                .ge(SysLoginLog::getLoginTime, todayStart)
                .count();
        Long operateException = sysLogService.lambdaQuery()
                .eq(SysLog::getStatus, OperLogStatusEnum.ABNORMAL.getCode())
                .count();
        Long jobFail = jobLogService.lambdaQuery()
                .eq(SysJobLog::getStatus, JobLogStatusEnum.FAIL.getCode())
                .ge(SysJobLog::getCreateTime, todayStart)
                .count();
        Long jobPause = jobService.lambdaQuery()
                .eq(SysJob::getStatus, JobStatusEnum.PAUSED.getCode())
                .count();
        DashboardOpsBO bo = new DashboardOpsBO();
        bo.setLoginFail(loginFail);
        bo.setOperateException(operateException);
        bo.setJobFail(jobFail);
        bo.setJobPause(jobPause);
        return bo;
    }

    /**
     * 最近登录（最新 5 条）
     */
    public List<SysLoginLog> getRecentLogins() {
        return loginLogService.lambdaQuery()
                .orderByDesc(SysLoginLog::getLoginTime)
                .last("LIMIT 5")
                .list();
    }

    /**
     * 最新公告（status=1 且未删除，最新 5 条）
     */
    public List<SysNotice> getNotices() {
        return noticeService.lambdaQuery()
                .eq(SysNotice::getStatus, SysNoticeStatusEnum.NORMAL.getCode())
                .eq(SysNotice::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .orderByDesc(SysNotice::getCreateTime)
                .last("LIMIT 5")
                .list();
    }

    /**
     * 保留两位小数（四舍五入）
     */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 判断是否纳入网络利用率统计的物理网卡：速率 > 0 且非回环接口。
     *
     * <p>OSHI 6.x 的 {@link NetworkIF} 未提供 isLoopback()/isUp()，
     * 故回环判定以接口名是否含 "loopback" 近似（Windows 回环名为 "Loopback Pseudo-Interface"）。</p>
     */
    private static boolean isEligibleNic(NetworkIF net) {
        if (net.getSpeed() <= 0) {
            return false;
        }
        String name = net.getName();
        return !(name != null && name.toLowerCase().contains("loopback"));
    }
}
