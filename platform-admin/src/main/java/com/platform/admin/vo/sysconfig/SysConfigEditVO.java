package com.platform.admin.vo.sysconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置编辑入参 VO
 *
 * <p>在 SysConfigSaveVO 基础上增加 id 字段，其他字段校验与 SysConfigSaveVO 一致。
 * 字段长度依据 sys_config 表定义。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "系统配置编辑参数")
public class SysConfigEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 参数主键ID */
    @Schema(description = "参数主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "参数主键ID不能为空")
    private Long id;

    /** 参数名称 */
    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 64, message = "参数名称长度不能超过64个字符")
    private String configName;

    /** 参数键名 */
    @Schema(description = "参数键名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数键名不能为空")
    @Size(max = 128, message = "参数键名长度不能超过128个字符")
    private String configKey;

    /** 参数键值 */
    @Schema(description = "参数键值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数键值不能为空")
    @Size(max = 255, message = "参数键值长度不能超过255个字符")
    private String configValue;

    /** 是否系统内置（1是 0否） */
    @Schema(description = "是否系统内置（1是 0否）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否系统内置不能为空")
    private Integer configType;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
