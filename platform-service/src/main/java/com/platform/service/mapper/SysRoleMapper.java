package com.platform.service.mapper;

import com.platform.common.entity.admin.SysMenu;
import com.platform.common.entity.admin.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysMenu> listMenuByIds(@Param("ids") Set<Long> ids);
}
