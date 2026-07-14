package com.platform.admin.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "岗位信息")
public class PostVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 岗位ID */
    @Schema(description = "岗位ID")
    private Long id;

    /** 岗位编码（如：CEO、HR、DEV） */
    @Schema(description = "岗位编码")
    private String postCode;

    /** 岗位名称 */
    @Schema(description = "岗位名称")
    private String postName;

    /** 排序（升序） */
    @Schema(description = "排序")
    private Integer displayOrder;

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
}
