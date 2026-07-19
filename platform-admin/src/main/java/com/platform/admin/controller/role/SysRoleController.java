package com.platform.admin.controller.role;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.role.RoleEditVO;
import com.platform.admin.vo.role.RoleMenuAssignVO;
import com.platform.admin.vo.role.RoleSaveVO;
import com.platform.admin.vo.role.RoleSelectVO;
import com.platform.admin.vo.role.RoleSortVO;
import com.platform.admin.vo.role.RoleVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysRole;
import com.platform.common.entity.admin.SysRoleMenu;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.RoleStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.role.RoleComponent;
import com.platform.service.service.SysMenuService;
import com.platform.service.service.SysRoleMenuService;
import com.platform.service.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 *
 * <p>提供角色列表查询、角色添加、角色编辑、角色删除、角色详情等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /role/page   - 角色列表（分页查询）</li>
 *   <li>GET  /role/select-list - 角色选择列表（性能查询，仅返回id/角色名称）</li>
 *   <li>GET  /role/view   - 角色详情</li>
 *   <li>GET  /role/enums  - 角色相关枚举列表（状态）</li>
 *   <li>POST /role/add    - 添加角色</li>
 *   <li>POST /role/edit   - 编辑角色</li>
 *   <li>POST /role/editStatus - 切换角色状态（禁用/激活）</li>
 *   <li>POST /role/delete - 删除角色（逻辑删除）</li>
 *   <li>POST /role/sort    - 批量排序角色</li>
 *   <li>POST /role/assignMenus - 分配角色菜单</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "角色管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/role")
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysMenuService sysMenuService;
    private final MapperFacade mapperFacade;
    private final RoleComponent roleComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询角色列表
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param status    状态（1正常 0禁用），可选筛选条件
     * @return          角色分页列表
     */
    @Operation(summary = "角色列表")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysRole> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysRoleService.paging(paging, paramsMap);
        paging.convert(role -> {
            RoleVO vo = mapperFacade.map(role, RoleVO.class);
            vo.setStatusText(RoleStatusEnum.getDescByCode(role.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 角色选择列表
     *
     * <p>性能接口：仅返回 id、角色名称两个字段，只查未删除的正常状态角色。
     * 支持 keyword 模糊匹配角色名称，用于下拉选择、分配角色等场景。</p>
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param keyword   模糊匹配关键词（匹配角色名称），可选
     * @return          角色选择列表（分页）
     */
    @Operation(summary = "角色选择列表")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysRole> paging = new Paging<>(page, pageSize);
        sysRoleService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(role -> mapperFacade.map(role, RoleSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 角色详情
     *
     * @param id    角色ID
     * @return      角色详细信息
     */
    @Operation(summary = "角色详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "角色ID不能为空") Long id) {
        SysRole sysRole = sysRoleService.findById(id);
        RoleVO vo = mapperFacade.map(sysRole, RoleVO.class);
        vo.setStatusText(RoleStatusEnum.getDescByCode(sysRole.getStatus()));
        return Result.success(vo);
    }

    /**
     * 获取角色的菜单
     *
     * @param id    角色ID
     * @return      角色的菜单
     */
    @Operation(summary = "获取角色的菜单")
    @GetMapping("/menuIds")
    public Result menuIds(@NotNull(message = "角色id不能为空") Long id) {
        sysRoleService.findById(id);
        Set<Long> menuIds = sysRoleMenuService.lambdaQuery()
                .eq(SysRoleMenu::getRoleId, id)
                .list().stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
        return Result.success(menuIds);
    }

    /**
     * 角色相关枚举列表
     *
     * <p>返回角色模块前端需要的枚举选项（状态），供下拉选择使用。</p>
     *
     * @return 枚举选项集合（key: 枚举类型, value: 选项列表）
     */
    @Operation(summary = "角色相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(RoleStatusEnum.values())
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
     * 添加角色
     *
     * <p>校验角色标识是否重复及状态合法性后保存。</p>
     *
     * @param saveVO    角色添加入参
     * @return          操作结果
     */
    @Operation(summary = "添加角色")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody RoleSaveVO saveVO) {
        // 1. 校验状态合法性
        Assert.notNull(RoleStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 2. 校验角色标识是否重复
        checkRoleCodeNotExist(saveVO.getRoleCode());
        // 3. 保存
        SysRole sysRole = mapperFacade.map(saveVO, SysRole.class);
        sysRole.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysRoleService.save(sysRole);
        roleComponent.cleanRoleCache();
        return Result.success();
    }

    /**
     * 编辑角色
     *
     * <p>修改角色基本信息（名称、标识、排序、状态、备注）。</p>
     *
     * @param editVO    角色编辑入参
     * @return          操作结果
     */
    @Operation(summary = "编辑角色")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody RoleEditVO editVO) {
        // 1. 校验角色是否存在
        sysRoleService.findById(editVO.getId());
        // 2. 更新
        SysRole sysRole = mapperFacade.map(editVO, SysRole.class);
        sysRole.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysRoleService.updateById(sysRole);
        roleComponent.cleanRoleCache();
        return Result.success();
    }

    /**
     * 切换角色状态
     *
     * <p>根据角色当前状态取反：正常→禁用，禁用→正常。
     * 切换为禁用时异步清除角色关联缓存（所有用户的权限/角色缓存），
     * 复用 {@link RoleComponent#cleanRoleCache()}，防止已禁用角色的权限继续生效。</p>
     *
     * @param id    角色ID
     * @return      操作结果
     */
    @Operation(summary = "切换角色状态")
    @PostMapping("/editStatus")
    @JsonCoverParam
    public Result editStatus(@NotNull(message = "请选择需要操作的角色") Long id) {
        // 1. 校验角色是否存在
        SysRole sysRole = sysRoleService.findById(id);
        // 2. 根据当前状态取反
        Integer targetStatus = RoleStatusEnum.NORMAL.fromStatus(sysRole.getStatus())
                ? RoleStatusEnum.DISABLED.getCode()
                : RoleStatusEnum.NORMAL.getCode();
        sysRoleService.lambdaUpdate()
                .eq(SysRole::getId, id)
                .set(SysRole::getStatus, targetStatus)
                .set(SysRole::getUpdateBy, SecurityUser.getUserId())
                .set(SysRole::getUpdateTime, LocalDateTime.now())
                .update();
        roleComponent.cleanRoleCache();
        return Result.success();
    }

    /**
     * 删除角色（逻辑删除）
     *
     * @param id    角色ID
     * @return      操作结果
     */
    @Operation(summary = "删除角色")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的角色") Long id) {
        // 1. 校验角色是否存在
        sysRoleService.findById(id);
        // 2. 逻辑删除
        sysRoleService.lambdaUpdate()
                .eq(SysRole::getId, id)
                .set(SysRole::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysRole::getUpdateBy, SecurityUser.getUserId())
                .set(SysRole::getUpdateTime, LocalDateTime.now())
                .update();
        roleComponent.cleanRoleCache();
        return Result.success();
    }

    /**
     * 批量排序角色
     *
     * <p>前端拖拽排序后，传入角色ID列表（按排序顺序）和起始排序值。
     * 后端按 startOrder + index 赋值 display_order。
     * 分页场景下前端传当前页的起始排序值，各页互不干扰。
     * 在事务中执行，保证所有排序值要么全部更新成功，要么全部回滚。</p>
     *
     * @param sortVO    排序入参（起始排序值 + 角色ID列表）
     * @return          操作结果
     */
    @Operation(summary = "批量排序角色")
    @PostMapping("/sort")
    public Result sort(@Valid @RequestBody RoleSortVO sortVO) {
        List<Long> ids = sortVO.getIds();
        AtomicInteger idx = new AtomicInteger(sortVO.getStartOrder());

        List<SysRole> roleList = ids.stream()
                .map(id -> {
                    return new SysRole()
                            .setId(id)
                            .setDisplayOrder(idx.incrementAndGet())
                            .setUpdateBy(SecurityUser.getUserId())
                            .setUpdateTime(LocalDateTime.now());
                }).toList();
        sysRoleService.updateBatchById(roleList);
        return Result.success();
    }

    /**
     * 分配角色菜单
     *
     * <p>为指定角色全量分配菜单权限：先删除角色原有的菜单关联，再批量插入新的关联记录。
     * 分配后异步清除角色关联缓存（所有用户的权限/角色缓存）。</p>
     *
     * @param assignVO  角色分配菜单入参（角色ID + 菜单ID列表）
     * @return          操作结果
     */
    @Operation(summary = "分配角色菜单")
    @PostMapping("/assignMenus")
    public Result assignMenus(@Valid @RequestBody RoleMenuAssignVO assignVO) {
        // 1. 校验角色是否存在
        sysRoleService.findById(assignVO.getRoleId());
        // 2. 校验菜单ID是否合法
        if (CollUtil.isNotEmpty(assignVO.getMenuIds())) {
            for (Long menuId : assignVO.getMenuIds()) {
                sysMenuService.findById(menuId);
            }
        }
        // 3. 在事务中执行：删除旧关联 + 批量插入新关联
        roleComponent.doSomethingInTransactional(() -> {
            sysRoleMenuService.lambdaUpdate()
                    .eq(SysRoleMenu::getRoleId, assignVO.getRoleId())
                    .remove();
            if (CollUtil.isNotEmpty(assignVO.getMenuIds())) {
                List<SysRoleMenu> menus = assignVO.getMenuIds().stream()
                        .map(menuId -> new SysRoleMenu()
                                .setRoleId(assignVO.getRoleId())
                                .setMenuId(menuId))
                        .toList();
                sysRoleMenuService.saveBatch(menus);
            }
            return true;
        });
        // 4. 异步清除角色关联缓存
        roleComponent.cleanRoleCache();
        return Result.success();
    }

    // ============================ 私有校验方法 ============================

    /**
     * 校验角色标识是否已存在
     *
     * @param roleCode  角色标识
     */
    private void checkRoleCodeNotExist(String roleCode) {
        SysRole existSysRole = sysRoleService.lambdaQuery()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.isNull(existSysRole, "角色标识已存在:{}", roleCode);
    }
}
