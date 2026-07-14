package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 通知展示位置枚举
 *
 * <p>对应 sys_notice 表的 position 字段：1=后台 2=前台</p>
 *
 * @author platform
 */
public enum SysNoticePositionEnum {

    /** 后台 */
    ADMIN(1, "后台"),
    /** 前台 */
    SERVER(2, "前台");

    private final Integer code;
    private final String desc;

    SysNoticePositionEnum(Integer code, String desc) {
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
     * @param code 位置码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static SysNoticePositionEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (SysNoticePositionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 位置码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(SysNoticePositionEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 位置码
     * @return true 表示匹配
     */
    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
