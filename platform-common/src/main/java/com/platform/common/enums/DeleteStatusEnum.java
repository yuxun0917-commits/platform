package com.platform.common.enums;

import java.util.Objects;

/**
 * 删除状态枚举
 */
public enum DeleteStatusEnum {

    NORMAL(0, "未删除"),
    DELETED(1, "已删除");

    private final Integer code;
    private final String desc;

    DeleteStatusEnum(Integer code, String desc) {
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
    public static DeleteStatusEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (DeleteStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     * 用法：DeleteStatusEnum.NORMAL.fromStatus(template.getStatus())
     * @param code 状态码
     * @return true 表示匹配
     */
    public boolean fromStatus(Integer code) {
        return this.code.equals(code);
    }
}