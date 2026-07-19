package com.platform.admin.controller.post;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.post.PostEditVO;
import com.platform.admin.vo.post.PostSaveVO;
import com.platform.admin.vo.post.PostSelectVO;
import com.platform.admin.vo.post.PostSortVO;
import com.platform.admin.vo.post.PostVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysPost;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysPostStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.post.PostComponent;
import com.platform.service.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 岗位管理控制器
 *
 * <p>提供岗位的分页查询、新增、编辑、删除等接口。
 * 岗位编码（post_code）全局唯一，添加/编辑时校验唯一性。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /post/page        - 岗位列表（分页查询）</li>
 *   <li>GET  /post/select-list - 岗位选择列表（性能查询）</li>
 *   <li>GET  /post/view        - 岗位详情</li>
 *   <li>GET  /post/enums       - 岗位相关枚举列表</li>
 *   <li>POST /post/add         - 添加岗位</li>
 *   <li>POST /post/edit        - 编辑岗位</li>
 *   <li>POST /post/delete      - 删除岗位（逻辑删除）</li>
 *   <li>POST /post/sort        - 批量排序岗位</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "岗位管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/post")
public class SysPostController {

    private final SysPostService sysPostService;
    private final MapperFacade mapperFacade;
    private final PostComponent postComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询岗位列表
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param status   状态（1正常 0禁用），可选筛选条件
     * @param keyword  模糊匹配关键词（匹配岗位名称、岗位编码），可选
     * @return 岗位分页列表
     */
    @Operation(summary = "岗位列表")
    @SaCheckPermission("system:post:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysPost> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysPostService.paging(paging, paramsMap);
        paging.convert(post -> {
            PostVO vo = mapperFacade.map(post, PostVO.class);
            vo.setStatusText(SysPostStatusEnum.getDescByCode(post.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 岗位选择列表
     *
     * <p>性能接口：仅返回 id、岗位名称两个字段。用于下拉选择等场景。</p>
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param keyword  模糊匹配关键词（匹配岗位名称），可选
     * @return 岗位选择列表（分页）
     */
    @Operation(summary = "岗位选择列表")
    @SaCheckPermission("system:post:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysPost> paging = new Paging<>(page, pageSize);
        sysPostService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(post -> mapperFacade.map(post, PostSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 岗位详情
     *
     * @param id 岗位ID
     * @return 岗位详细信息
     */
    @Operation(summary = "岗位详情")
    @SaCheckPermission("system:post:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "岗位ID不能为空") Long id) {
        SysPost post = sysPostService.findById(id);
        PostVO vo = mapperFacade.map(post, PostVO.class);
        vo.setStatusText(SysPostStatusEnum.getDescByCode(post.getStatus()));
        return Result.success(vo);
    }

    /**
     * 岗位相关枚举列表
     *
     * <p>返回岗位模块前端需要的枚举选项（岗位状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "岗位相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(SysPostStatusEnum.values())
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
     * 添加岗位
     *
     * <p>校验岗位编码唯一性后保存。</p>
     *
     * @param saveVO 岗位添加入参
     * @return 操作结果
     */
    @Operation(summary = "添加岗位")
    @SaCheckPermission("system:post:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody PostSaveVO saveVO) {
        // 1. 校验岗位编码唯一性
        SysPost existing = sysPostService.findByPostCode(saveVO.getPostCode());
        Assert.isNull(existing, "岗位编码【{}】已存在", saveVO.getPostCode());
        // 2. 校验状态合法性
        Assert.notNull(SysPostStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 3. 保存
        SysPost post = mapperFacade.map(saveVO, SysPost.class);
        post.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysPostService.save(post);
        return Result.success();
    }

    /**
     * 编辑岗位
     *
     * <p>修改岗位信息，岗位编码变更时校验新编码唯一性。</p>
     *
     * @param editVO 岗位编辑入参
     * @return 操作结果
     */
    @Operation(summary = "编辑岗位")
    @SaCheckPermission("system:post:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody PostEditVO editVO) {
        // 1. 校验岗位是否存在
        SysPost post = sysPostService.findById(editVO.getId());
        // 2. 如果岗位编码变更，校验新编码唯一性
        if (!Objects.equals(post.getPostCode(), editVO.getPostCode())) {
            SysPost existing = sysPostService.findByPostCode(editVO.getPostCode());
            Assert.isNull(existing, "岗位编码【{}】已存在", editVO.getPostCode());
        }
        // 3. 校验状态合法性
        Assert.notNull(SysPostStatusEnum.getByCode(editVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 4. 更新
        SysPost updatePost = mapperFacade.map(editVO, SysPost.class);
        updatePost.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysPostService.updateById(updatePost);
        return Result.success();
    }

    /**
     * 删除岗位（逻辑删除）
     *
     * @param id 岗位ID
     * @return 操作结果
     */
    @Operation(summary = "删除岗位")
    @SaCheckPermission("system:post:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的岗位") Long id) {
        // 1. 校验岗位是否存在
        sysPostService.findById(id);
        // 2. 逻辑删除
        sysPostService.lambdaUpdate()
                .eq(SysPost::getId, id)
                .set(SysPost::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysPost::getUpdateBy, SecurityUser.getUserId())
                .set(SysPost::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success();
    }

    /**
     * 批量排序岗位
     *
     * <p>前端拖拽排序后，传入岗位ID列表（按排序顺序）和起始排序值。
     * 后端按 startOrder + index 赋值 display_order。
     * 分页场景下前端传当前页的起始排序值，各页互不干扰。
     * 在事务中执行，保证所有排序值要么全部更新成功，要么全部回滚。</p>
     *
     * @param sortVO 排序入参（起始排序值 + 岗位ID列表）
     * @return 操作结果
     */
    @Operation(summary = "批量排序岗位")
    @SaCheckPermission("system:post:sort")
    @PostMapping("/sort")
    public Result sort(@Valid @RequestBody PostSortVO sortVO) {
        List<Long> ids = sortVO.getIds();
        int startOrder = sortVO.getStartOrder();
        AtomicInteger idx = new AtomicInteger(startOrder);
        List<SysPost> postList = ids.stream()
                .map(id -> {
                    return new SysPost()
                            .setId(id)
                            .setDisplayOrder(idx.getAndIncrement())
                            .setUpdateBy(SecurityUser.getUserId())
                            .setUpdateTime(LocalDateTime.now());
                }).toList();
        sysPostService.updateBatchById(postList);
        return Result.success();
    }
}
