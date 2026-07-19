package com.platform.admin.vo.dashboard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘服务器健康度展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "仪表盘-服务器健康度")
public class DashboardHealthVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "CPU 使用率（%）")
    private Double cpuUsage;

    @Schema(description = "系统内存使用率（%）")
    private Double memoryUsage;

    @Schema(description = "磁盘最大使用率（%）")
    private Double diskUsage;

    @Schema(description = "网络利用率（%，各物理网卡收发峰值；不支持为 null）")
    private Double networkUsage;
}
