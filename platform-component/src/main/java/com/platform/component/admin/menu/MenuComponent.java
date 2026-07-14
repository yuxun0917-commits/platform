package com.platform.component.admin.menu;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.common.constant.RedisConstant;
import com.platform.common.entity.admin.SysMenu;
import com.platform.common.event.AllUserCacheDeleteEvent;
import com.platform.common.utils.JacksonUtil;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysMenuService;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 菜单业务组合组件
 *
 * <p>封装菜单相关的跨模块组合操作，供 Controller 层调用。
 * 典型场景：删除菜单 / 禁用菜单后，需要清除所有用户的权限/角色缓存。</p>
 *
 * <p>缓存策略：菜单树（所有未删除菜单的扁平列表）以 JSON 字符串形式缓存到 Redis，
 * key 为 {@code menu:tree}。读缓存、未命中查库、回填缓存、返回数据全部由
 * {@link #getMenuTree()} 统一负责，Controller 层只负责将结果转换为树形 VO，无需关心缓存细节。
 * 增删改菜单后异步清除缓存，下次查询时重建。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuComponent {

    private final AsyncManager asyncManager;
    private final RedisUtil redisUtil;
    private final SysMenuService sysMenuService;

    /**
     * 在事务中运行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 异步清除菜单关联缓存
     *
     * <p>在删除菜单、禁用菜单等场景中复用：清除所有用户的权限缓存和角色缓存。
     * 异步执行，不阻塞主流程。</p>
     */
    public void cleanMenuCache() {
        asyncManager.execute(() -> {
            // 清除所有用户的权限与角色缓存
            SpringUtil.getApplicationContext().publishEvent(new AllUserCacheDeleteEvent(this));
        });
    }

    /**
     * 读取菜单树缓存（缓存未命中时查询数据库并回填缓存）
     *
     * <p>缓存策略：以 JSON 字符串形式缓存所有未删除菜单的扁平列表，key 为 {@code menu:tree}。
     * 优先读缓存；缓存未命中则查询数据库（{@code listTree}），写回缓存后返回。
     * 缓存的读写由本方法统一负责，Controller 层无需关心缓存细节，仅负责将结果转换为树形 VO。</p>
     *
     * @return 菜单扁平列表（未删除），供 Controller 构建树形结构
     */
    public List<SysMenu> getMenuTree() {
        // 1. 优先读缓存
        Object cached = redisUtil.get(RedisConstant.MENU_TREE);
        if (Objects.nonNull(cached)) {
            return JacksonUtil.parseTypeRef(cached.toString(), new TypeReference<List<SysMenu>>() {});
        }
        // 2. 缓存未命中：查询所有未删除菜单（已按 displayOrder 升序、id 倒序排序）
        List<SysMenu> list = sysMenuService.listTree();
        // 3. 写回缓存
        redisUtil.set(RedisConstant.MENU_TREE, JacksonUtil.toJsonString(list));
        return list;
    }

    /**
     * 异步清除菜单树缓存
     *
     * <p>在添加、编辑、删除、切换状态等场景中复用。
     * 异步执行，不阻塞主流程。</p>
     */
    public void cleanMenuTreeCache() {
        asyncManager.execute(() -> {
            redisUtil.delete(RedisConstant.MENU_TREE);
            log.info("[MenuCache] 菜单树缓存已清除");
        });
    }
}
