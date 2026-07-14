package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 菜单状态枚举
 *
 * <p>对应 menu 表的 status 字段：1=正常 0=禁用</p>
 *
 * @author platform
 */
public enum MenuStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;

    MenuStatusEnum(Integer code, String desc) {
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
     * @param code 状态码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static MenuStatusEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (MenuStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 状态码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(MenuStatusEnum::getDesc)
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
