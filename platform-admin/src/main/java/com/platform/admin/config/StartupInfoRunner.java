package com.platform.admin.config;

import cn.hutool.core.net.NetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 应用启动信息打印
 *
 * <p>基于 {@link ApplicationRunner} 实现，在 Spring Boot 完全启动后（所有 Bean 初始化、
 * Web 服务器就绪）自动输出启动摘要，包括：</p>
 * <ul>
 *   <li>应用名称、运行环境、启动耗时</li>
 *   <li>本地访问地址（IP + 端口 + 上下文路径）</li>
 *   <li>Swagger / OpenAPI 文档地址</li>
 *   <li>已注册的 Controller 接口数量</li>
 * </ul>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInfoRunner implements ApplicationRunner {

    private final Environment environment;

    /** 记录应用启动开始时间（类加载时即记录） */
    private static final Instant START_TIME = Instant.now();

    @Override
    public void run(ApplicationArguments args) {
        // 启动耗时
        long costSeconds = Duration.between(START_TIME, Instant.now()).getSeconds();

        // 应用信息
        String appName = environment.getProperty("spring.application.name", "unknown");
        String activeProfile = environment.getProperty("spring.profiles.active", "default");

        // 端口与上下文路径
        Integer port = environment.getProperty("server.port", Integer.class, 8080);
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        // 统一去掉末尾的 /，避免拼接子路径时出现 //
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        // 本机 IP
        String hostIp = getHostIp();

        // Swagger 路径
        String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String apiDocsPath = environment.getProperty("springdoc.api-docs.path", "/v3/api-docs");

        // 拼接基础地址（base 结尾无 /）
        String baseLocal = "http://localhost:" + port + contextPath;
        String baseIp = "http://" + hostIp + ":" + port + contextPath;

        // 输出启动信息
        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("  " + appName + " 启动成功 (" + activeProfile + ")  耗时 " + costSeconds + "s");
        System.out.println("------------------------------------------------------------");
        System.out.println("  Local     :  " + baseLocal);
        System.out.println("  Network   :  " + baseIp);
        System.out.println("  Swagger   :  " + baseLocal + swaggerPath);
        System.out.println("  OpenAPI   :  " + baseLocal + apiDocsPath);
        System.out.println("------------------------------------------------------------");
        System.out.println();
    }

    /**
     * 获取本机 IP
     */
    private String getHostIp() {
        try {
            return NetUtil.getLocalhostStr();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
