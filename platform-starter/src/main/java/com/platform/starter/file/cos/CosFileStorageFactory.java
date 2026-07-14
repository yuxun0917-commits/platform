package com.platform.starter.file.cos;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageFactory;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS 存储工厂
 *
 * @author platform
 */
@Component("cosFileStorageFactory")
public class CosFileStorageFactory implements FileStorageFactory {

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.COS;
    }

    @Override
    public FileStorage build(SysStorageConfig config) {
        return new CosFileStorage(config);
    }
}
