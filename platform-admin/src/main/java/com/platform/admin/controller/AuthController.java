package com.platform.admin.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.platform.admin.vo.LoginVO;
import com.platform.admin.vo.SecondAuthVO;
import com.platform.common.constant.CommonConstant;
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
import com.platform.starter.security.RsaComponent;
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
    private final RsaComponent rsaComponent;

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
        // 2. 前端必须用 RSA 公钥加密，此处无条件解密
        String username = rsaComponent.decryptByPrivateKey(loginVO.getUsername());
        String password = rsaComponent.decryptByPrivateKey(loginVO.getPassword());
        // 3. Service 层校验用户名、密码、禁用状态
        SysUser sysUser = sysUserService.login(username, password);
        // 4. Sa-Token 登录，生成令牌
        StpUtil.login(sysUser.getId());
        StpUtil.getSession().set(CommonConstant.SESSION_USERNAME_KEY, sysUser.getUsername());
        StpUtil.getSession().set(CommonConstant.SESSION_NICKNAME_KEY, sysUser.getNickname());
        if (Objects.nonNull(sysUser.getPasswordUpdateTime())) {
            StpUtil.getSession().set(CommonConstant.SESSION_PWD_UPDATE_KEY, sysUser.getPasswordUpdateTime());
        }
        return Result.success(StpUtil.getTokenValue());
    }

    /**
     * 获取 RSA 公钥（Base64）
     *
     * <p>前端登录 / 二次认证前调用此接口拿到公钥，用其对用户名、密码加密后再提交，
     * 避免凭据以明文出现在网络中。公钥可公开获取，配对私钥仅驻留后端内存。</p>
     *
     * @return RSA 公钥（Base64 编码的 X.509 格式）
     */
    @Operation(summary = "获取 RSA 公钥")
    @GetMapping("/rsa/public-key")
    public Result rsaPublicKey() {
        return Result.success(rsaComponent.getPublicKeyBase64());
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
        // 前端必须用 RSA 公钥加密，此处无条件解密
        String password = rsaComponent.decryptByPrivateKey(secondAuthVO.getPassword());
        SysUser sysUser = sysUserService.findById(SecurityUser.getUserId());
        Assert.isTrue(
                passwordEncoder.matches(password, sysUser.getPassword()),
                 "认证失败，密码错误！"
        );
        redisUtil.set(RedisConstant.SECOND_AUTH_LOCK + SecurityUser.getUserId(), 1, 30, TimeUnit.MINUTES);
        return Result.success();
    }
}
