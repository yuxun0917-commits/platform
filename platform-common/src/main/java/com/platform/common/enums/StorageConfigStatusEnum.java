package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 存储配置状态枚举
 *
 * <p>对应 sys_storage_config 表的 status 字段：1=启用 0=禁用。</p>
 *
 * @author platform
 */
public enum StorageConfigStatusEnum {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 启用 */
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    StorageConfigStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static StorageConfigStatusEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (StorageConfigStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(StorageConfigStatusEnum::getDesc)
                .orElse("");
    }

    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
