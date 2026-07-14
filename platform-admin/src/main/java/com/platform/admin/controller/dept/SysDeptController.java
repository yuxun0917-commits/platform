package com.platform.admin.controller.dept;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.admin.vo.dept.*;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysDept;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.DeptStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.utils.JacksonUtil;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.dept.DeptComponent;
import com.platform.service.service.SysDeptService;
import com.platform.service.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 部门管理控制器
 *
 * <p>提供部门树形列表、部门添加、部门编辑、部门删除、部门详情等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /dept/tree   - 部门树形列表（无需入参）</li>
 *   <li>GET  /dept/select-list - 部门选择列表（性能查询，仅返回id/部门名称）</li>
 *   <li>GET  /dept/view   - 部门详情</li>
 *   <li>GET  /dept/enums  - 部门相关枚举列表（状态）</li>
 *   <li>POST /dept/add    - 添加部门</li>
 *   <li>POST /dept/edit   - 编辑部门</li>
 *   <li>POST /dept/editStatus - 切换部门状态（禁用/激活）</li>
 *   <li>POST /dept/delete - 删除部门（逻辑删除）</li>
 *   <li>POST /dept/sort   - 部门排序（仅支持同级排序）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "部门管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dept")
public class SysDeptController {

    private final SysDeptService sysDeptService;
    private final SysUserService sysUserService;
    private final MapperFacade mapperFacade;
    private final DeptComponent deptComponent;

    // ============================ 查询接口 ============================

    /**
     * 部门树形列表
     *
     * <p>查询所有未删除部门，按 display_order 升序排序，递归构建树形结构。
     * 根节点的 parentId 为 0。无需任何入参。</p>
     *
     * @return 部门树形列表
     */
    @Operation(summary = "部门树形列表")
    @GetMapping("/tree")
    public Result tree() {
        // 1. 优先读缓存
        String cached = deptComponent.getDeptTree();
        if (Objects.nonNull(cached)) {
            List<DeptTreeVO> tree = JacksonUtil.parseTypeRef(cached, new TypeReference<List<DeptTreeVO>>() {});
            return Result.success(tree);
        }
        // 2. 缓存未命中：查询所有未删除部门（已按 displayOrder 升序、id 倒序排序）
        List<SysDept> list = sysDeptService.listTree();
        // 3. 转换为 DeptTreeVO 并填充 statusText
        List<DeptTreeVO> voList = list.stream()
                .map(sysDept -> {
                    DeptTreeVO vo = mapperFacade.map(sysDept, DeptTreeVO.class);
                    vo.setStatusText(DeptStatusEnum.getDescByCode(sysDept.getStatus()));
                    return vo;
                })
                .toList();
        // 4. 递归构建树（根节点 parentId = 0）
        List<DeptTreeVO> tree = buildTree(voList, 0L);
        // 5. 写入缓存
        deptComponent.setDeptTree(JacksonUtil.toJsonString(tree));
        return Result.success(tree);
    }

    /**
     * 递归构建部门树
     *
     * @param list     所有部门列表
     * @param parentId 当前层级的父ID
     * @return         子树列表
     */
    private List<DeptTreeVO> buildTree(List<DeptTreeVO> list, Long parentId) {
        List<DeptTreeVO> tree = new ArrayList<>();
        for (DeptTreeVO vo : list) {
            if (Objects.equals(vo.getParentId(), parentId)) {
                vo.setChildren(buildTree(list, vo.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 部门选择列表
     *
     * <p>性能接口：仅返回 id、部门名称两个字段，只查未删除的正常状态部门。
     * 支持 keyword 模糊匹配部门名称，用于下拉选择等场景。</p>
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param keyword   模糊匹配关键词（匹配部门名称），可选
     * @return          部门选择列表（分页）
     */
    @Operation(summary = "部门选择列表")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysDept> paging = new Paging<>(page, pageSize);
        sysDeptService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(sysDept -> mapperFacade.map(sysDept, DeptSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 部门详情
     *
     * @param id    部门ID
     * @return      部门详细信息
     */
    @Operation(summary = "部门详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "部门ID不能为空") Long id) {
        SysDept sysDept = sysDeptService.findById(id);
        DeptVO vo = mapperFacade.map(sysDept, DeptVO.class);
        vo.setStatusText(DeptStatusEnum.getDescByCode(sysDept.getStatus()));
        return Result.success(vo);
    }

    /**
     * 部门相关枚举列表
     *
     * <p>返回部门模块前端需要的枚举选项（状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "部门相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(DeptStatusEnum.values())
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
     * 添加部门
     *
     * <p>校验状态合法性后保存。</p>
     *
     * @param saveVO    部门添加入参
     * @return          操作结果
     */
    @Operation(summary = "添加部门")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody DeptSaveVO saveVO) {
        // 1. 校验状态合法性
        Assert.notNull(DeptStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 2. 保存
        SysDept sysDept = mapperFacade.map(saveVO, SysDept.class);
        sysDeptService.save(sysDept);
        // 3. 清除部门树缓存
        deptComponent.cleanDeptCache();
        return Result.success();
    }

    /**
     * 编辑部门
     *
     * <p>修改部门基本信息。</p>
     *
     * @param editVO    部门编辑入参
     * @return          操作结果
     */
    @Operation(summary = "编辑部门")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody DeptEditVO editVO) {
        // 1. 校验部门是否存在
        sysDeptService.findById(editVO.getId());
        // 2. 校验状态合法性
        if (Objects.nonNull(editVO.getStatus())) {
            Assert.notNull(DeptStatusEnum.getByCode(editVO.getStatus()), "状态值不合法（0禁用 1正常）");
        }
        // 3. 更新
        SysDept sysDept = mapperFacade.map(editVO, SysDept.class);
        sysDeptService.lambdaUpdate()
                .eq(SysDept::getId, editVO.getId())
                .update(sysDept);
        // 4. 清除部门树缓存
        deptComponent.cleanDeptCache();
        return Result.success();
    }

    /**
     * 切换部门状态
     *
     * <p>根据部门当前状态取反：正常→禁用，禁用→正常。</p>
     *
     * @param id    部门ID
     * @return      操作结果
     */
    @Operation(summary = "切换部门状态")
    @PostMapping("/editStatus")
    @JsonCoverParam
    public Result editStatus(@NotNull(message = "请选择需要操作的部门") Long id) {
        // 1. 校验部门是否存在
        SysDept sysDept = sysDeptService.findById(id);
        // 2. 根据当前状态取反
        Integer targetStatus = DeptStatusEnum.NORMAL.fromStatus(sysDept.getStatus())
                ? DeptStatusEnum.DISABLED.getCode()
                : DeptStatusEnum.NORMAL.getCode();
        sysDeptService.lambdaUpdate()
                .set(SysDept::getStatus, targetStatus)
                .eq(SysDept::getId, id)
                .update();
        // 3. 清除部门树缓存
        deptComponent.cleanDeptCache();
        return Result.success();
    }

    /**
     * 删除部门（逻辑删除）
     *
     * <p>删除前校验：该部门下不能有子部门，且不能有关联的用户。</p>
     *
     * @param id    部门ID
     * @return      操作结果
     */
    @Operation(summary = "删除部门")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的部门") Long id) {
        // 1. 校验部门是否存在
        sysDeptService.findById(id);
        // 2. 校验是否存在子部门
        long childCount = sysDeptService.lambdaQuery()
                .eq(SysDept::getParentId, id)
                .eq(SysDept::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .count();
        Assert.isTrue(childCount == 0, "该部门下存在子部门，无法删除");
        // 3. 校验部门下是否存在用户
        long userCount = sysUserService.lambdaQuery()
                .eq(SysUser::getDeptId, id)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .count();
        Assert.isTrue(userCount == 0, "该部门下存在用户，无法删除");
        // 4. 逻辑删除
        deptComponent.doSomethingInTransactional(() -> {
            sysDeptService.lambdaUpdate()
                    .set(SysDept::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                    .eq(SysDept::getId, id)
                    .update();
            return true;
        });
        // 5. 清除部门树缓存
        deptComponent.cleanDeptCache();
        return Result.success();
    }

    /**
     * 部门排序（仅支持同级排序）
     *
     * <p>前端拖拽调整<b>同一层级</b>内部门的顺序后，传入该层级的 parentId 与
     * 部门ID列表（按排序顺序），后端按 index + 1 依次赋值 display_order。</p>
     *
     * <p>约束：传入的所有部门必须同属一个父级（parentId），否则拒绝排序，
     * 以保证仅在同级范围内调整顺序，不会跨层级移动部门。
     * 在事务中执行，保证所有排序值要么全部更新成功，要么全部回滚。</p>
     *
     * @param sortVO    排序入参（父部门ID + 同级部门ID列表）
     * @return          操作结果
     */
    @Operation(summary = "部门排序")
    @PostMapping("/sort")
    public Result sort(@Valid @RequestBody DeptSortVO sortVO) {
        List<Long> ids = sortVO.getIds();
        Long parentId = sortVO.getParentId();
        // 1. 校验：传入的部门必须都存在且同属一个父级（仅同级排序）
        List<SysDept> depts = sysDeptService.lambdaQuery()
                .in(SysDept::getId, ids)
                .list();
        Assert.isTrue(depts.size() == ids.size(), "存在无效的部门ID");
        boolean allSameLevel = depts.stream()
                .allMatch(dept -> Objects.equals(dept.getParentId(), parentId));
        Assert.isTrue(allSameLevel, "仅支持同级排序，请勿跨层级排序");
        // 2. 在事务中按顺序赋值 displayOrder
        deptComponent.doSomethingInTransactional(() -> {
            for (int i = 0; i < ids.size(); i++) {
                sysDeptService.lambdaUpdate()
                        .set(SysDept::getDisplayOrder, i + 1)
                        .eq(SysDept::getId, ids.get(i))
                        .update();
            }
            return true;
        });
        // 3. 清除部门树缓存
        deptComponent.cleanDeptCache();
        return Result.success();
    }
}
