package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 性别枚举
 *
 * <p>对应 user 表的 gender 字段：0=未知 1=男 2=女</p>
 *
 * @author platform
 */
public enum GenderEnum {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final Integer code;
    private final String desc;

    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 性别码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static GenderEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (GenderEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 性别码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(GenderEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 性别码
     * @return true 表示匹配
     */
    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
