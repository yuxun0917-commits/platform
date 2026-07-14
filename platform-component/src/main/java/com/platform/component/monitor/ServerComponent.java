package com.platform.component.monitor;

import com.platform.common.bo.ServerBO;
import com.platform.starter.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.Util;
import com.platform.common.constant.RabbitMqConstant;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 服务监控采集组件
 *
 * <p>实时采集服务器运行指标并聚合为 {@link ServerBO}，供监控接口直接返回。
 * 数据来源：</p>
 * <ul>
 *   <li>CPU / 系统内存 / 磁盘 / 系统负载：OSHI（{@link SystemInfo}）</li>
 *   <li>JVM 内存 / 启动时长：{@link Runtime} 与 {@link ManagementFactory}</li>
 *   <li>服务器信息：{@link InetAddress} 与系统属性</li>
 *   <li>Redis：{@link RedisUtil#getInfo()} 执行 INFO 命令</li>
 *   <li>RabbitMQ：{@link RabbitTemplate} 连接信息与 {@link RabbitAdmin} 队列 / 消息统计</li>
 * </ul>
 *
 * <p>本组件仅做数据采集与聚合，不感知展示层结构（{@link ServerBO} 为 entity 级载体），
 * 符合 component 模块不依赖 admin 展示层 VO 的边界约定。</p>
 *
 * @author platform
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ServerComponent {

    private final RedisUtil redisUtil;
    private final RabbitTemplate rabbitTemplate;

    /** CPU 负载采样等待时间（毫秒） */
    private static final long CPU_SAMPLE_INTERVAL = 1000L;

    /**
     * 采集服务器全部监控指标
     *
     * @return 聚合后的服务监控数据
     */
    public ServerBO getServerInfo() {
        ServerBO bo = new ServerBO();
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();

        bo.setCpu(setCpuInfo(si));
        bo.setMem(setMemInfo(memory));
        bo.setJvm(setJvmInfo());
        bo.setSys(setSysInfo());
        bo.setSysFiles(setSysFiles(fileSystem));
        bo.setRedis(setRedisInfo());
        bo.setMq(setMqInfo());
        return bo;
    }

    /**
     * 采集 CPU 信息
     *
     * <p>通过 OSHI 前后两次 tick 采样计算系统 CPU 负载（存在约 1 秒阻塞）。
     * 用户/系统/空闲三项之和为 100%。</p>
     */
    private ServerBO.Cpu setCpuInfo(SystemInfo si) {
        CentralProcessor processor = si.getHardware().getProcessor();
        ServerBO.Cpu cpu = new ServerBO.Cpu();
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        // 系统 CPU：两次 tick 采样计算
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(CPU_SAMPLE_INTERVAL);
        double systemCpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        cpu.setSys(round(systemCpuLoad * 100));
        cpu.setUsed(round(systemCpuLoad * 100));
        cpu.setFree(round((1.0 - systemCpuLoad) * 100));
        return cpu;
    }

    /**
     * 采集系统物理内存信息
     */
    private ServerBO.Mem setMemInfo(GlobalMemory memory) {
        ServerBO.Mem mem = new ServerBO.Mem();
        long total = memory.getTotal();
        long free = memory.getAvailable();
        long used = total - free;
        mem.setTotal(total);
        mem.setFree(free);
        mem.setUsed(used);
        mem.setUsage(round((double) used / total * 100));
        return mem;
    }

    /**
     * 采集 JVM 信息
     */
    private ServerBO.Jvm setJvmInfo() {
        ServerBO.Jvm jvm = new ServerBO.Jvm();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long used = totalMemory - freeMemory;
        jvm.setTotal(totalMemory);
        jvm.setFree(freeMemory);
        jvm.setMax(maxMemory);
        jvm.setUsed(used);
        jvm.setUsage(round((double) used / maxMemory * 100));
        jvm.setVersion(System.getProperty("java.version"));
        jvm.setHome(System.getProperty("java.home"));
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        jvm.setStartTime(startTime);
        jvm.setRunTime(getRunTime(startTime));
        return jvm;
    }

    /**
     * 采集服务器（操作系统）信息
     */
    private ServerBO.Sys setSysInfo() {
        ServerBO.Sys sys = new ServerBO.Sys();
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sys.setComputerName(addr.getHostName());
            sys.setComputerIp(addr.getHostAddress());
        } catch (UnknownHostException e) {
            log.warn("[ServerMonitor] 获取本机网络信息失败", e);
        }
        sys.setUserDir(System.getProperty("user.dir"));
        sys.setOsName(System.getProperty("os.name"));
        sys.setOsArch(System.getProperty("os.arch"));
        sys.setOsVersion(System.getProperty("os.version"));
        return sys;
    }

    /**
     * 采集磁盘信息列表
     */
    private List<ServerBO.SysFile> setSysFiles(FileSystem fileSystem) {
        List<ServerBO.SysFile> list = new ArrayList<>();
        for (OSFileStore fs : fileSystem.getFileStores()) {
            long total = fs.getTotalSpace();
            long free = fs.getFreeSpace();
            long used = total - free;
            // 跳过无容量的虚拟/映射盘
            if (total <= 0) {
                continue;
            }
            ServerBO.SysFile sf = new ServerBO.SysFile();
            sf.setDirName(fs.getMount());
            sf.setName(fs.getName());
            sf.setType(fs.getType());
            sf.setTotal(total);
            sf.setFree(free);
            sf.setUsed(used);
            sf.setTotalGb(round(total / 1024.0 / 1024.0 / 1024.0));
            sf.setUsedGb(round(used / 1024.0 / 1024.0 / 1024.0));
            sf.setFreeGb(round(free / 1024.0 / 1024.0 / 1024.0));
            sf.setUsage(round((double) used / total * 100));
            list.add(sf);
        }
        return list;
    }

    /**
     * 采集 Redis 信息（来自 INFO 命令）
     */
    private ServerBO.RedisCache setRedisInfo() {
        ServerBO.RedisCache redis = new ServerBO.RedisCache();
        try {
            Properties info = redisUtil.getInfo();
            if (Objects.isNull(info)) {
                return redis;
            }
            redis.setVersion(info.getProperty("redis_version", "-"));
            redis.setMode(info.getProperty("redis_mode", "-"));
            redis.setPort(info.getProperty("tcp_port", "-"));
            redis.setUptime(info.getProperty("uptime_in_days", "-"));
            redis.setConnectedClients(info.getProperty("connected_clients", "-"));
            redis.setUsedMemory(info.getProperty("used_memory_human", "-"));
            redis.setMaxMemory(info.getProperty("maxmemory_human", "-"));
            redis.setCommandsProcessed(info.getProperty("total_commands_processed", "-"));
            long hits = parseLong(info.getProperty("keyspace_hits"), 0L);
            long misses = parseLong(info.getProperty("keyspace_misses"), 0L);
            double rate = (hits + misses) == 0 ? 0 : (double) hits / (hits + misses) * 100;
            redis.setHitRate(String.format("%.2f", rate));
        } catch (Exception e) {
            log.warn("[ServerMonitor] 获取 Redis INFO 失败", e);
        }
        return redis;
    }

    /**
     * 采集 RabbitMQ 信息（连接信息 + 队列 / 消息统计）
     */
    private ServerBO.MqCache setMqInfo() {
        ServerBO.MqCache mq = new ServerBO.MqCache();
        mq.setName("RabbitMQ");
        try {
            // 连接层面信息（版本 / 地址 / 端口 / 集群名 / 状态）
            Connection connection = rabbitTemplate.getConnectionFactory().createConnection();
            com.rabbitmq.client.Connection delegate = connection.getDelegate();
            Map<String, Object> serverProps = delegate.getServerProperties();
            mq.setVersion(Objects.toString(serverProps.get("version"), "-"));
            mq.setHost(delegate.getAddress().getHostAddress());
            mq.setPort(String.valueOf(delegate.getPort()));
            mq.setClusterName(Objects.toString(serverProps.get("cluster_name"), "-"));
            mq.setStatus(delegate.isOpen() ? "在线" : "离线");
            connection.close();
            // 队列与消息统计（遍历项目声明的队列，累加消息数 / 消费者数）
            List<String> queueNames = List.of(RabbitMqConstant.USER_QUEUE, RabbitMqConstant.DEAD_LETTER_QUEUE);
            long messageCount = 0L;
            long consumerCount = 0L;
            for (String name : queueNames) {
                try {
                    com.rabbitmq.client.AMQP.Queue.DeclareOk ok =
                            rabbitTemplate.execute(channel -> channel.queueDeclarePassive(name));
                    if (Objects.nonNull(ok)) {
                        messageCount += ok.getMessageCount();
                        consumerCount += ok.getConsumerCount();
                    }
                } catch (Exception e) {
                    log.debug("[ServerMonitor] 获取队列 {} 信息失败", name);
                }
            }
            mq.setQueueCount(queueNames.size());
            mq.setMessageCount(messageCount);
            mq.setConsumerCount(consumerCount);
        } catch (Exception e) {
            mq.setStatus("离线");
            log.warn("[ServerMonitor] 获取 RabbitMQ 信息失败", e);
        }
        return mq;
    }

    /**
     * 计算运行时长（天/小时/分钟）
     */
    private String getRunTime(long startTime) {
        long runMs = System.currentTimeMillis() - startTime;
        long day = TimeUnit.MILLISECONDS.toDays(runMs);
        long hour = TimeUnit.MILLISECONDS.toHours(runMs) % 24;
        long minute = TimeUnit.MILLISECONDS.toMinutes(runMs) % 60;
        return day + "天" + hour + "小时" + minute + "分钟";
    }

    /**
     * 保留两位小数（四舍五入）
     */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 安全解析长整型
     */
    private long parseLong(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
