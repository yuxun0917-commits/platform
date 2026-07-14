package com.platform.common.utils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 正则校验工具类
 *
 * @author platform
 */
public final class RegexUtil {

    private RegexUtil() {
    }

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** 手机号正则（中国大陆） */
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    /** 用户名正则（字母/数字/下划线，4-20位） */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{4,20}$");

    /** 密码正则（字母+数字，6-20位） */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$");

    public static boolean isEmail(String email) {
        return Objects.nonNull(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isMobile(String mobile) {
        return Objects.nonNull(mobile) && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isUsername(String username) {
        return Objects.nonNull(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isPassword(String password) {
        return Objects.nonNull(password) && PASSWORD_PATTERN.matcher(password).matches();
    }
}
