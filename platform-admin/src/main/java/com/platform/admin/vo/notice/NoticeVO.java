package com.platform.admin.vo.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "通知公告信息")
public class NoticeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @Schema(description = "通知ID")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题")
    private String title;

    /** 展示位置（1后台 2前台） */
    @Schema(description = "展示位置")
    private Integer position;

    /** 展示位置描述 */
    @Schema(description = "展示位置描述")
    private String positionText;

    /** 通知内容（支持富文本） */
    @Schema(description = "通知内容")
    private String content;

    /** 状态（1正常 0禁用） */
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
    @Schema(description = "更新时间", example = "2026-06-28T10:00:00")
    private LocalDateTime updateTime;
}
