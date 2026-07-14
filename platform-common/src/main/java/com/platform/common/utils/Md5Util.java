package com.platform.common.utils;

import cn.hutool.crypto.digest.DigestUtil;

import java.nio.charset.StandardCharsets;

/**
 * MD5 加密工具类
 *
 * <p>基于 Hutool 的 DigestUtil 实现，提供加盐加密能力。</p>
 *
 * @author platform
 */
public final class Md5Util {

    private Md5Util() {
    }

    /** 默认盐值（生产环境应从配置读取） */
    private static final String DEFAULT_SALT = "platform-salt";

    /**
     * MD5 加密（不带盐）
     */
    public static String md5(String text) {
        return DigestUtil.md5Hex(text);
    }

    /**
     * MD5 加密（带默认盐）
     */
    public static String md5WithSalt(String text) {
        return md5WithSalt(text, DEFAULT_SALT);
    }

    /**
     * MD5 加密（带自定义盐）
     */
    public static String md5WithSalt(String text, String salt) {
        String content = text + salt;
        return DigestUtil.md5Hex(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验原文与密文是否匹配（带默认盐）
     */
    public static boolean verify(String text, String ciphertext) {
        return md5WithSalt(text).equals(ciphertext);
    }
}
