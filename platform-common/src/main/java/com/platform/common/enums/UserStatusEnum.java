package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 用户状态枚举
 *
 * <p>对应 user 表的 status 字段：1=正常 0=禁用</p>
 *
 * @author platform
 */
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
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
     */
    public static UserStatusEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (UserStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取枚举
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(UserStatusEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 状态码
     * @return true 表示匹配
     */
    public boolean fromStatus(Integer code) {
        return this.code.equals(code);
    }
}
