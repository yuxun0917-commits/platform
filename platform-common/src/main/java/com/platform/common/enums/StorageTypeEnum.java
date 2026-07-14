package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 存储类型枚举
 *
 * <p>对应 sys_storage_config 表的 storage_type 字段：1=本地存储 2=阿里云OSS 3=腾讯云COS 4=MinIO。</p>
 * <p>beanName 绑定 Spring 容器中对应的 {@code FileStorageFactory} 实现，由 FileStorageManager 路由使用。</p>
 *
 * @author platform
 */
public enum StorageTypeEnum {

    /** 本地磁盘 */
    LOCAL(1, "本地存储", "localFileStorageFactory"),
    /** 阿里云 OSS */
    OSS(2, "阿里云OSS", "ossFileStorageFactory"),
    /** 腾讯云 COS */
    COS(3, "腾讯云COS", "cosFileStorageFactory"),
    /** MinIO（S3 兼容） */
    MINIO(4, "MinIO", "minioFileStorageFactory");

    private final Integer code;
    private final String desc;
    private final String beanName;

    StorageTypeEnum(Integer code, String desc, String beanName) {
        this.code = code;
        this.desc = desc;
        this.beanName = beanName;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getBeanName() {
        return beanName;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 存储类型码值
     * @return 枚举实例，code 不合法时返回 null
     */
    public static StorageTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (StorageTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 存储类型码值
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(StorageTypeEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 存储类型码值
     * @return true 表示匹配
     */
    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
