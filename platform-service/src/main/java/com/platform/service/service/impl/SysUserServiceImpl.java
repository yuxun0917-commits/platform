package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysRole;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.UserStatusEnum;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysUserMapper;
import com.platform.service.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-06-28
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser findById(Long loginId) {
        SysUser sysUser = lambdaQuery()
                .eq(SysUser::getId, loginId)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(sysUser, "无效的用户id:{}", loginId);
        return sysUser;
    }

    @Override
    public List<SysRole> listRoleById(Long loginId) {
        return sysUserMapper.listRoleById(loginId);
    }

    @Override
    public SysUser login(String username, String password) {
        // 1. 根据用户名查询未删除的用户
        SysUser sysUser = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(sysUser, () -> {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        });
        // 2. 判断用户是否已禁用
        Assert.isTrue(UserStatusEnum.NORMAL.fromStatus(sysUser.getStatus()), () -> {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        });
        // 3. 校验密码（BCrypt）
        Assert.isTrue(passwordEncoder.matches(password, sysUser.getPassword()), () -> {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        });
        return sysUser;
    }

    @Override
    public void paging(Paging<SysUser> paging, Map<String, Object> paramsMap) {
        sysUserMapper.paging(paging, paramsMap);
    }

    @Override
    public void selectList(Paging<SysUser> paging, String keyword) {
        lambdaQuery()
                .select(SysUser::getId, SysUser::getNickname, SysUser::getDeptName)
                .eq(SysUser::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysUser::getStatus, UserStatusEnum.NORMAL.getCode())
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysUser::getNickname, keyword)
                                .or()
                                .like(SysUser::getDeptName, keyword))
                .orderByDesc(SysUser::getId)
                .page(paging);
    }
}
