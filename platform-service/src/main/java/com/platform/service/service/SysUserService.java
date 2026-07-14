package com.platform.service.service;

import com.platform.common.entity.admin.SysRole;
import com.platform.common.entity.admin.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 通过id找到正常User
     *
     * @param loginId 用户id
     * @return 用户实体
     */
    SysUser findById(Long loginId);

    /**
     * 找到用户的所有角色
     *
     * @param loginId 用户id
     * @return 角色列表
     */
    List<SysRole> listRoleById(Long loginId);

    /**
     * 登录校验
     *
     * <p>校验用户名是否存在、密码是否正确、账号是否禁用，
     * 校验通过返回用户实体（不含登录态处理，由 Controller 调用 Sa-Token 完成登录）。</p>
     *
     * @param username 用户名
     * @param password 密码明文
     * @return 校验通过的用户实体
     */
    SysUser login(String username, String password);

    /**
     * 分页查询
     * @param paging        分页
     * @param paramsMap     查询参数
     */
    void paging(Paging<SysUser> paging, Map<String, Object> paramsMap);

    /**
     * 用户选择列表（性能查询）
     *
     * <p>仅查询 id、昵称、部门名称三个字段，只返回未删除的正常状态用户。
     * 用于下拉选择等性能敏感场景。支持按昵称、部门名称模糊匹配。</p>
     *
     * @param paging    分页对象
     * @param keyword   模糊匹配关键词（匹配昵称或部门名称），可为null
     */
    void selectList(Paging<SysUser> paging, String keyword);
}
