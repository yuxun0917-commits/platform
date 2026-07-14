package com.platform.admin.vo.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知公告选择列表 VO
 *
 * <p>用于下拉选择等性能敏感场景，仅返回必要的2个字段：id、通知标题。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "通知公告选择列表")
public class NoticeSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @Schema(description = "通知ID")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题")
    private String noticeTitle;
}
