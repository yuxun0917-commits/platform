package com.platform.admin.config.handler;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HttpUtil;
import com.platform.common.entity.admin.SysLoginLog;
import com.platform.common.entity.admin.SysUser;
import com.platform.common.enums.LoginLogStatusEnum;
import com.platform.common.enums.LoginTypeEnum;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysLoginLogService;
import com.platform.service.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sa-Token 全局监听器
 *
 * <p>统一处理 Sa-Token 的各类事件回调：</p>
 * <ul>
 *   <li>登录成功：异步更新 user 表 login_ip/login_date + 异步写入 sys_login_log（登录日志）</li>
 *   <li>登出：异步写入 sys_login_log（登出日志）</li>
 *   <li>踢人下线：异步写入 sys_login_log（踢下线日志）</li>
 * </ul>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MySaTokenListener implements SaTokenListener {

    private final AsyncManager asyncManager;
    private final SysUserService sysUserService;
    private final SysLoginLogService sysLoginLogService;

    /**
     * 登录成功回调
     *
     * <p>异步执行两件事：</p>
     * <ol>
     *   <li>更新 user 表的 login_ip 和 login_date</li>
     *   <li>写入 sys_login_log 登录日志（loginType=1, status=1）</li>
     * </ol>
     *
     * @param loginType     账号类型
     * @param loginId       账号ID
     * @param tokenValue    令牌值
     * @param loginParameter 登录参数
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        Long userId = Long.parseLong(loginId.toString());
        HttpServletRequest request = getRequest();
        String loginIp = getClientIp(request);
        String browser = getBrowser(request);
        String os = getOs(request);

        // 1. 异步更新用户登录IP和时间
        asyncManager.execute(() -> {
            sysUserService.lambdaUpdate()
                    .set(SysUser::getLoginIp, loginIp)
                    .set(SysUser::getLoginDate, LocalDateTime.now())
                    .eq(SysUser::getId, userId)
                    .update();
        });
        // 2. 异步写入登录日志
        asyncManager.execute(() -> {
            SysUser sysUser = sysUserService.getById(userId);
            String username = Objects.nonNull(sysUser) ? sysUser.getUsername() : String.valueOf(userId);
            SysLoginLog loginLog = new SysLoginLog()
                    .setUserId(userId)
                    .setUsername(username)
                    .setLoginType(LoginTypeEnum.LOGIN.getCode())
                    .setLoginIp(loginIp)
                    .setLoginLocation(getLoginLocation(loginIp))
                    .setBrowser(browser)
                    .setOs(os)
                    .setStatus(LoginLogStatusEnum.SUCCESS.getCode())
                    .setLoginTime(LocalDateTime.now());
            sysLoginLogService.save(loginLog);
        });

        log.info("[Sa-Token] 用户登录成功，userId={}, ip={}", userId, loginIp);
    }

    /**
     * 登出回调
     *
     * <p>异步写入 sys_login_log 登出日志（loginType=2）。</p>
     *
     * @param loginType  账号类型
     * @param loginId    账号ID
     * @param tokenValue 令牌值
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        saveLoginLog(loginId, LoginTypeEnum.LOGOUT);
        log.info("[Sa-Token] 用户登出，userId={}", loginId);
    }

    /**
     * 踢人下线回调
     *
     * <p>异步写入 sys_login_log 踢下线日志（loginType=3）。</p>
     *
     * @param loginType  账号类型
     * @param loginId    账号ID
     * @param tokenValue 令牌值
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        saveLoginLog(loginId, LoginTypeEnum.KICKOUT);
        log.info("[Sa-Token] 用户被踢下线，userId={}", loginId);
    }

    /**
     * 异步保存登录日志（登出/踢下线复用）
     *
     * @param loginId      用户ID
     * @param loginTypeEnum 登录类型枚举
     */
    private void saveLoginLog(Object loginId, LoginTypeEnum loginTypeEnum) {
        Long userId = Long.parseLong(loginId.toString());
        HttpServletRequest request = getRequest();
        String loginIp = getClientIp(request);
        String browser = getBrowser(request);
        String os = getOs(request);

        asyncManager.execute(() -> {
            SysUser sysUser = sysUserService.getById(userId);
            String username = Objects.nonNull(sysUser) ? sysUser.getUsername() : String.valueOf(userId);
            SysLoginLog loginLog = new SysLoginLog()
                    .setUserId(userId)
                    .setUsername(username)
                    .setLoginType(loginTypeEnum.getCode())
                    .setLoginIp(loginIp)
                    .setLoginLocation(getLoginLocation(loginIp))
                    .setBrowser(browser)
                    .setOs(os)
                    .setStatus(LoginLogStatusEnum.SUCCESS.getCode())
                    .setLoginTime(LocalDateTime.now());
            sysLoginLogService.save(loginLog);
        });
    }

    // ============================ 以下为空实现 ============================

    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
    }

    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
    }

    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
    }

    @Override
    public void doOpenSafe(String loginType, String safeValue, String service, long safeTime) {
    }

    @Override
    public void doCloseSafe(String loginType, String safeValue, String service) {
    }

    @Override
    public void doCreateSession(String id) {
    }

    @Override
    public void doLogoutSession(String id) {
    }

    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
    }

    // ============================ 请求工具 ============================

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.isNull(attributes) ? null : attributes.getRequest();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return "unknown";
        }
        return JakartaServletUtil.getClientIP(request);
    }

    /**
     * 从 User-Agent 解析浏览器类型
     */
    private String getBrowser(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return "";
        }
        String ua = request.getHeader("User-Agent");
        if (Objects.isNull(ua)) {
            return "";
        }
        if (ua.contains("Edg")) {
            return "Edge";
        } else if (ua.contains("Chrome")) {
            return "Chrome";
        } else if (ua.contains("Firefox")) {
            return "Firefox";
        } else if (ua.contains("Safari")) {
            return "Safari";
        } else if (ua.contains("MSIE") || ua.contains("Trident")) {
            return "IE";
        }
        return "Unknown";
    }

    /**
     * 从 User-Agent 解析操作系统
     */
    private String getOs(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return "";
        }
        String ua = request.getHeader("User-Agent");
        if (Objects.isNull(ua)) {
            return "";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        } else if (ua.contains("Mac")) {
            return "macOS";
        } else if (ua.contains("Linux")) {
            return "Linux";
        } else if (ua.contains("Android")) {
            return "Android";
        } else if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        return "Unknown";
    }

    /**
     * 根据 IP 解析登录地点（归属地）
     *
     * <p>内网/本地回环 IP 返回"内网IP"；外网 IP 调用太平洋电脑城在线接口解析归属地；
     * 解析失败返回"未知"。在线接口受网络影响，若后续需更高稳定性可替换为离线 ip2region 库。</p>
     *
     * @param ip 登录IP
     * @return 登录地点
     */
    private String getLoginLocation(String ip) {
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return "内网IP";
        }
        try {
            if (Ipv4Util.isInnerIP(ip)) {
                return "内网IP";
            }
            String url = "https://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;
            String resp = HttpUtil.get(url, 3000);
            String addr = ReUtil.getGroup1("\"addr\":\"(.*?)\"", resp);
            return StrUtil.isBlank(addr) ? "未知" : addr;
        } catch (Exception e) {
            log.warn("[Sa-Token] IP归属地解析失败 ip={}", ip);
            return "未知";
        }
    }
}
