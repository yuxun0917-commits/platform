package com.platform.starter.file;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.common.utils.Assert;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件存储管理器（路由中心）
 *
 * <p>收集所有 {@link FileStorageFactory}，按 {@link StorageTypeEnum} 建立路由表。
 * 调用方传入一条 {@link SysStorageConfig}，由管理器找到对应工厂并构建具体的
 * {@link FileStorage} 实现，从而实现“配置驱动、可人为切换”的多存储后端能力。</p>
 *
 * @author platform
 */
@Slf4j
@Component
public class FileStorageManager {

    private final List<FileStorageFactory> factories;
    private final Map<StorageTypeEnum, FileStorageFactory> factoryMap = new HashMap<>();

    public FileStorageManager(List<FileStorageFactory> factories) {
        this.factories = factories;
    }

    /**
     * 初始化路由表
     */
    @PostConstruct
    public void init() {
        for (FileStorageFactory factory : factories) {
            factoryMap.put(factory.type(), factory);
            log.info("[存储管理] 注册存储工厂, type={}, bean={}", factory.type().getDesc(), factory.type().getBeanName());
        }
    }

    /**
     * 根据存储配置获取对应的存储实现
     *
     * @param config 存储配置（含 storageType、endpoint、bucket、ak/sk 等）
     * @return 文件存储实现
     */
    public FileStorage getStorage(SysStorageConfig config) {
        Assert.notNull(config, "存储配置不能为空");
        StorageTypeEnum type = StorageTypeEnum.getByCode(config.getStorageType());
        Assert.notNull(type, "不支持的存储类型:{}", config.getStorageType());
        FileStorageFactory factory = factoryMap.get(type);
        Assert.notNull(factory, "未找到存储类型[{}]的实现", type.getDesc());
        return factory.build(config);
    }
}
