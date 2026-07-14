package com.platform.starter.file;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;

/**
 * 文件存储工厂
 *
 * <p>根据 {@link SysStorageConfig} 构建具体的 {@link FileStorage} 实现。
 * 每种存储类型对应一个工厂 Bean（beanName 与 {@link StorageTypeEnum#getBeanName()} 一致），
 * 由 {@code FileStorageManager} 收集并按类型路由。</p>
 *
 * @author platform
 */
public interface FileStorageFactory {

    /**
     * 该工厂支持的存储类型
     *
     * @return 存储类型枚举
     */
    StorageTypeEnum type();

    /**
     * 基于存储配置构建存储实现
     *
     * @param config 存储配置（含 endpoint/bucket/ak/sk 等）
     * @return 文件存储实现
     */
    FileStorage build(SysStorageConfig config);
}
