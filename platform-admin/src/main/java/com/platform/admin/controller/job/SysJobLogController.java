package com.platform.admin.controller.job;

import com.platform.admin.vo.job.SysJobLogVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysJobLog;
import com.platform.common.enums.JobLogStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.service.service.SysJobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 定时任务日志控制器
 *
 * <p>提供任务执行日志的分页查询、详情、删除、清空接口。
 * 支持以定时任务维度（jobId）查询某任务的执行日志。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /jobLog/page   - 任务日志列表（分页查询，支持 jobId 维度过滤）</li>
 *   <li>GET  /jobLog/view   - 任务日志详情</li>
 *   <li>POST /jobLog/delete - 删除日志</li>
 *   <li>POST /jobLog/clean  - 清空任务日志</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "任务日志")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/jobLog")
public class SysJobLogController {

    private final SysJobLogService sysJobLogService;
    private final MapperFacade mapperFacade;

    /**
     * 分页查询任务日志
     *
     * <p>支持以定时任务维度查询：传入 jobId 时按任务ID精确匹配日志，
     * 用于「在任务列表点某任务查看其执行日志」的场景。</p>
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param status   执行状态（1成功 0失败），可选
     * @param jobId    定时任务ID，可选（以任务维度过滤日志）
     * @return 任务日志分页列表
     */
    @Operation(summary = "任务日志列表")
    @SaCheckPermission("monitor:jobLog:list")
    @GetMapping("/page")
    public Result page(@NotNull(message = "任务id不能为空") Long jobId, Integer page, Integer pageSize, Integer status) {
        Paging<SysJobLog> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        // 以定时任务维度查询：按 jobId 精确匹配日志
        paramsMap.put("jobId", jobId);
        sysJobLogService.paging(paging, paramsMap);
        paging.convert(log -> {
            SysJobLogVO vo = mapperFacade.map(log, SysJobLogVO.class);
            vo.setStatusText(JobLogStatusEnum.getDescByCode(log.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 任务日志详情
     *
     * @param id 日志ID
     * @return 日志详细信息
     */
    @Operation(summary = "任务日志详情")
    @SaCheckPermission("monitor:jobLog:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "日志ID不能为空") Long id) {
        SysJobLog log = sysJobLogService.findById(id);
        SysJobLogVO vo = mapperFacade.map(log, SysJobLogVO.class);
        vo.setStatusText(JobLogStatusEnum.getDescByCode(log.getStatus()));
        return Result.success(vo);
    }

    /**
     * 删除单条任务日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @Operation(summary = "删除日志")
    @SaCheckPermission("monitor:jobLog:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的日志") Long id) {
        sysJobLogService.findById(id);
        sysJobLogService.removeById(id);
        return Result.success();
    }

    /**
     * 清空指定任务日志
     *
     * @return 操作结果
     */
    @Operation(summary = "清空任务日志")
    @SaCheckPermission("monitor:jobLog:clean")
    @PostMapping("/clean")
    public Result clean(@NotNull(message = "任务id不能为空") Long jobId) {
        Assert.isTrue(sysJobLogService.lambdaUpdate()
                .eq(SysJobLog::getJobId, jobId)
                .remove(), "清空任务日志失败");
        return Result.success();
    }
}
