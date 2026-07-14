package com.platform.common.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文件存储配置表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_storage_config")
public class SysStorageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称（如 阿里云生产桶）
     */
    @TableField("config_name")
    private String configName;

    /**
     * 存储类型(1本地 2阿里云OSS 3腾讯COS 4MinIO)，见 StorageTypeEnum
     */
    @TableField("storage_type")
    private Integer storageType;

    /**
     * 访问域名/接入点（对象存储用）
     */
    @TableField("endpoint")
    private String endpoint;

    /**
     * 地域（COS等需要）
     */
    @TableField("region")
    private String region;

    /**
     * 存储桶名
     */
    @TableField("bucket")
    private String bucket;

    /**
     * AccessKey / SecretId
     */
    @TableField("access_key")
    private String accessKey;

    /**
     * SecretKey
     */
    @TableField("secret_key")
    private String secretKey;

    /**
     * 本地根路径 或 对象存储路径前缀
     */
    @TableField("base_path")
    private String basePath;

    /**
     * 自定义访问域名/CDN（拼URL用）
     */
    @TableField("domain")
    private String domain;

    /**
     * 是否https（0否 1是）
     */
    @TableField("is_https")
    private Integer isHttps;

    /**
     * 是否默认存储（0否 1是，全表仅一条为1）
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 状态（1启用 0停用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除（0未删除 1已删除）
     */
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 创建人（0=系统）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（0=系统）
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
