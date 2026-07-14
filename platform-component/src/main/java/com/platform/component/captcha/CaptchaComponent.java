package com.platform.component.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.common.constant.RedisConstant;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.starter.redis.RedisUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 验证码组件
 *
 * <p>基于 Hutool 图形验证码生成，并将验证码文本缓存至 Redis，用于登录等场景的安全校验。</p>
 *
 * @author platform
 */
@Component
public class CaptchaComponent {

    /**
     * 验证码默认宽度
     */
    private static final int DEFAULT_WIDTH = 140;

    /**
     * 验证码默认高度
     */
    private static final int DEFAULT_HEIGHT = 40;

    /**
     * 验证码默认字符长度
     */
    private static final int DEFAULT_CODE_COUNT = 4;

    /**
     * 验证码 Redis 缓存过期时间（秒）
     */
    @Value("${platform.captcha.ttl:120}")
    private long captchaTtlSeconds;

    private final RedisUtil redisUtil;

    public CaptchaComponent(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 生成图形验证码
     *
     * @return 验证码 key、Base64 图片数据
     */
    public CaptchaResult generate() {
        try {
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_CODE_COUNT, 20);
            String captchaCode = lineCaptcha.getCode();
            String captchaKey = IdUtil.simpleUUID();

            // 缓存验证码文本
            redisUtil.set(RedisConstant.CAPTCHA + captchaKey, captchaCode, Math.max(captchaTtlSeconds, 1), TimeUnit.SECONDS);

            // 输出为 Base64 PNG 图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            lineCaptcha.write(outputStream);
            String base64Image = Base64.encode(outputStream.toByteArray());

            CaptchaResult result = new CaptchaResult();
            result.setCaptchaKey(captchaKey);
            result.setBase64Image("data:image/png;base64," + base64Image);
            return result;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CAPTCHA_GENERATE_FAIL);
        }
    }

    /**
     * 校验验证码
     *
     * <p>校验成功后立即删除缓存，防止重复利用。</p>
     *
     * @param captchaKey  验证码唯一 key
     * @param captchaCode 用户输入的验证码
     */
    public void verify(String captchaKey, String captchaCode) {
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
            throw new BusinessException(ErrorCode.CAPTCHA_REQUIRED);
        }

        String cacheKey = RedisConstant.CAPTCHA + captchaKey;
        Object cacheValue = redisUtil.get(cacheKey);
        if (Objects.isNull(cacheValue)) {
            throw new BusinessException(ErrorCode.CAPTCHA_EXPIRED);
        }
        // 校验成功后删除缓存
        redisUtil.delete(cacheKey);

        if (!captchaCode.equalsIgnoreCase(cacheValue.toString())) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
    }

    /**
     * 验证码生成结果
     */
    @Data
    public static class CaptchaResult {

        /** 验证码唯一 key */
        private String captchaKey;

        /** Base64 编码的图形验证码图片（含 data URI 前缀） */
        private String base64Image;
    }
}
