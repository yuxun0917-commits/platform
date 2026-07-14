package com.platform.starter.file.minio;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileStorageFactory;
import org.springframework.stereotype.Component;

/**
 * MinIO 存储工厂（S3 兼容）
 *
 * @author platform
 */
@Component("minioFileStorageFactory")
public class MinioFileStorageFactory implements FileStorageFactory {

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.MINIO;
    }

    @Override
    public FileStorage build(SysStorageConfig config) {
        return new MinioFileStorage(config);
    }
}
