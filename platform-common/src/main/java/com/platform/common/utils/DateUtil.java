package com.platform.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Objects;

/**
 * 日期时间工具类
 *
 * @author platform
 */
public final class DateUtil {

    private DateUtil() {
    }

    /** 默认日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 默认日期时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        return Objects.isNull(date) ? null : DATE_FORMATTER.format(date);
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return Objects.isNull(dateTime) ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String text) {
        return LocalDate.parse(text, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String text) {
        return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
    }

    /**
     * 获取当前日期时间字符串
     */
    public static String now() {
        return formatDateTime(LocalDateTime.now());
    }
}
