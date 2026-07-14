package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 定时任务并发执行策略枚举
 *
 * <p>对应 sys_job 表的 concurrent 字段：1=允许并发 0=禁止并发。
 * 禁止并发时，任务类会使用 {@code @DisallowConcurrentExecution} 注解，
 * 上一次未执行完则下一次触发点被跳过，避免任务叠加。</p>
 *
 * @author platform
 */
public enum ConcurrentEnum {

    FORBID(0, "禁止"),
    ALLOW(1, "允许");

    private final Integer code;
    private final String desc;

    ConcurrentEnum(Integer code, String desc) {
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
     * @param code 并发策略码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static ConcurrentEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (ConcurrentEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 并发策略码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(ConcurrentEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 并发策略码
     * @return true 表示匹配
     */
    public boolean fromStatus(Integer code) {
        return this.code.equals(code);
    }
}
