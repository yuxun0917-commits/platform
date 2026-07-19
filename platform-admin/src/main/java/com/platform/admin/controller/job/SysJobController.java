package com.platform.admin.controller.job;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.job.SysJobEditVO;
import com.platform.admin.vo.job.SysJobSaveVO;
import com.platform.admin.vo.job.SysJobVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysJob;
import com.platform.common.enums.ConcurrentEnum;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.JobStatusEnum;
import com.platform.common.enums.MisfirePolicyEnum;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.job.ScheduleComponent;
import com.platform.service.service.SysJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 定时任务管理控制器
 *
 * <p>提供定时任务的分页查询、新增、编辑、删除、状态切换、立即执行等接口。
 * 任务在数据库配置 cron 与调用目标，状态为正常时注册到 Quartz 调度器按 cron 执行。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /job/page        - 任务列表（分页查询）</li>
 *   <li>GET  /job/select-list - 任务选择列表（性能查询）</li>
 *   <li>GET  /job/view        - 任务详情</li>
 *   <li>GET  /job/enums       - 任务相关枚举列表</li>
 *   <li>POST /job/add         - 添加任务</li>
 *   <li>POST /job/edit        - 编辑任务</li>
 *   <li>POST /job/delete      - 删除任务（逻辑删除）</li>
 *   <li>POST /job/changeStatus - 切换任务状态（启用/暂停）</li>
 *   <li>POST /job/run         - 立即执行一次</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "定时任务")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/job")
public class SysJobController {

    private final SysJobService sysJobService;
    private final MapperFacade mapperFacade;
    private final ScheduleComponent scheduleComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询任务列表
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param status   状态（1正常 0暂停），可选
     * @param keyword  模糊匹配关键词（匹配任务名称、任务组名），可选
     * @return 任务分页列表
     */
    @Operation(summary = "任务列表")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysJob> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysJobService.paging(paging, paramsMap);
        paging.convert(job -> {
            SysJobVO vo = mapperFacade.map(job, SysJobVO.class);
            vo.setStatusText(JobStatusEnum.getDescByCode(job.getStatus()));
            vo.setMisfirePolicyText(MisfirePolicyEnum.getDescByCode(job.getMisfirePolicy()));
            vo.setConcurrentText(ConcurrentEnum.getDescByCode(job.getConcurrent()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 任务选择列表
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param keyword  模糊匹配关键词（匹配任务名称），可选
     * @return 任务选择列表（分页）
     */
    @Operation(summary = "任务选择列表")
    @SaCheckPermission("monitor:job:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysJob> paging = new Paging<>(page, pageSize);
        sysJobService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(job -> mapperFacade.map(job, SysJobVO.class));
        return Result.success(paging);
    }

    /**
     * 任务详情
     *
     * @param id 任务ID
     * @return 任务详细信息
     */
    @Operation(summary = "任务详情")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "任务ID不能为空") Long id) {
        SysJob job = sysJobService.findById(id);
        SysJobVO vo = mapperFacade.map(job, SysJobVO.class);
        vo.setStatusText(JobStatusEnum.getDescByCode(job.getStatus()));
        vo.setMisfirePolicyText(MisfirePolicyEnum.getDescByCode(job.getMisfirePolicy()));
        vo.setConcurrentText(ConcurrentEnum.getDescByCode(job.getConcurrent()));
        return Result.success(vo);
    }

    /**
     * 任务相关枚举列表
     *
     * <p>本模块关联三个枚举（任务状态、错失触发策略、并发策略），按分组返回。</p>
     *
     * @return 枚举选项分组
     */
    @Operation(summary = "任务相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        Map<String, List<EnumVO>> result = new HashMap<>();
        result.put("jobStatus", toEnumVOList(JobStatusEnum.values()));
        result.put("misfirePolicy", toEnumVOList(MisfirePolicyEnum.values()));
        result.put("concurrent", toEnumVOList(ConcurrentEnum.values()));
        return Result.success(result);
    }

    // ============================ 数据修改接口 ============================

    /**
     * 添加任务
     *
     * <p>校验 cron 表达式合法性后保存；状态为正常时同步注册到调度器。</p>
     *
     * @param saveVO 任务添加入参
     * @return 操作结果
     */
    @Operation(summary = "添加任务")
    @SaCheckPermission("monitor:job:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody SysJobSaveVO saveVO) {
        checkParams(saveVO.getCronExpression(), saveVO.getMisfirePolicy(), saveVO.getConcurrent(), saveVO.getStatus());
        SysJob job = mapperFacade.map(saveVO, SysJob.class);
        if (StrUtil.isBlank(job.getJobGroup())) {
            job.setJobGroup("DEFAULT");
        }
        job.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysJobService.save(job);
        // 状态正常则注册到调度器
        if (JobStatusEnum.NORMAL.fromStatus(job.getStatus())) {
            scheduleWithCatch(job, "任务调度注册失败");
        }
        return Result.success();
    }

    /**
     * 编辑任务
     *
     * <p>修改任务定义，若 cron 或并发策略变化则重建调度。</p>
     *
     * @param editVO 任务编辑入参
     * @return 操作结果
     */
    @Operation(summary = "编辑任务")
    @SaCheckPermission("monitor:job:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody SysJobEditVO editVO) {
        SysJob exist = sysJobService.findById(editVO.getId());
        checkParams(editVO.getCronExpression(), editVO.getMisfirePolicy(), editVO.getConcurrent(), editVO.getStatus());
        SysJob updateJob = mapperFacade.map(editVO, SysJob.class);
        updateJob.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysJobService.updateById(updateJob);
        // 先移除旧调度，再按新定义重建
        removeJobWithCatch(exist.getId());
        if (JobStatusEnum.NORMAL.fromStatus(editVO.getStatus())) {
            SysJob latest = sysJobService.findById(editVO.getId());
            scheduleWithCatch(latest, "任务调度重建失败");
        }
        return Result.success();
    }

    /**
     * 删除任务（逻辑删除）
     *
     * <p>同时从调度器移除该任务。</p>
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @Operation(summary = "删除任务")
    @SaCheckPermission("monitor:job:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的任务") Long id) {
        sysJobService.findById(id);
        sysJobService.lambdaUpdate()
                .eq(SysJob::getId, id)
                .set(SysJob::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysJob::getUpdateBy, SecurityUser.getUserId())
                .set(SysJob::getUpdateTime, LocalDateTime.now())
                .update();
        removeJobWithCatch(id);
        return Result.success();
    }

    /**
     * 切换任务状态（启用/暂停）
     *
     * <p>当前为暂停则启用并注册调度，当前为正常则暂停并移除调度。</p>
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @Operation(summary = "切换任务状态")
    @SaCheckPermission("monitor:job:changeStatus")
    @PostMapping("/changeStatus")
    @JsonCoverParam
    public Result changeStatus(@NotNull(message = "请选择需要操作的任务") Long id) {
        SysJob job = sysJobService.findById(id);
        Integer newStatus = JobStatusEnum.NORMAL.fromStatus(job.getStatus())
                ? JobStatusEnum.PAUSED.getCode()
                : JobStatusEnum.NORMAL.getCode();
        sysJobService.lambdaUpdate()
                .eq(SysJob::getId, id)
                .set(SysJob::getStatus, newStatus)
                .set(SysJob::getUpdateBy, SecurityUser.getUserId())
                .set(SysJob::getUpdateTime, LocalDateTime.now())
                .update();
        if (JobStatusEnum.NORMAL.fromStatus(newStatus)) {
            // 启用：重新注册调度
            SysJob latest = sysJobService.findById(id);
            scheduleWithCatch(latest, "任务启用失败");
        } else {
            // 暂停：移除调度
            removeJobWithCatch(id);
        }
        return Result.success();
    }

    /**
     * 立即执行一次
     *
     * <p>不影响 cron 计划，仅手动触发一次。</p>
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @Operation(summary = "立即执行一次")
    @SaCheckPermission("monitor:job:run")
    @PostMapping("/run")
    @JsonCoverParam
    public Result run(@NotNull(message = "请选择需要执行的任务") Long id) {
        SysJob job = sysJobService.findById(id);
        Assert.isTrue(JobStatusEnum.NORMAL.fromStatus(job.getStatus()), "任务已暂停, 请开启后执行");
        try {
            scheduleComponent.runOnce(job);
        } catch (SchedulerException e) {
            throw new BusinessException("任务立即执行失败：" + e.getMessage());
        }
        return Result.success();
    }

    // ============================ 私有方法 ============================

    /**
     * 参数校验：cron 合法性 + 枚举值合法性
     */
    private void checkParams(String cronExpression, Integer misfirePolicy, Integer concurrent, Integer status) {
        Assert.isTrue(CronExpression.isValidExpression(cronExpression), "cron表达式不合法：{}", cronExpression);
        Assert.notNull(MisfirePolicyEnum.getByCode(misfirePolicy), "错失触发策略值不合法（0不补跑 1补跑）");
        Assert.notNull(ConcurrentEnum.getByCode(concurrent), "是否并发值不合法（0禁止 1允许）");
        Assert.notNull(JobStatusEnum.getByCode(status), "状态值不合法（1正常 0暂停）");
    }

    /**
     * 注册调度并捕获 SchedulerException
     */
    private void scheduleWithCatch(SysJob job, String errorMsg) {
        try {
            scheduleComponent.createScheduleJob(job);
        } catch (SchedulerException e) {
            throw new BusinessException(errorMsg + "：" + e.getMessage());
        }
    }

    /**
     * 移除调度并捕获 SchedulerException
     */
    private void removeJobWithCatch(Long jobId) {
        try {
            scheduleComponent.deleteJob(jobId);
        } catch (SchedulerException e) {
            throw new BusinessException("任务调度移除失败：" + e.getMessage());
        }
    }

    /**
     * 枚举数组转 EnumVO 列表
     */
    private List<EnumVO> toEnumVOList(Object[] enums) {
        // 反射获取 code/desc（枚举均提供 getCode/getDesc）
        return Arrays.stream(enums)
                .map(e -> {
                    try {
                        EnumVO vo = new EnumVO();
                        vo.setCode((Integer) e.getClass().getMethod("getCode").invoke(e));
                        vo.setDesc((String) e.getClass().getMethod("getDesc").invoke(e));
                        return vo;
                    } catch (Exception ex) {
                        throw new BusinessException("枚举转换失败");
                    }
                }).toList();
    }
}
