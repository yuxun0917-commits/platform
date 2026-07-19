package com.platform.admin.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改密码入参 VO
 *
 * <p>用户自行修改密码场景：需验证旧密码，并确认新密码。
 * 用户ID从登录态获取，无需前端传入。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "修改密码参数")
public class UserPasswordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 旧密码 */
    @Schema(description = "旧密码", example = "old123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码（前端须用 RSA 公钥加密后传入，明文长度约束在后端解密后校验） */
    @Schema(description = "新密码（RSA加密密文）", example = "new123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /** 确认新密码 */
    @Schema(description = "确认新密码", example = "new123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
