package com.platform.admin.controller.monitor;

import com.platform.admin.vo.monitor.ServerVO;
import com.platform.common.bo.ServerBO;
import com.platform.common.result.Result;
import com.platform.component.monitor.ServerComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务监控控制器
 *
 * <p>提供服务器运行指标查询，包括 CPU、系统内存、JVM、磁盘、Redis 等实时监控数据。
 * 数据由 {@link ServerComponent} 实时采集聚合，不落库（无需建表）。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET /monitor/server - 服务监控（CPU / 内存 / JVM / 磁盘 / Redis）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "服务监控")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor")
public class ServerController {

    private final ServerComponent serverComponent;
    private final MapperFacade mapperFacade;

    /**
     * 获取服务监控数据
     *
     * @return 服务监控聚合数据（CPU / 内存 / JVM / 磁盘 / Redis）
     */
    @Operation(summary = "服务监控（CPU/内存/JVM/磁盘/Redis）")
    @GetMapping("/server")
    public Result server() {
        ServerBO bo = serverComponent.getServerInfo();
        return Result.success(mapperFacade.map(bo, ServerVO.class));
    }
}
