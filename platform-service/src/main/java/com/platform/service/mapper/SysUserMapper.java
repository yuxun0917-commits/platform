package com.platform.service.mapper;

import com.platform.common.entity.admin.SysRole;
import com.platform.common.entity.admin.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.common.result.Paging;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 找到用户的角色集合
     * @param loginId   id
     * @return  roles
     */
    List<SysRole> listRoleById(@Param("loginId") Long loginId);

    /**
     * 分页查询用户
     * @param paging        分页
     * @param paramsMap     查询参数
     */
    Paging<SysUser> paging(@Param("paging") Paging<SysUser> paging, @Param("paramsMap") Map<String, Object> paramsMap);
}
