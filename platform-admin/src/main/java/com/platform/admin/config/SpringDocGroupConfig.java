package com.platform.admin.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 分组配置
 *
 * <p>使用编程式 {@link GroupedOpenApi} 替代 YAML {@code group-configs}，
 * 并通过 {@code addOperationCustomizer} 显式注册自定义器，
 * 确保 {@link OperationCustomizer} 一定会被 springdoc 调用。</p>
 *
 * <p>注意：使用此配置后，application.yml 中的 {@code springdoc.group-configs} 应移除，
 * 避免产生同名分组冲突。</p>
 *
 * @author platform
 */
@Configuration
public class SpringDocGroupConfig {

    /**
     * 默认分组：扫描 Controller 包下所有接口
     *
     * <p>通过 {@code addOperationCustomizer} 将全局 {@link OperationCustomizer} 显式绑定到本分组，
     * 解决 springdoc 2.3.0 在 YAML group-configs 模式下可能不调用全局 customizer 的问题。</p>
     *
     * @param platformOperationCustomizer 平台 OperationCustomizer（由 project-framework 自动注册）
     * @return GroupedOpenApi 默认分组
     */
    @Bean
    public GroupedOpenApi adminGroup(OperationCustomizer platformOperationCustomizer) {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/**")
                .packagesToScan("com.platform.admin.controller")
                .addOperationCustomizer(platformOperationCustomizer)
                .build();
    }
}
