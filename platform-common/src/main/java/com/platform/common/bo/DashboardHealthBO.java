package com.platform.common.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘服务器健康度聚合对象
 *
 * <p>承载首页健康度概览（CPU / 系统内存 / 磁盘使用率与网络利用率），
 * 由 {@code DashboardComponent} 使用 OSHI 独立采集后返回，与系统监控（ServerComponent）解耦。</p>
 *
 * <p>网络利用率在部分环境（回环网卡、未启用、驱动未上报速率）下无法计算，置为 {@code null}，
 * 前端应显示为「不支持」而非 0 或 -1。</p>
 *
 * @author platform
 */
@Data
public class DashboardHealthBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** CPU 使用率（%） */
    private Double cpuUsage;

    /** 系统内存使用率（%） */
    private Double memoryUsage;

    /** 磁盘最大使用率（%，取所有挂载点中的最大值） */
    private Double diskUsage;

    /** 网络利用率（%，各物理网卡收发方向取最大值后的整体峰值；不支持时为 null） */
    private Double networkUsage;
}
