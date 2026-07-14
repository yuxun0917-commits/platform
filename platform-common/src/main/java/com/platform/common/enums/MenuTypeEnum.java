package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 菜单类型枚举
 *
 * <p>对应 menu 表的 menu_type 字段：1=目录 2=菜单 3=按钮</p>
 *
 * @author platform
 */
public enum MenuTypeEnum {

    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    private final Integer code;
    private final String desc;

    MenuTypeEnum(Integer code, String desc) {
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
     * @param code 类型码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static MenuTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (MenuTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 类型码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(MenuTypeEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 类型码
     * @return true 表示匹配
     */
    public boolean fromType(Integer code) {
        return this.code.equals(code);
    }
}
