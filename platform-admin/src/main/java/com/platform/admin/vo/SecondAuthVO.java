package com.platform.admin.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 二次认证信息
 *
 * @author platform
 */
@Data
@Schema(description = "二次认证信息")
public class SecondAuthVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 密码 */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;
}