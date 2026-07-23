package com.platform.common.enums;

/**
 * 是否枚举（是/否）
 * <p>
 * 适用于所有需要 "是/否"、"启用/禁用"、"开启/关闭" 二选一的场景
 * 数据库建议使用 tinyint(1) 类型，存储值 1 或 0
 */
public enum YesOrNoEnum {

    /**
     * 是 / 启用 / 开启
     */
    YES(1, "是"),

    /**
     * 否 / 停用 / 关闭
     */
    NO(0, "否");

    /**
     * 代码值（与数据库 tinyint 严格对应）
     */
    private final Integer code;

    /**
     * 展示名称（给前端或日志看）
     */
    private final String name;

    YesOrNoEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据 code（Integer）获取对应的枚举
     *
     * @param code 数据库值（1 或 0）
     * @return 对应枚举，若 code 非法则返回 null（调用方自己决定默认值）
     */
    public static YesOrNoEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (YesOrNoEnum item : YesOrNoEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 转换为 Java 布尔值
     * <p>
     * YES(1) -> true, NO(0) -> false
     */
    public boolean toBoolean() {
        return this == YES;
    }

    /**
     * 从 Java 布尔值转换为枚举
     * <p>
     * true -> YES(1), false -> NO(0)
     */
    public static YesOrNoEnum fromBoolean(boolean flag) {
        return flag ? YES : NO;
    }

    /**
     * 安全获取 code，防止空指针
     */
    public Integer getCodeSafe() {
        return this.code;
    }

    /**
     * 安全获取 name，防止空指针
     */
    public String getNameSafe() {
        return this.name;
    }
}