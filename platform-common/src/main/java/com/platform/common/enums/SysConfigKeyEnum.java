package com.platform.common.enums;

/**
 * 系统内置配置枚举
 *
 * <p>集中维护平台自带、不可删除的系统配置项（对应 sys_config 表的 config_type=1）。</p>
 *
 * <p>约束与收益：</p>
 * <ul>
 *   <li>代码需要引用系统配置键名时，应使用本枚举而非硬编码字符串（如
 *       {@code SysConfigKeyEnum.USER_INIT_PASSWORD.getConfigKey()}），避免拼写错误。</li>
 *   <li>新增系统内置配置项时，只需在此枚举追加一项即可，无需改动散落的字符串常量。</li>
 *   <li>所有枚举项的 {@link #getConfigType()} 恒为 {@link SysConfigTypeEnum#YES}（1=系统内置），
 *       与 sys_config 表的 config_type 字段保持一致。</li>
 * </ul>
 *
 * @author platform
 */
public enum SysConfigKeyEnum {

    /**
     * 系统首页标题
     */
    INDEX_TITLE("sys.index.title", "系统首页标题", "系统首页显示标题"),

    /**
     * 系统 Logo
     */
    INDEX_LOGO("sys.index.logo", "系统Logo", "系统首页 Logo 图片地址"),

    /**
     * 系统首页版权信息
     */
    INDEX_COPYRIGHT("sys.index.copyright", "版权信息", "系统首页底部版权信息"),

    /**
     * 主框架页默认皮肤样式名称
     */
    INDEX_SKIN_NAME("sys.index.skinName", "默认皮肤样式", "主框架页默认皮肤样式名称"),

    /**
     * 新增用户默认初始密码
     */
    USER_INIT_PASSWORD("sys.user.initPassword", "用户初始密码", "新增用户默认初始密码"),

    /**
     * 新增用户默认头像
     */
    USER_AVATAR("sys.user.avatar", "用户默认头像", "新增用户默认头像地址"),

    /**
     * 登录是否启用验证码
     */
    ACCOUNT_CAPTCHA_ENABLED("sys.account.captchaEnabled", "验证码开关", "登录是否启用验证码（true/false）"),

    /**
     * 登录是否启用验证码
     */
    BYPASS_CAPTCHA("sys.bypass.captcha", "万能验证码", "万能验证码"),

    /**
     * 是否允许自助注册账号
     */
    ACCOUNT_REGISTER_USER("sys.account.registerUser", "是否开放注册", "是否允许自助注册账号（true/false）"),

    /**
     * 密码过期时间窗口（天）
     */
    PASSWORD_EXPIRE_DAYS("sys.password.expireDays", "密码过期天数", "密码有效期，超过此天数需强制修改（单位：天）"),

    /**
     * 是否启用新用户强制修改密码
     */
    PASSWORD_FORCE_NEW_USER("sys.password.forceNewUser", "新用户强制改密", "新用户首次登录是否强制修改密码（true/false）");

    /**
     * 配置键名（如 sys.user.initPassword）
     */
    private final String configKey;

    /**
     * 配置名称（如：用户初始密码）
     */
    private final String configName;

    /**
     * 备注说明
     */
    private final String remark;

    SysConfigKeyEnum(String configKey, String configName, String remark) {
        this.configKey = configKey;
        this.configName = configName;
        this.remark = remark;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public String getRemark() {
        return remark;
    }

    /**
     * 配置类型：恒为系统内置（1）
     *
     * <p>与 sys_config 表的 config_type 字段对齐，便于初始化内置配置时直接取值。</p>
     *
     * @return 系统内置对应的状态码
     */
    public Integer getConfigType() {
        return SysConfigTypeEnum.YES.getCode();
    }

    /**
     * 根据配置键名获取枚举
     *
     * @param configKey 配置键名
     * @return 枚举实例；键名为空或不匹配时返回 null
     */
    public static SysConfigKeyEnum getByConfigKey(String configKey) {
        if (configKey == null || configKey.isEmpty()) {
            return null;
        }
        for (SysConfigKeyEnum value : values()) {
            if (value.configKey.equals(configKey)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断给定配置键名是否为系统内置配置
     *
     * @param configKey 配置键名
     * @return true 表示属于系统内置配置
     */
    public static boolean isBuiltIn(String configKey) {
        return getByConfigKey(configKey) != null;
    }
}
