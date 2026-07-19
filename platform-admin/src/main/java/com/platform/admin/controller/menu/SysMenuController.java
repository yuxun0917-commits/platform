package com.platform.admin.controller.menu;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.menu.*;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.MenuStatusEnum;
import com.platform.common.enums.MenuTypeEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.menu.MenuComponent;
import com.platform.service.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 菜单管理控制器
 *
 * <p>提供菜单树形列表、菜单添加、菜单编辑、菜单删除、菜单详情等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /menu/tree   - 菜单树形列表（无需入参）</li>
 *   <li>GET  /menu/select-list - 菜单选择列表（性能查询，仅返回id/菜单名称）</li>
 *   <li>GET  /menu/view   - 菜单详情</li>
 *   <li>GET  /menu/enums  - 菜单相关枚举列表（状态）</li>
 *   <li>POST /menu/add    - 添加菜单</li>
 *   <li>POST /menu/edit   - 编辑菜单</li>
 *   <li>POST /menu/editStatus - 切换菜单状态（禁用/激活）</li>
 *   <li>POST /menu/delete - 删除菜单（逻辑删除）</li>
 *   <li>POST /menu/sort   - 菜单排序（仅同级）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "菜单管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/menu")
public class SysMenuController {

    private final SysMenuService sysMenuService;
    private final MapperFacade mapperFacade;
    private final MenuComponent menuComponent;

    // ============================ 查询接口 ============================

    /**
     * 菜单树形列表
     *
     * <p>查询所有未删除菜单，按 display_order 升序排序，递归构建树形结构。
     * 根节点的 parentId 为 0。无需任何入参。</p>
     *
     * @return 菜单树形列表
     */
    @Operation(summary = "菜单树形列表")
    @GetMapping("/tree")
    public Result tree() {
        // 1. 从组件读取菜单列表（组件内部处理缓存：命中直接返回，未命中查库并回填缓存）
        List<SysMenu> list = menuComponent.getMenuTree();
        // 2. 转换为 MenuTreeVO 并填充 statusText、menuTypeText
        List<MenuTreeVO> voList = list.stream()
                .map(menu -> {
                    MenuTreeVO vo = mapperFacade.map(menu, MenuTreeVO.class);
                    vo.setStatusText(MenuStatusEnum.getDescByCode(menu.getStatus()));
                    vo.setMenuTypeText(MenuTypeEnum.getDescByCode(menu.getMenuType()));
                    return vo;
                })
                .toList();
        // 3. 递归构建树（根节点 parentId = 0）
        List<MenuTreeVO> tree = buildTree(voList, 0L);
        return Result.success(tree);
    }

    /**
     * 递归构建菜单树
     *
     * @param list     所有菜单列表
     * @param parentId 当前层级的父ID
     * @return         子树列表
     */
    private List<MenuTreeVO> buildTree(List<MenuTreeVO> list, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();
        for (MenuTreeVO vo : list) {
            if (Objects.equals(vo.getParentId(), parentId)) {
                vo.setChildren(buildTree(list, vo.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 菜单选择列表
     *
     * <p>性能接口：仅返回 id、菜单名称两个字段，只查未删除的正常状态菜单。
     * 支持 keyword 模糊匹配菜单名称，用于下拉选择等场景。</p>
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param keyword   模糊匹配关键词（匹配菜单名称），可选
     * @return          菜单选择列表（分页）
     */
    @Operation(summary = "菜单选择列表")
    @SaCheckPermission("system:menu:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysMenu> paging = new Paging<>(page, pageSize);
        sysMenuService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(menu -> mapperFacade.map(menu, MenuSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 菜单详情
     *
     * @param id    菜单ID
     * @return      菜单详细信息
     */
    @Operation(summary = "菜单详情")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "菜单ID不能为空") Long id) {
        SysMenu sysMenu = sysMenuService.findById(id);
        MenuVO vo = mapperFacade.map(sysMenu, MenuVO.class);
        vo.setStatusText(MenuStatusEnum.getDescByCode(sysMenu.getStatus()));
        vo.setMenuTypeText(MenuTypeEnum.getDescByCode(sysMenu.getMenuType()));
        return Result.success(vo);
    }

    /**
     * 菜单相关枚举列表
     *
     * <p>返回菜单模块前端需要的枚举选项（状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "菜单相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(MenuStatusEnum.values())
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
     * 添加菜单
     *
     * <p>校验菜单类型及状态合法性后保存。</p>
     *
     * @param saveVO    菜单添加入参
     * @return          操作结果
     */
    @Operation(summary = "添加菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody MenuSaveVO saveVO) {
        // 1. 校验状态合法性
        Assert.notNull(MenuStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 2. 校验菜单类型合法性
        Assert.notNull(MenuTypeEnum.getByCode(saveVO.getMenuType()), "菜单类型值不合法（1目录 2菜单 3按钮）");
        // 3. 保存
        SysMenu sysMenu = mapperFacade.map(saveVO, SysMenu.class);
        sysMenu.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysMenuService.save(sysMenu);
        // 4. 清除菜单树缓存
        menuComponent.cleanMenuCache();
        menuComponent.cleanMenuTreeCache();
        return Result.success();
    }

    /**
     * 编辑菜单
     *
     * <p>修改菜单基本信息。</p>
     *
     * @param editVO    菜单编辑入参
     * @return          操作结果
     */
    @Operation(summary = "编辑菜单")
    @SaCheckPermission("system:menu:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody MenuEditVO editVO) {
        // 1. 校验菜单是否存在
        sysMenuService.findById(editVO.getId());
        // 2. 校验菜单类型合法性
        if (Objects.nonNull(editVO.getMenuType())) {
            Assert.notNull(MenuTypeEnum.getByCode(editVO.getMenuType()), "菜单类型值不合法（1目录 2菜单 3按钮）");
        }
        // 3. 更新
        SysMenu sysMenu = mapperFacade.map(editVO, SysMenu.class);
        sysMenu.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysMenuService.updateById(sysMenu);
        // 4. 清除菜单树缓存
        menuComponent.cleanMenuCache();
        menuComponent.cleanMenuTreeCache();
        return Result.success();
    }

    /**
     * 切换菜单状态
     *
     * <p>根据菜单当前状态取反：正常→禁用，禁用→正常。
     * 切换为禁用时异步清除菜单关联缓存（所有用户的权限/角色缓存）。</p>
     *
     * @param id    菜单ID
     * @return      操作结果
     */
    @Operation(summary = "切换菜单状态")
    @SaCheckPermission("system:menu:editStatus")
    @PostMapping("/editStatus")
    @JsonCoverParam
    public Result editStatus(@NotNull(message = "请选择需要操作的菜单") Long id) {
        // 1. 校验菜单是否存在
        SysMenu sysMenu = sysMenuService.findById(id);
        // 2. 根据当前状态取反
        Integer targetStatus = MenuStatusEnum.NORMAL.fromStatus(sysMenu.getStatus())
                ? MenuStatusEnum.DISABLED.getCode()
                : MenuStatusEnum.NORMAL.getCode();
        menuComponent.doSomethingInTransactional(() -> {
            sysMenuService.lambdaUpdate()
                    .eq(SysMenu::getId, id)
                    .set(SysMenu::getStatus, targetStatus)
                    .set(SysMenu::getUpdateBy, SecurityUser.getUserId())
                    .set(SysMenu::getUpdateTime, LocalDateTime.now())
                    .update();
            // 如果是顶级菜单，子菜单全部跟随父菜单状态
            if (sysMenu.getParentId() == 0) {
                sysMenuService.lambdaUpdate()
                        .eq(SysMenu::getParentId, id)
                        .set(SysMenu::getStatus, targetStatus)
                        .update();
            }
            return true;
        });
        // 3. 异步清除菜单关联缓存（用户权限/角色）与菜单树缓存
        menuComponent.cleanMenuCache();
        menuComponent.cleanMenuTreeCache();
        return Result.success();
    }

    /**
     * 删除菜单（逻辑删除）
     *
     * @param id    菜单ID
     * @return      操作结果
     */
    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的菜单") Long id) {
        // 1. 校验菜单是否存在
        sysMenuService.findById(id);
        // 2. 逻辑删除
        sysMenuService.lambdaUpdate()
                .eq(SysMenu::getId, id)
                .set(SysMenu::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysMenu::getUpdateBy, SecurityUser.getUserId())
                .set(SysMenu::getUpdateTime, LocalDateTime.now())
                .update();
        // 3. 异步清除菜单关联缓存（用户权限/角色）与菜单树缓存
        menuComponent.cleanMenuCache();
        menuComponent.cleanMenuTreeCache();
        return Result.success();
    }

    /**
     * 菜单排序（仅同级）
     *
     * <p>前端拖拽调整<b>同一层级</b>内菜单的顺序后，传入该层级的 parentId、
     * 菜单ID列表（按排序顺序），后端按 index + 1 依次为同级菜单赋值 displayOrder。</p>
     *
     * <p>约束：传入的所有菜单必须同属一个父级（parentId），否则拒绝排序，
     * 以保证仅在同级范围内调整顺序，不会跨层级移动菜单。
     * 在事务中执行，保证所有排序值要么全部更新成功，要么全部回滚。</p>
     *
     * @param sortVO    排序入参（父菜单ID + 同级菜单ID列表）
     * @return          操作结果
     */
    @Operation(summary = "菜单排序")
    @SaCheckPermission("system:menu:sort")
    @PostMapping("/sort")
    public Result sort(@Valid @RequestBody MenuSortVO sortVO) {
        List<Long> ids = sortVO.getIds();
        Long parentId = sortVO.getParentId();
        // 1. 校验：传入的菜单必须都存在且同属一个父级（仅同级排序）
        List<SysMenu> menus = sysMenuService.lambdaQuery()
                .in(SysMenu::getId, ids)
                .list();
        Assert.isTrue(menus.size() == ids.size(), "存在无效的菜单ID");
        boolean allSameLevel = menus.stream()
                .allMatch(menu -> Objects.equals(menu.getParentId(), parentId));
        Assert.isTrue(allSameLevel, "仅支持同级排序，请勿跨层级排序");
        // 2. 在事务中按数组顺序依次赋值 displayOrder（1,2,3...）
        AtomicInteger idx = new AtomicInteger(1);
        menuComponent.doSomethingInTransactional(() -> {
            List<SysMenu> menuList = ids.stream()
                    .map(id -> {
                        return new SysMenu()
                                .setId(id)
                                .setDisplayOrder(idx.getAndIncrement())
                                .setUpdateBy(SecurityUser.getUserId())
                                .setUpdateTime(LocalDateTime.now());
                    }).toList();
            sysMenuService.updateBatchById(menuList);
            return true;
        });
        // 3. 清除菜单树缓存
        menuComponent.cleanMenuCache();
        menuComponent.cleanMenuTreeCache();
        return Result.success();
    }
}
