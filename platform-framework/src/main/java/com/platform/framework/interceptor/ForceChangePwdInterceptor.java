package com.platform.framework.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.common.constant.CommonConstant;
import com.platform.common.constant.RedisConstant;
import com.platform.common.entity.admin.SysConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.SysConfigKeyEnum;
import com.platform.common.exception.BusinessException;
import com.platform.service.service.SysConfigService;
import com.platform.starter.redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 强制修改密码拦截器
 *
 * <p>登录态校验通过后，对已登录用户判断是否处于"必须修改密码"状态，
 * 抛出 {@link ErrorCode#NEED_CHANGE_PWD} 阻止继续访问，直到用户自主改密。</p>
 *
 * <p>判定规则（合并两个需求，不新增字段）：</p>
 * <ol>
 *     <li>新用户：{@code password_update_time} 为 null（新增用户默认不写该字段）→ 强制改密；</li>
 *     <li>定期过期：距上次修改密码已超过系统参数 {@code sys.password.expireDays} 天（默认 30）→ 强制改密。</li>
 * </ol>
 *
 * <p>密码修改时间直接从登录时写入 Sa-Session 的
 * {@link CommonConstant#SESSION_PWD_UPDATE_KEY} 读取，每个请求零额外开销、不查库；
 * 过期天数从系统参数 {@code sys.password.expireDays} 读取（Redis 缓存 3 天 TTL），
 * 未命中才查 {@link SysConfigService}。</p>
 *
 * <p>用户通过 {@code /user/reset-pwd}（或 {@code /user/changePassword}）改密后，
 * 需更新对应 Sa-Session 中的密码修改时间（见 {@link CommonConstant#SESSION_PWD_UPDATE_KEY}），
 * 下次请求即重新判定。</p>
 */
public class ForceChangePwdInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;
    private final SysConfigService sysConfigService;

    /** 系统参数读取失败时的默认值 */
    private static final int DEFAULT_EXPIRE_DAYS = 30;

    public ForceChangePwdInterceptor(RedisUtil redisUtil, SysConfigService sysConfigService) {
        this.redisUtil = redisUtil;
        this.sysConfigService = sysConfigService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 未登录直接放行（公开接口由 SaInterceptor 处理）
        if (!StpUtil.isLogin()) {
            return true;
        }
        // 从 Sa-Session 读取密码修改时间（登录时由 AuthController 写入，key=SESSION_PWD_UPDATE_KEY）
        Object value = StpUtil.getSession().get(CommonConstant.SESSION_PWD_UPDATE_KEY);
        LocalDateTime passwordUpdateTime = value instanceof LocalDateTime ? (LocalDateTime) value : null;

        // 判定是否需要强制改密（合并两个需求，不新增字段）
        if (passwordUpdateTime == null) {
            throw new BusinessException(ErrorCode.NEED_CHANGE_PWD, "新用户需要修改密码！");
        } else {
            // 2. 读取过期天数（系统参数，默认 30；0=关闭定期过期）
            int expireDays = getExpireDays();
            // 3. 距上次修改已超过策略天数 → 强制改密
            boolean isNeedChangPwd = expireDays > 0 && passwordUpdateTime.plusDays(expireDays).isBefore(LocalDateTime.now());
            if (isNeedChangPwd) {
                throw new BusinessException(ErrorCode.NEED_CHANGE_PWD, "密码过期，请修改密码！");
            }
        }
        return true;
    }

    /**
     * 读取密码过期天数（系统参数 sys.password.expireDays，默认 30）
     */
    private int getExpireDays() {
        String value = getSysConfig(SysConfigKeyEnum.PASSWORD_EXPIRE_DAYS.getConfigKey());
        if (value == null) {
            return DEFAULT_EXPIRE_DAYS;
        }
        try {
            int days = Integer.parseInt(value.trim());
            return days < 0 ? DEFAULT_EXPIRE_DAYS : days;
        } catch (NumberFormatException e) {
            return DEFAULT_EXPIRE_DAYS;
        }
    }

    /**
     * 读取系统参数（cache-aside：命中读 Redis，未命中查库并回填 3 天）
     */
    private String getSysConfig(String configKey) {
        String cached = redisUtil.get(RedisConstant.SYS_CONFIG + configKey, String.class);
        if (cached != null) {
            return cached;
        }
        SysConfig config = sysConfigService.findByKey(configKey);
        if (config == null) {
            return null;
        }
        redisUtil.set(RedisConstant.SYS_CONFIG + configKey, config.getConfigValue(), 3, TimeUnit.DAYS);
        return config.getConfigValue();
    }
}
