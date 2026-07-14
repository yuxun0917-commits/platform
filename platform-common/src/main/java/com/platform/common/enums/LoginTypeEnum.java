package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 登录类型枚举
 *
 * <p>对应 sys_login_log 表的 login_type 字段：1=登录 2=登出 3=踢下线</p>
 *
 * @author platform
 */
public enum LoginTypeEnum {

    LOGIN(1, "登录"),
    LOGOUT(2, "登出"),
    KICKOUT(3, "踢下线");

    private final Integer code;
    private final String desc;

    LoginTypeEnum(Integer code, String desc) {
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
     * @param code 登录类型编码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static LoginTypeEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (LoginTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 登录类型编码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(LoginTypeEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 登录类型编码
     * @return true 表示匹配
     */
    public boolean fromCode(Integer code) {
        return this.code.equals(code);
    }
}
