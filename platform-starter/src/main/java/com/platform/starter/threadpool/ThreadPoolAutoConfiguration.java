package com.platform.starter.threadpool;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池自动配置类
 *
 * <p>当 {@code platform.thread-pool.enabled=true}（默认开启）时自动注入异步线程池，
 * 业务层通过 {@code @Async("platformAsyncExecutor")} 即可使用。</p>
 *
 * @author platform
 */
@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties(ThreadPoolProperties.class)
@ConditionalOnProperty(prefix = "platform.thread-pool", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThreadPoolAutoConfiguration {

    /**
     * 平台异步线程池
     *
     * @param properties 线程池配置
     * @return 线程池执行器
     */
    @Bean("platformAsyncExecutor")
    @ConditionalOnMissingBean(name = "platformAsyncExecutor")
    public Executor platformAsyncExecutor(ThreadPoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
