package com.platform.starter.threadpool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * 线程池配置属性类
 *
 * <p>对应 application.yml 中 {@code platform.thread-pool} 前缀的配置项。</p>
 *
 * @author platform
 */
@Data
@ConfigurationProperties(prefix = "platform.thread-pool")
public class ThreadPoolProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否启用线程池自动配置（默认开启） */
    private boolean enabled = true;

    /** 核心线程数（默认 5） */
    private int corePoolSize = 5;

    /** 最大线程数（默认 20） */
    private int maxPoolSize = 20;

    /** 队列容量（默认 200） */
    private int queueCapacity = 200;

    /** 线程空闲存活时间（秒，默认 60） */
    private int keepAliveSeconds = 60;

    /** 线程名前缀（默认 platform-async-） */
    private String threadNamePrefix = "platform-async-";
}
