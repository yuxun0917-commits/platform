package com.platform.admin.vo.storage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储配置展示 VO
 *
 * <p>accessKey / secretKey 在返回前端时做脱敏处理，避免密钥泄露。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "存储配置信息")
public class StorageConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @Schema(description = "配置ID")
    private Long id;

    /** 配置名称 */
    @Schema(description = "配置名称")
    private String configName;

    /** 存储类型(1本地 2阿里云OSS 3腾讯COS 4MinIO) */
    @Schema(description = "存储类型")
    private Integer storageType;

    /** 存储类型描述 */
    @Schema(description = "存储类型描述")
    private String storageTypeText;

    /** 访问域名/接入点 */
    @Schema(description = "访问域名")
    private String endpoint;

    /** 地域 */
    @Schema(description = "地域")
    private String region;

    /** 存储桶名 */
    @Schema(description = "存储桶名")
    private String bucket;

    /** AccessKey（脱敏） */
    @Schema(description = "AccessKey(脱敏)")
    private String accessKey;

    /** SecretKey（脱敏） */
    @Schema(description = "SecretKey(脱敏)")
    private String secretKey;

    /** 本地根路径 或 对象存储路径前缀 */
    @Schema(description = "根路径/前缀")
    private String basePath;

    /** 自定义访问域名/CDN */
    @Schema(description = "自定义域名")
    private String domain;

    /** 是否https（0否 1是） */
    @Schema(description = "是否https")
    private Integer isHttps;

    /** 是否默认存储（0否 1是） */
    @Schema(description = "是否默认")
    private Integer isDefault;

    /** 状态（1启用 0停用） */
    @Schema(description = "状态")
    private Integer status;

    /** 状态描述 */
    @Schema(description = "状态描述")
    private String statusText;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
