package com.platform.admin.vo.storage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 存储配置编辑入参 VO
 *
 * @author platform
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "存储配置编辑参数")
public class StorageConfigEditVO extends StorageConfigSaveVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "配置ID不能为空")
    private Long id;
}
