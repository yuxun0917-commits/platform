package com.platform.admin.vo.monitor;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 服务监控聚合对象
 *
 * <p>采集并聚合服务器运行指标，供系统监控接口（服务监控）直接返回。
 * 包含六大块：CPU、系统内存、JVM、服务器信息、磁盘列表、Redis 缓存。
 * 仅承载数据，不依赖任何展示层 VO，可跨模块（component / admin）复用。</p>
 *
 * @author platform
 */
@Data
public class ServerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** CPU 信息 */
    private ServerVO.Cpu cpu;

    /** 系统内存信息 */
    private ServerVO.Mem mem;

    /** JVM 信息 */
    private ServerVO.Jvm jvm;

    /** 服务器信息 */
    private ServerVO.Sys sys;

    /** 磁盘信息列表 */
    private List<ServerVO.SysFile> sysFiles;

    /** Redis 缓存信息 */
    private ServerVO.RedisCache redis;

    /** 消息队列（RabbitMQ）信息 */
    private ServerVO.MqCache mq;

    /**
     * CPU 信息
     */
    @Data
    public static class Cpu implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** CPU 核心数（逻辑处理器数） */
        private int cpuNum;

        /** 系统 CPU 使用率（%） */
        private double sys;

        /** 当前进程（JVM）CPU 使用率（%） */
        private double used;

        /** 空闲率（%） */
        private double free;
    }

    /**
     * 系统内存信息（物理内存）
     */
    @Data
    public static class Mem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 总内存（字节） */
        private long total;

        /** 已用内存（字节） */
        private long used;

        /** 剩余内存（字节） */
        private long free;

        /** 使用率（%） */
        private double usage;
    }

    /**
     * JVM 信息
     */
    @Data
    public static class Jvm implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 总内存（字节） */
        private long total;

        /** 已用内存（字节） */
        private long used;

        /** 最大可用内存（字节） */
        private long max;

        /** 剩余内存（字节） */
        private long free;

        /** 使用率（%） */
        private double usage;

        /** JDK 版本 */
        private String version;

        /** JDK 路径 */
        private String home;

        /** 启动时间（毫秒时间戳） */
        private long startTime;

        /** 运行时长（格式化字符串，如 1天2小时3分钟） */
        private String runTime;
    }

    /**
     * 磁盘分区信息
     */
    @Data
    public static class SysFile implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 挂载路径（盘符） */
        private String dirName;

        /** 文件系统名称 */
        private String name;

        /** 文件系统类型 */
        private String type;

        /** 总大小（字节） */
        private long total;

        /** 已用（字节） */
        private long used;

        /** 剩余（字节） */
        private long free;

        /** 总大小（GB） */
        private double totalGb;

        /** 已用（GB） */
        private double usedGb;

        /** 剩余（GB） */
        private double freeGb;

        /** 使用率（%） */
        private double usage;
    }

    /**
     * 服务器（操作系统）信息
     */
    @Data
    public static class Sys implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 服务器名称 */
        private String computerName;

        /** 服务器 IP */
        private String computerIp;

        /** 项目路径 */
        private String userDir;

        /** 操作系统名称 */
        private String osName;

        /** 操作系统架构 */
        private String osArch;

        /** 操作系统版本 */
        private String osVersion;
    }

    /**
     * Redis 缓存信息（来自 INFO 命令）
     */
    @Data
    public static class RedisCache implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** Redis 版本 */
        private String version;

        /** 运行模式（standalone / sentinel / cluster） */
        private String mode;

        /** 监听端口 */
        private String port;

        /** 运行时长（天） */
        private String uptime;

        /** 当前客户端连接数 */
        private String connectedClients;

        /** 已用内存（人类可读，如 2.10M） */
        private String usedMemory;

        /** 最大内存（人类可读） */
        private String maxMemory;

        /** 命令处理总数 */
        private String commandsProcessed;

        /** 缓存命中率（%） */
        private String hitRate;
    }

    /**
     * 消息队列（RabbitMQ）信息
     */
    @Data
    public static class MqCache implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** MQ 名称（如 RabbitMQ） */
        private String name;

        /** 服务端版本 */
        private String version;

        /** 连接地址（host） */
        private String host;

        /** 连接端口 */
        private String port;

        /** 集群名称 */
        private String clusterName;

        /** 连接状态（在线 / 离线） */
        private String status;

        /** 队列总数 */
        private int queueCount;

        /** 消息总数（所有队列待消费消息之和） */
        private long messageCount;

        /** 消费者总数 */
        private long consumerCount;
    }
}
