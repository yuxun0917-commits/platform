package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 定时任务执行日志状态枚举
 *
 * <p>对应 sys_job_log 表的 status 字段：1=执行成功 0=执行失败。</p>
 *
 * @author platform
 */
public enum JobLogStatusEnum {

    FAIL(0, "失败"),
    SUCCESS(1, "成功");

    private final Integer code;
    private final String desc;

    JobLogStatusEnum(Integer code, String desc) {
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
    public static JobLogStatusEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (JobLogStatusEnum value : values()) {
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
                .map(JobLogStatusEnum::getDesc)
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
