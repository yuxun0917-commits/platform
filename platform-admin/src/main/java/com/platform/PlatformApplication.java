package com.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 平台启动类
 *
 * <p>位于根包 {@code com.platform}，默认组件扫描覆盖 common、framework、starter、compose、service、api 全部子包。
 * 启动入口位于 admin（Web）模块，依赖方向为 admin -> service -> compose -> framework/starter -> common，无循环依赖。</p>
 *
 * <p>starter 模块的自动配置类通过 {@code AutoConfiguration.imports} 自动注册，
 * 引入 project-starter 依赖即自动生效，无需额外配置。</p>
 *
 * @author platform
 */
@SpringBootApplication
@MapperScan("com.platform.service.mapper")
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
