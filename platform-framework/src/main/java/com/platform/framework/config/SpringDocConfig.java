package com.platform.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3 (Swagger3) 配置类
 *
 * @author platform
 */
@Configuration
public class SpringDocConfig{

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Platform 快速开发脚手架 API 文档")
                        .description("基于 Spring Boot 3.x + MyBatis-Plus + RabbitMQ + Sa-Token 的企业级后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("platform")
                                .email("platform@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
