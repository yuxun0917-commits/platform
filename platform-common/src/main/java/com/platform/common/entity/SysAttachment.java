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
 * 附件表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_attachment")
public class SysAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存储配置ID（关联 sys_storage_config.id，永远指向一条真实配置）
     */
    @TableField("config_id")
    private Long configId;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 存储键/相对路径（本地=日期目录+UUID；对象存储=object key）
     */
    @TableField("file_key")
    private String fileKey;

    /**
     * 扩展名（不含点）
     */
    @TableField("file_ext")
    private String fileExt;

    /**
     * MIME类型
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 业务类型（tinyint，对应 AttachmentBizTypeEnum：1头像 2文章图片 3文档 4导入模板 5其他）
     */
    @TableField("biz_type")
    private Integer bizType;

    /**
     * 业务ID（关联具体业务记录，如用户ID）
     */
    @TableField("biz_id")
    private Long bizId;

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
