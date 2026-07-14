package com.platform.starter.file.oss;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageFactory;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 存储工厂
 *
 * @author platform
 */
@Component("ossFileStorageFactory")
public class OssFileStorageFactory implements FileStorageFactory {

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.OSS;
    }

    @Override
    public FileStorage build(SysStorageConfig config) {
        return new OssFileStorage(config);
    }
}
