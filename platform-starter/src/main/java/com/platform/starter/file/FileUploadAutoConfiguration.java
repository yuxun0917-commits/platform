package com.platform.starter.file;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传自动配置类
 *
 * <p>基于 Spring Boot 自动装配机制，当 {@code file.upload.enabled=true}（默认开启）时自动生效。
 * 负责：</p>
 * <ul>
 *   <li>绑定 {@link FileUploadProperties} 配置属性（大小限制、允许类型、本地路径、URL前缀）；</li>
 *   <li>注册本地静态资源映射，使本地磁盘存储的文件可通过 URL 直接访问。</li>
 * </ul>
 *
 * <p>多存储后端（本地/OSS/COS/MinIO）的路由由 {@code FileStorageManager} 与各
 * {@code FileStorageFactory} 共同完成，均通过组件扫描自动注册。</p>
 *
 * @author platform
 */
@AutoConfiguration
@EnableConfigurationProperties(FileUploadProperties.class)
@ConditionalOnProperty(prefix = "file.upload", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadAutoConfiguration implements WebMvcConfigurer {

    private final FileUploadProperties properties;

    public FileUploadAutoConfiguration(FileUploadProperties properties) {
        this.properties = properties;
    }

    /**
     * 注册静态资源映射，将本地上传目录映射到 URL 前缀
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPrefix = properties.getUrlPrefix();
        // 统一为 /** 结尾
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix + "/";
        }
        String location = "file:" + properties.getPath() + "/";
        registry.addResourceHandler(urlPrefix + "**").addResourceLocations(location);
    }
}
