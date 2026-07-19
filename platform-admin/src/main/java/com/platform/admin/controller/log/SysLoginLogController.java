package com.platform.admin.controller.log;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.log.SysLoginLogVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysLoginLog;
import com.platform.common.enums.LoginLogStatusEnum;
import com.platform.common.enums.LoginTypeEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.service.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 登录日志管理控制器
 *
 * <p>提供登录日志分页查询、日志详情、日志删除、清空日志等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /sysLoginLog/page   - 登录日志列表（分页查询）</li>
 *   <li>GET  /sysLoginLog/view   - 登录日志详情</li>
 *   <li>GET  /sysLoginLog/enums  - 登录日志相关枚举列表（登录类型）</li>
 *   <li>POST /sysLoginLog/delete - 删除登录日志（单条）</li>
 *   <li>POST /sysLoginLog/clean  - 清空全部登录日志</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "登录日志")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysLoginLog")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;
    private final MapperFacade mapperFacade;

    // ============================ 查询接口 ============================

    /**
     * 分页查询登录日志列表
     *
     * @param page       页码
     * @param pageSize   页大小
     * @param loginType  登录类型（1登录 2登出 3踢下线），可选筛选条件
     * @param status     登录状态（1成功 0失败），可选筛选条件
     * @param keyword    模糊匹配关键词（匹配用户名、登录IP），可选
     * @return           登录日志分页列表
     */
    @Operation(summary = "登录日志列表")
    @SaCheckPermission("log:loginlog:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer loginType, Integer status, String keyword) {
        Paging<SysLoginLog> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("loginType", loginType);
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysLoginLogService.paging(paging, paramsMap);
        paging.convert(loginLog -> {
            SysLoginLogVO vo = mapperFacade.map(loginLog, SysLoginLogVO.class);
            vo.setLoginTypeText(LoginTypeEnum.getDescByCode(loginLog.getLoginType()));
            vo.setStatusText(LoginLogStatusEnum.getDescByCode(loginLog.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 登录日志详情
     *
     * @param id    日志ID
     * @return      登录日志详细信息
     */
    @Operation(summary = "登录日志详情")
    @SaCheckPermission("log:loginlog:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "日志ID不能为空") Long id) {
        SysLoginLog loginLog = sysLoginLogService.findById(id);
        SysLoginLogVO vo = mapperFacade.map(loginLog, SysLoginLogVO.class);
        vo.setLoginTypeText(LoginTypeEnum.getDescByCode(loginLog.getLoginType()));
        vo.setStatusText(LoginLogStatusEnum.getDescByCode(loginLog.getStatus()));
        return Result.success(vo);
    }

    /**
     * 登录日志相关枚举列表
     *
     * <p>返回登录日志模块前端需要的枚举选项（登录类型），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "登录日志相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        Map<String, List<EnumVO>> result = new HashMap<>();
        // 登录类型枚举
        result.put("loginType", Arrays.stream(LoginTypeEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                }).toList());
        // 登录状态枚举
        result.put("status", Arrays.stream(LoginLogStatusEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                }).toList());
        return Result.success(result);
    }

    // ============================ 数据修改接口 ============================

    /**
     * 删除登录日志（单条）
     *
     * <p>日志表无逻辑删除字段，直接物理删除单条记录。</p>
     *
     * @param id    日志ID
     * @return      操作结果
     */
    @Operation(summary = "删除登录日志")
    @SaCheckPermission("log:loginlog:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的日志") Long id) {
        // 校验日志是否存在
        sysLoginLogService.findById(id);
        // 日志表无 is_delete，直接 removeById
        sysLoginLogService.removeById(id);
        return Result.success();
    }

    /**
     * 清空全部登录日志
     *
     * <p>删除登录日志表中的全部记录。</p>
     *
     * @return 操作结果
     */
    @Operation(summary = "清空登录日志")
    @SaCheckPermission("log:loginlog:clean")
    @PostMapping("/clean")
    @JsonCoverParam
    public Result clean() {
        Assert.isTrue(sysLoginLogService.lambdaUpdate()
                .ge(SysLoginLog::getId, 0)
                .remove(), "清空登录日志失败");
        return Result.success();
    }
}
