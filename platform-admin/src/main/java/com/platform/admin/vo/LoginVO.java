package com.platform.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录入参 VO
 *
 * <p>Controller 接收前端登录参数使用，使用 jakarta.validation 注解进行参数校验。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "登录参数")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码唯一 key */
    @Schema(description = "验证码唯一 key", example = "a1b2c3d4e5f6789012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码 key 不能为空")
    private String captchaKey;

    /** 用户输入的验证码 */
    @Schema(description = "验证码", example = "TUDS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
