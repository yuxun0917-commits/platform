package com.platform.common.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * 定时任务错失触发（misfire）策略枚举
 *
 * <p>对应 sys_job 表的 misfire_policy 字段。当任务因服务重启、线程池占满、
 * 并发限制等原因错过原定触发点时，决定如何处理错过的那次触发。</p>
 *
 * <p>Quartz 原生仅两类策略，本枚举收敛为两值：</p>
 * <ul>
 *   <li>0 = 不补跑（底层 DO_NOTHING，错过的直接跳过）</li>
 *   <li>1 = 补跑（底层 FireAndProceed，错过的那次立即补执行一次）</li>
 * </ul>
 *
 * @author platform
 */
public enum MisfirePolicyEnum {

    DO_NOTHING(0, "不补跑"),
    FIRE_AND_PROCEED(1, "补跑");

    private final Integer code;
    private final String desc;

    MisfirePolicyEnum(Integer code, String desc) {
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
     * @param code 策略码
     * @return 枚举实例，code 不合法时返回 null
     */
    public static MisfirePolicyEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (MisfirePolicyEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取描述
     *
     * @param code 策略码
     * @return 描述文本，code 不合法时返回空字符串
     */
    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(getByCode(code))
                .map(MisfirePolicyEnum::getDesc)
                .orElse("");
    }

    /**
     * 判断传入的 code 是否与当前枚举实例的 code 一致
     *
     * @param code 策略码
     * @return true 表示匹配
     */
    public boolean fromStatus(Integer code) {
        return this.code.equals(code);
    }
}
