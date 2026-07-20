package com.platform.admin.controller;

import com.platform.admin.vo.CaptchaVO;
import com.platform.common.result.Result;
import com.platform.component.captcha.CaptchaComponent;
import com.platform.starter.ratelimiter.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 *
 * <p>提供图形验证码生成接口，用于登录等场景的安全校验。</p>
 *
 * @author platform
 */
@Tag(name = "验证码")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private final CaptchaComponent captchaComponent;

    /**
     * 获取图形验证码
     *
     * @return 验证码 key 与 Base64 图片
     */
    @RateLimit(limit = 5)
    @Operation(summary = "获取图形验证码")
    @GetMapping("/get")
    public Result get() {
        CaptchaComponent.CaptchaResult result = captchaComponent.generate();
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaKey(result.getCaptchaKey());
        vo.setBase64Image(result.getBase64Image());
        return Result.success(vo);
    }
}
