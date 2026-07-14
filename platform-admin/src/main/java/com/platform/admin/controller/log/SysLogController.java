package com.platform.admin.controller.log;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.log.SysLogVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysLog;
import com.platform.common.enums.OperLogStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.service.service.SysLogService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志管理控制器
 *
 * <p>提供操作日志分页查询、日志详情、日志删除、清空日志等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /sysLog/page   - 操作日志列表（分页查询）</li>
 *   <li>GET  /sysLog/view   - 操作日志详情</li>
 *   <li>POST /sysLog/delete - 删除操作日志（单条）</li>
 *   <li>POST /sysLog/clean  - 清空全部操作日志</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "操作日志")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysLog")
public class SysLogController {

    private final SysLogService sysLogService;
    private final MapperFacade mapperFacade;

    // ============================ 查询接口 ============================

    /**
     * 分页查询操作日志列表
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param status    操作状态（1正常 0异常），可选筛选条件
     * @param keyword   模糊匹配关键词（匹配模块标题、操作人名称），可选
     * @return          操作日志分页列表
     */
    @Operation(summary = "操作日志列表")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysLog> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysLogService.paging(paging, paramsMap);
        paging.convert(log -> {
            SysLogVO vo = mapperFacade.map(log, SysLogVO.class);
            vo.setStatusText(OperLogStatusEnum.getDescByCode(log.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 操作日志详情
     *
     * @param id    日志ID
     * @return      操作日志详细信息
     */
    @Operation(summary = "操作日志详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "日志ID不能为空") Long id) {
        SysLog log = sysLogService.findById(id);
        SysLogVO vo = mapperFacade.map(log, SysLogVO.class);
        vo.setStatusText(OperLogStatusEnum.getDescByCode(log.getStatus()));
        return Result.success(vo);
    }

    /**
     * 操作日志相关枚举列表
     *
     * <p>返回操作日志模块前端需要的枚举选项（操作状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "操作日志相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(OperLogStatusEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                }).toList();
        return Result.success(vos);
    }

    // ============================ 数据修改接口 ============================

    /**
     * 删除操作日志（单条）
     *
     * <p>日志表无逻辑删除字段，直接物理删除单条记录。</p>
     *
     * @param id    日志ID
     * @return      操作结果
     */
    @Operation(summary = "删除操作日志")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的日志") Long id) {
        // 校验日志是否存在
        sysLogService.findById(id);
        // 日志表无 is_delete，直接 removeById
        sysLogService.removeById(id);
        return Result.success();
    }

    /**
     * 清空全部操作日志
     *
     * <p>删除操作日志表中的全部记录。</p>
     *
     * @return 操作结果
     */
    @Operation(summary = "清空操作日志")
    @PostMapping("/clean")
    @JsonCoverParam
    public Result clean() {
        Assert.isTrue(sysLogService.lambdaUpdate()
                .ge(SysLog::getId, 0)
                .remove(), "清空操作日志失败");
        return Result.success();
    }
}
