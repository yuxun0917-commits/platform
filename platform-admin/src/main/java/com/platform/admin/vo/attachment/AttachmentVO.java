package com.platform.admin.vo.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "附件信息")
public class AttachmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 附件ID */
    @Schema(description = "附件ID")
    private Long id;

    /** 存储配置ID */
    @Schema(description = "存储配置ID")
    private Long configId;

    /** 存储配置名称（展示用，由 configId 关联得到） */
    @Schema(description = "存储配置名称")
    private String configName;

    /** 原始文件名 */
    @Schema(description = "原始文件名")
    private String fileName;

    /** 存储键/相对路径 */
    @Schema(description = "存储键")
    private String fileKey;

    /** 文件访问地址 */
    @Schema(description = "文件访问地址")
    private String fileUrl;

    /** 扩展名（不含点） */
    @Schema(description = "扩展名")
    private String fileExt;

    /** MIME类型 */
    @Schema(description = "MIME类型")
    private String contentType;

    /** 文件大小（字节） */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /** 业务类型 */
    @Schema(description = "业务类型")
    private String bizType;

    /** 业务ID */
    @Schema(description = "业务ID")
    private String bizId;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
