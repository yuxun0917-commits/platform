package com.platform.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图形验证码返回 VO
 *
 * @author platform
 */
@Data
@Schema(description = "图形验证码信息")
public class CaptchaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 验证码唯一 key（提交登录时需回传） */
    @Schema(description = "验证码唯一 key", example = "a1b2c3d4e5f6789012345678")
    private String captchaKey;

    /** Base64 编码的验证码图片（可直接赋值给 img 标签 src） */
    @Schema(description = "Base64 验证码图片", example = "data:image/png;base64,iVBORw0KGgo...")
    private String base64Image;
}
