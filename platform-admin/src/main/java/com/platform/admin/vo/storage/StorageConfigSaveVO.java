package com.platform.admin.vo.storage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存储配置添加入参 VO
 *
 * @author platform
 */
@Data
@Schema(description = "存储配置添加参数")
public class StorageConfigSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 配置名称 */
    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    /** 存储类型(1本地 2阿里云OSS 3腾讯COS 4MinIO) */
    @Schema(description = "存储类型(1本地 2阿里云OSS 3腾讯COS 4MinIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "存储类型不能为空")
    private Integer storageType;

    /** 访问域名/接入点 */
    @Schema(description = "访问域名")
    @Size(max = 255, message = "长度不能超过255个字符")
    private String endpoint;

    /** 地域 */
    @Schema(description = "地域")
    @Size(max = 100, message = "长度不能超过100个字符")
    private String region;

    /** 存储桶名 */
    @Schema(description = "存储桶名")
    @Size(max = 100, message = "长度不能超过100个字符")
    private String bucket;

    /** AccessKey / SecretId */
    @Schema(description = "AccessKey")
    @Size(max = 255, message = "长度不能超过255个字符")
    private String accessKey;

    /** SecretKey */
    @Schema(description = "SecretKey")
    @Size(max = 255, message = "长度不能超过255个字符")
    private String secretKey;

    /** 本地根路径 或 对象存储路径前缀 */
    @Schema(description = "根路径/前缀")
    @Size(max = 255, message = "长度不能超过255个字符")
    private String basePath;

    /** 自定义访问域名/CDN */
    @Schema(description = "自定义域名")
    @Size(max = 255, message = "长度不能超过255个字符")
    private String domain;

    /** 是否https（0否 1是） */
    @Schema(description = "是否https(0否 1是)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否https不能为空")
    private Integer isHttps;

    /** 状态（1启用 0停用） */
    @Schema(description = "状态(1启用 0停用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
