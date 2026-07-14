package com.platform.admin.config;

import com.platform.component.job.ScheduleComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 定时任务启动恢复 Runner
 *
 * <p>基于 {@link ApplicationRunner}，在 Spring Boot 完全启动后将数据库中
 * 状态正常（status=1）且未删除的任务重新注册到 Quartz 调度器。</p>
 *
 * <p>因使用内存 RAMJobStore，进程重启后调度器为空，必须由此处恢复。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(20)
public class JobInitRunner implements ApplicationRunner {

    private final ScheduleComponent scheduleComponent;

    @Override
    public void run(ApplicationArguments args) {
        scheduleComponent.initAllRunningJobs();
    }
}
