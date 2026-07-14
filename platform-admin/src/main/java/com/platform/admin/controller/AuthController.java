package com.platform.admin.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.platform.admin.vo.LoginVO;
import com.platform.admin.vo.SecondAuthVO;
import com.platform.common.constant.RedisConstant;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.SysConfigKeyEnum;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.component.admin.config.SysConfigComponent;
import com.platform.component.admin.user.UserComponent;
import com.platform.component.captcha.CaptchaComponent;
import com.platform.service.service.SysUserService;
import com.platform.starter.redis.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Tag(name = "认证授权管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final CaptchaComponent captchaComponent;
    private final SysConfigComponent sysConfigComponent;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final UserComponent userComponent;

    /**
     * 用户登录
     *
     * <p>先校验图形验证码，再校验用户名、密码、启用状态，登录成功返回 Sa-Token 令牌。</p>
     *
     * @param loginVO 登录入参
     * @return 登录结果（Token + 用户基本信息）
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginVO loginVO) {
        String byPassCaptcha = sysConfigComponent.getConfig(SysConfigKeyEnum.BYPASS_CAPTCHA.getConfigKey());
        // 1. 校验图形验证码
        if (!Objects.equals(loginVO.getCaptchaCode(), byPassCaptcha)) {
            captchaComponent.verify(loginVO.getCaptchaKey(), loginVO.getCaptchaCode());
        }
        // 2. Service 层校验用户名、密码、禁用状态
        SysUser sysUser = sysUserService.login(loginVO.getUsername(), loginVO.getPassword());
        // 3. Sa-Token 登录，生成令牌
        StpUtil.login(sysUser.getId());
        StpUtil.getSession().set("username", sysUser.getUsername());
        StpUtil.getSession().set("nickname", sysUser.getNickname());
        return Result.success(StpUtil.getTokenValue());
    }

    /**
     * 用户登出
     *
     * <p>注销当前登录的 Sa-Token 会话（Token + Session），后续请求需重新登录。</p>
     *
     * @return 操作结果
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result logout() {
        Long userId = SecurityUser.getUserId();
        StpUtil.logout();
        // 发布事件，由监听器清除当前用户缓存
        userComponent.cleanUserCache(userId);
        return Result.success();
    }

    /**
     * 是否需要二次认证
     *
     * @return Result
     */
    @Operation(summary = "是否需要二次认证")
    @GetMapping("/is-need/second-factor/verify")
    public Result isNeedSecondAuth() {
        if (!redisUtil.hasKey(RedisConstant.SECOND_AUTH_LOCK + SecurityUser.getUserId())) {
            throw new BusinessException(ErrorCode.SECOND_AUTH);
        }
        return Result.success();
    }

    /**
     * 二次认证
     *
     * @param secondAuthVO 二次认证入参
     * @return 登录结果（Token + 用户基本信息）
     */
    @Operation(summary = "二次认证")
    @PostMapping("/second-factor/verify")
    public Result secondAuth(@Valid @RequestBody SecondAuthVO secondAuthVO) {
        SysUser sysUser = sysUserService.findById(SecurityUser.getUserId());
        Assert.isTrue(
                passwordEncoder.matches(secondAuthVO.getPassword(), sysUser.getPassword()),
                 "认证失败，密码错误！"
        );
        redisUtil.set(RedisConstant.SECOND_AUTH_LOCK + SecurityUser.getUserId(), 1, 30, TimeUnit.MINUTES);
        return Result.success();
    }
}
