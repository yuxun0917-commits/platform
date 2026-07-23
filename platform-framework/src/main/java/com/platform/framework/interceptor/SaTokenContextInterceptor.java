package com.platform.framework.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.platform.common.constant.CommonConstant;
import com.platform.common.context.UserContext;
import com.platform.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * <p>
 * 职责:在 sa-token 校验通过后,将用户/租户信息写入 ThreadLocal;请求结束时清理
 * <p>
 * 必须注册在 sa-token 拦截器 {@code SaInterceptor} <b>之后</b>,
 * 保证进入本拦截器时登录态已校验完成
 *
 * @author yuxun
 */
public class SaTokenContextInterceptor implements HandlerInterceptor {

    /**
     * 预处理:sa-token 已校验,这里读取会话信息并写入上下文
     * <p>
     * 若 sa-token 拦截器未对当前路径做登录校验,本方法也不会写入上下文
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            // 租户 id 在登录时写入 Session,此处取出;单租户场景可省略
            String username = StpUtil.getSession().getString(CommonConstant.SESSION_USERNAME_KEY);
            String nickname = StpUtil.getSession().getString(CommonConstant.SESSION_NICKNAME_KEY);
            UserContextHolder.set(new UserContext(null, userId, username, nickname));
        }
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal,防止线程池复用导致数据串读
     * <p>
     * 即使业务抛异常也会执行,确保上下文不残留
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}