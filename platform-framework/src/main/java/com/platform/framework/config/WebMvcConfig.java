package com.platform.framework.config;

import com.platform.framework.handler.MyHandlerMethodArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类
 *
 * <p>全局跨域已交由 {@code CorsConfig}（过滤器级，最高优先级）处理，
 * 此处仅保留参数解析器，避免与 CorsFilter 重复下发 CORS 头。</p>
 *
 * @author platform
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MyHandlerMethodArgumentResolver());
    }
}
