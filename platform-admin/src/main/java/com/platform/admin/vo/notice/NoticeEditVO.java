package com.platform.admin.vo.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知公告编辑入参 VO
 *
 * <p>在 NoticeSaveVO 基础上增加 id 字段，其他字段校验与 NoticeSaveVO 一致。
 * 字段长度依据 sys_notice 表定义。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "通知公告编辑参数")
public class NoticeEditVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @Schema(description = "通知ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "通知ID不能为空")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 128, message = "通知标题长度不能超过128个字符")
    private String title;

    /** 展示位置（1后台 2前台） */
    @Schema(description = "展示位置（1后台 2前台）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "展示位置不能为空")
    private Integer position;

    /** 通知内容（支持富文本） */
    @Schema(description = "通知内容")
    private String content;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 备注 */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
