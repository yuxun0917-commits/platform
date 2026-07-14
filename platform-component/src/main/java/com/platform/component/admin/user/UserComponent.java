package com.platform.component.admin.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.platform.common.bo.UserInfoBO;
import com.platform.common.constant.RedisConstant;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.entity.admin.SysRole;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.event.AllUserCacheDeleteEvent;
import com.platform.common.event.UserCacheDeleteEvent;
import com.platform.common.utils.JacksonUtil;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysRoleService;
import com.platform.service.service.SysUserService;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用户业务组合组件
 *
 * <p>封装用户相关的跨模块组合操作，供 Controller 层调用。
 * 典型场景：删除用户 / 禁用用户后，需要踢下线 + 清缓存，
 * 这类后置清理逻辑在多个接口中复用，抽到组件层统一维护。</p>
 *
 * <p>用户详情聚合（/user/info）的数据聚合与缓存读写也统一由本组件负责，
 * Controller 层只负责将聚合结果转换为对外展示的 VO。</p>
 *
 * <p>与 Service 层的区别：</p>
 * <ul>
 *   <li>Service 层：纯数据操作（CRUD），不涉及认证框架和缓存</li>
 *   <li>Component 层：组合多个基础设施（Sa-Token、Redis、AsyncManager）完成业务动作</li>
 * </ul>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserComponent {

    private final AsyncManager asyncManager;
    private final RedisUtil redisUtil;
    private final MapperFacade mapperFacade;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    /**
     * 在事务中运行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 获取用户详情聚合数据（基本信息 + 角色 + 权限 + 菜单扁平列表）
     *
     * <p>缓存策略：以 JSON 字符串形式缓存聚合结果，key 为 {@code sys:user:info:{userId}}。
     * 优先读缓存；缓存未命中则查库聚合（基本信息、角色列表、角色关联的菜单，
     * 并由菜单 perms 去重推导权限），写回缓存后返回。缓存的读写由本方法统一负责，
     * Controller 层无需关心缓存细节，仅负责将结果转换为对外展示的 VO。</p>
     *
     * <p>聚合数据使用 {@link UserInfoBO} 的内部 BO 承载（{@code UserBO}/{@code RoleBO}/{@code MenuBO}），
     * 不直接引用 entity，避免污染实体结构；菜单在组件层直接构建为树形结构
     * （{@link UserInfoBO.MenuBO#getChildren()} 挂载子节点，根节点 parentId 为 0），
     * 与菜单管理的建树算法保持一致，Controller 仅需做展示层 VO 转换。
     * 用户角色/权限/状态变更时由 {@link #cleanUserCache} / {@link #cleanUserCacheAndSession}
     * 异步失效该缓存。</p>
     *
     * @param userId 用户ID
     * @return 用户详情聚合数据
     */
    public UserInfoBO getUserInfo(Long userId) {
        // 1. 优先读缓存
        Object cached = redisUtil.get(RedisConstant.USER_INFO + userId);
        if (Objects.nonNull(cached)) {
            return JacksonUtil.parseObj(cached.toString(), UserInfoBO.class);
        }
        // 2. 缓存未命中：基本信息（findById 包含存在性校验）
        SysUser sysUser = sysUserService.findById(userId);
        // 3. 角色列表
        List<SysRole> roles = sysUserService.listRoleById(userId);
        // 4. 菜单（由角色关联的菜单推导，无角色时防空 IN()）
        Set<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());
        List<SysMenu> menus = roleIds.isEmpty()
                ? Collections.emptyList()
                : sysRoleService.listMenuByIds(roleIds);
        // 5. 权限标识（菜单中非空的 perms 去重）
        List<String> permissions = menus.stream()
                .map(SysMenu::getPerms)
                .filter(perm -> Objects.nonNull(perm) && !perm.isBlank())
                .distinct()
                .toList();
        // 6. 聚合为内部 BO（不引用 entity，避免污染实体结构）
        List<UserInfoBO.MenuBO> menuBOList = menus.stream()
                .map(menu -> mapperFacade.map(menu, UserInfoBO.MenuBO.class))
                .toList();
        UserInfoBO bo = new UserInfoBO();
        bo.setUser(mapperFacade.map(sysUser, UserInfoBO.UserBO.class));
        bo.setRoles(roles.stream()
                .map(role -> mapperFacade.map(role, UserInfoBO.RoleBO.class))
                .toList());
        bo.setPermissions(permissions);
        // 组件层直接构建菜单树（根节点 parentId = 0），与菜单管理建树算法一致
        bo.setMenus(buildMenuTree(menuBOList, 0L));
        // 7. 写回缓存（TTL 走全局默认）
        redisUtil.set(RedisConstant.USER_INFO + userId, JacksonUtil.toJsonString(bo), 30, TimeUnit.MINUTES);
        return bo;
    }

    /**
     * 递归构建菜单树（组件层通用建树，与菜单管理算法一致）
     *
     * <p>从扁平菜单列表构建树形结构，将子节点挂到父节点的 {@link UserInfoBO.MenuBO#getChildren()} 上，
     * 返回以指定 parentId 为父节点的子树。节点类型为内部 BO（{@link UserInfoBO.MenuBO}），
     * 不依赖 admin 层 VO，可在组件层直接使用。</p>
     *
     * @param list     扁平菜单列表
     * @param parentId 当前层级父ID
     * @return 子树列表
     */
    private List<UserInfoBO.MenuBO> buildMenuTree(List<UserInfoBO.MenuBO> list, Long parentId) {
        List<UserInfoBO.MenuBO> tree = new ArrayList<>();
        for (UserInfoBO.MenuBO menu : list) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                menu.setChildren(buildMenuTree(list, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 监听用户缓存删除事件，统一清除该用户在 Redis 中的权限、角色与详情缓存。
     *
     * <p>缓存 key 的维护集中在此处，调用方（如 {@link #cleanUserCache} /
     * {@link #cleanUserCacheAndSession}）只需发布事件，无需感知具体 key：</p>
     * <ul>
     *   <li>权限缓存：{@code user:auth:perm:{userId}}</li>
     *   <li>角色缓存：{@code user:auth:role:{userId}}</li>
     *   <li>用户详情缓存：{@code sys:user:info:{userId}}</li>
     * </ul>
     *
     * <p>事件由 {@link #cleanUserCache} / {@link #cleanUserCacheAndSession} 在异步线程中发布，
     * 本监听器随发布线程同步执行，因此实际清理在异步线程完成，不阻塞主流程。</p>
     *
     * @param event 用户缓存删除事件
     */
    @EventListener
    public void onUserCacheDeleteEvent(UserCacheDeleteEvent event) {
        Long userId = event.getUserId();
        log.info("清除用户缓存, userId={}", userId);
        redisUtil.delete(RedisConstant.USER_AUTH_PERM + userId);
        redisUtil.delete(RedisConstant.USER_AUTH_ROLE + userId);
        redisUtil.delete(RedisConstant.USER_INFO + userId);
        log.info("清除用户缓存完成, userId={}", userId);
    }

    /**
     * 监听所有用户缓存删除事件，清除 Redis 中所有用户的权限、角色与详情缓存。
     *
     * <p>缓存 key 的维护集中在此处，调用方（{@link #cleanAllUserCache}）只需发布事件，
     * 无需感知具体 key 与前缀：</p>
     * <ul>
     *   <li>权限缓存：{@code user:auth:perm:*}</li>
     *   <li>角色缓存：{@code user:auth:role:*}</li>
     *   <li>用户详情缓存：{@code sys:user:info:*}</li>
     * </ul>
     *
     * <p>基于 key 前缀模式批量匹配删除；{@code RedisUtil#keys} 返回去前缀的 key，
     * 经 {@code RedisUtil#delete(Collection)} 再次 wrap 后删除，前缀处理配套正确。
     * 事件由 {@link #cleanAllUserCache} 在异步线程中发布，本监听器随发布线程同步执行，
     * 实际清理在异步线程完成，不阻塞主流程。</p>
     *
     * @param event 所有用户缓存删除事件
     */
    @EventListener
    public void onAllUserCacheDeleteEvent(AllUserCacheDeleteEvent event) {
        log.info("清除所有用户缓存 (perm/role/info)");
        redisUtil.delete(redisUtil.keys(RedisConstant.USER_AUTH_PERM + "*"));
        redisUtil.delete(redisUtil.keys(RedisConstant.USER_AUTH_ROLE + "*"));
        redisUtil.delete(redisUtil.keys(RedisConstant.USER_INFO + "*"));
        log.info("所有用户缓存清除完成！");
    }

    /**
     * 异步清理用户会话与缓存
     *
     * <p>在删除用户、禁用用户等场景中复用：踢用户下线，并发布
     * {@link UserCacheDeleteEvent} 清除权限/角色/详情缓存。
     * 踢下线与事件发布均在异步线程执行，不阻塞主流程。</p>
     *
     * @param userId 用户ID
     */
    public void cleanUserCacheAndSession(Long userId) {
        asyncManager.execute(() -> {
            // 踢下线
            if (StpUtil.isLogin(userId)) {
                StpUtil.kickout(userId);
            }
            // 发布事件，由监听器清除缓存
            SpringUtil.getApplicationContext().publishEvent(new UserCacheDeleteEvent(userId));
        });
    }

    /**
     * 异步清除用户权限/角色缓存（不踢下线）
     *
     * <p>在分配角色、修改权限等场景中复用：仅发布 {@link UserCacheDeleteEvent} 清除缓存，
     * 不踢用户下线。用户下次访问时从数据库重新加载最新权限。异步执行，不阻塞主流程。</p>
     *
     * @param userId 用户ID
     */
    public void cleanUserCache(Long userId) {
        asyncManager.execute(() -> SpringUtil.getApplicationContext().publishEvent(new UserCacheDeleteEvent(userId)));
    }

    /**
     * 异步清除所有用户的权限/角色/详情缓存
     *
     * <p>在影响全部用户的全局配置或权限结构变更场景中复用：发布
     * {@link AllUserCacheDeleteEvent}，由监听器基于 key 前缀批量清除所有用户缓存。
     * 用户下次访问时从数据库重新加载最新权限。异步执行，不阻塞主流程。</p>
     */
    public void cleanAllUserCache() {
        asyncManager.execute(() -> SpringUtil.getApplicationContext().publishEvent(new AllUserCacheDeleteEvent(this)));
    }
}
