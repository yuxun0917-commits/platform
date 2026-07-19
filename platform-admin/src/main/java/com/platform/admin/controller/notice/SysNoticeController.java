package com.platform.admin.controller.notice;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.notice.NoticeEditVO;
import com.platform.admin.vo.notice.NoticeSaveVO;
import com.platform.admin.vo.notice.NoticeSelectVO;
import com.platform.admin.vo.notice.NoticeVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysNotice;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysNoticePositionEnum;
import com.platform.common.enums.SysNoticeStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.service.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 通知公告管理控制器
 *
 * <p>提供通知公告的分页查询、新增、编辑、删除等接口。
 * 支持按通知类型（通知/公告）、展示位置（后台/前台）筛选。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /notice/page        - 通知公告列表（分页查询，支持类型和位置筛选）</li>
 *   <li>GET  /notice/select-list - 通知公告选择列表（性能查询）</li>
 *   <li>GET  /notice/view        - 通知公告详情</li>
 *   <li>GET  /notice/enums       - 通知相关枚举列表（状态、类型、位置）</li>
 *   <li>POST /notice/add         - 添加通知公告</li>
 *   <li>POST /notice/edit        - 编辑通知公告</li>
 *   <li>POST /notice/delete      - 删除通知公告（逻辑删除）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "通知公告")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;
    private final MapperFacade mapperFacade;

    // ============================ 查询接口 ============================

    /**
     * 分页查询通知公告列表
     *
     * @param page       页码
     * @param pageSize   页大小
     * @param status     状态（1正常 0禁用），可选筛选条件
     * @param noticeType 通知类型（1通知 2公告），可选筛选条件
     * @param position   展示位置（1后台 2前台），可选筛选条件
     * @param keyword    模糊匹配关键词（匹配通知标题），可选
     * @return 通知公告分页列表
     */
    @Operation(summary = "通知公告列表")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, Integer noticeType, Integer position, String keyword) {
        Paging<SysNotice> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("position", position);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysNoticeService.paging(paging, paramsMap);
        paging.convert(notice -> {
            NoticeVO vo = mapperFacade.map(notice, NoticeVO.class);
            vo.setPositionText(SysNoticePositionEnum.getDescByCode(notice.getPosition()));
            vo.setStatusText(SysNoticeStatusEnum.getDescByCode(notice.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 通知公告选择列表
     *
     * <p>性能接口：仅返回 id、通知标题两个字段。用于下拉选择等场景。</p>
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param keyword  模糊匹配关键词（匹配通知标题），可选
     * @return 通知公告选择列表（分页）
     */
    @Operation(summary = "通知公告选择列表")
    @SaCheckPermission("system:notice:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysNotice> paging = new Paging<>(page, pageSize);
        sysNoticeService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(notice -> mapperFacade.map(notice, NoticeSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 通知公告详情
     *
     * @param id 通知ID
     * @return 通知公告详细信息
     */
    @Operation(summary = "通知公告详情")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "通知ID不能为空") Long id) {
        SysNotice notice = sysNoticeService.findById(id);
        NoticeVO vo = mapperFacade.map(notice, NoticeVO.class);
        vo.setPositionText(SysNoticePositionEnum.getDescByCode(notice.getPosition()));
        vo.setStatusText(SysNoticeStatusEnum.getDescByCode(notice.getStatus()));
        return Result.success(vo);
    }

    /**
     * 通知相关枚举列表
     *
     * <p>返回通知模块前端需要的枚举选项（状态、类型、位置），供下拉选择使用。</p>
     *
     * @return 枚举选项列表（Map 结构，key 为枚举分类，value 为选项列表）
     */
    @Operation(summary = "通知相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        Map<String, List<EnumVO>> result = new HashMap<>();
        // 状态枚举
        result.put("status", Arrays.stream(SysNoticeStatusEnum.values())
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
     * 添加通知公告
     *
     * <p>校验通知类型和展示位置合法性后保存。</p>
     *
     * @param saveVO 通知公告添加入参
     * @return 操作结果
     */
    @Operation(summary = "添加通知公告")
    @SaCheckPermission("system:notice:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody NoticeSaveVO saveVO) {
        // 2. 校验展示位置合法性
        Assert.notNull(SysNoticePositionEnum.getByCode(saveVO.getPosition()), "展示位置值不合法（1后台 2前台）");
        // 3. 校验状态合法性
        Assert.notNull(SysNoticeStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 4. 保存
        SysNotice notice = mapperFacade.map(saveVO, SysNotice.class);
        notice.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysNoticeService.save(notice);
        return Result.success();
    }

    /**
     * 编辑通知公告
     *
     * @param editVO 通知公告编辑入参
     * @return 操作结果
     */
    @Operation(summary = "编辑通知公告")
    @SaCheckPermission("system:notice:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody NoticeEditVO editVO) {
        // 1. 校验通知公告是否存在
        sysNoticeService.findById(editVO.getId());
        // 3. 校验展示位置合法性
        Assert.notNull(SysNoticePositionEnum.getByCode(editVO.getPosition()), "展示位置值不合法（1后台 2前台）");
        // 4. 校验状态合法性
        Assert.notNull(SysNoticeStatusEnum.getByCode(editVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 5. 更新
        SysNotice updateNotice = mapperFacade.map(editVO, SysNotice.class);
        updateNotice.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysNoticeService.updateById(updateNotice);
        return Result.success();
    }

    /**
     * 删除通知公告（逻辑删除）
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "删除通知公告")
    @SaCheckPermission("system:notice:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的通知") Long id) {
        // 1. 校验通知公告是否存在
        sysNoticeService.findById(id);
        // 2. 逻辑删除
        sysNoticeService.lambdaUpdate()
                .eq(SysNotice::getId, id)
                .set(SysNotice::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysNotice::getUpdateBy, SecurityUser.getUserId())
                .set(SysNotice::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success();
    }
}
