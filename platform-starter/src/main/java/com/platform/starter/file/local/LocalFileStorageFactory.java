package com.platform.starter.file.local;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 本地磁盘存储工厂
 *
 * @author platform
 */
@Component("localFileStorageFactory")
@RequiredArgsConstructor
public class LocalFileStorageFactory implements FileStorageFactory {

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.LOCAL;
    }

    @Override
    public FileStorage build(SysStorageConfig config) {
        return new LocalFileStorage(config);
    }
}
