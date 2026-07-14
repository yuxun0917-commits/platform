package com.platform.admin.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "字典项信息")
public class DictItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典项ID */
    @Schema(description = "字典项ID")
    private Long id;

    /** 字典类型ID（关联 sys_dict.id） */
    @Schema(description = "字典类型ID")
    private Long dictId;

    /** 字典类型（冗余，避免联查） */
    @Schema(description = "字典类型")
    private String dictType;

    /** 字典标签（如：男） */
    @Schema(description = "字典标签")
    private String dictLabel;

    /** 字典键值（如：1） */
    @Schema(description = "字典键值")
    private String dictValue;

    /** 样式属性（如：danger/primary，前端标签颜色） */
    @Schema(description = "样式属性")
    private String cssClass;

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
