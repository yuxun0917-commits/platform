package com.platform.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.platform.framework.interceptor.SaTokenContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证配置类
 *
 * <p>配置全局登录拦截器，排除 Swagger、登录等公开接口。</p>
 *
 * @author platform
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册 sa-token 拦截器（登录校验 + 注解鉴权）
        //    注解 @SaCheckPermission / @SaCheckRole 由 SaInterceptor 自动处理，无需手动调用。
        registry.addInterceptor(new SaInterceptor(handler -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Swagger 相关
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        // 登录/验证码/公钥等免登录接口
                        "/auth/**",
                        "/captcha/get",
                        // 附件公开预览直链（静态资源映射，免鉴权）
                        "/file/**",
                        // 静态资源
                        "/static/**",
                        "/webjars/**"
                );
        // 2. 再注册上下文拦截器(此时登录态已校验完成,可安全读会话)
        registry.addInterceptor(new SaTokenContextInterceptor())
                .addPathPatterns("/**");
    }
}
