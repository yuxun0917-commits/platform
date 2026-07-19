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

    /** 预览链接（仅图片类附件返回，可直接用 <img>/<el-avatar> 渲染；非图片为 null） */
    @Schema(description = "预览链接(仅图片)")
    private String previewUrl;

    /** 扩展名（不含点） */
    @Schema(description = "扩展名")
    private String fileExt;

    /** MIME类型 */
    @Schema(description = "MIME类型")
    private String contentType;

    /** 文件大小（字节） */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /** 业务类型（code，对应 AttachmentBizTypeEnum） */
    @Schema(description = "业务类型（1头像 2文章图片 3文档 4导入模板 5其他）")
    private Integer bizType;

    /** 业务类型描述（由后端根据 bizType 填充，便于前端展示） */
    @Schema(description = "业务类型描述")
    private String bizTypeDesc;

    /** 业务ID */
    @Schema(description = "业务ID")
    private Long bizId;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
