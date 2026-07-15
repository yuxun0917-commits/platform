package com.platform.framework.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 全局跨域配置（过滤器级）
 *
 * <p>使用 {@link CorsFilter} 而非 {@code WebMvcConfigurer.addCorsMappings}，
 * 保证预检 OPTIONS 请求在 Sa-Token 等拦截器之前就拿到 CORS 头，
 * 否则 OPTIONS 预检被拦截器拦截（无 token → 401）会导致浏览器 CORS 报错。</p>
 *
 * @author platform
 */
@Configuration
public class CorsConfig {

    private static final List<String> EXPOSED_HEADERS = List.of(
            "Content-Type", "Authorization", "X-Requested-With", "X-Token", "Token"
    );

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（带凭证时反射为请求源，而非字面 *，否则浏览器拒绝）
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setExposedHeaders(EXPOSED_HEADERS);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // 最高优先级，确保在 Sa-Token 拦截器、DispatcherServlet 之前执行
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
