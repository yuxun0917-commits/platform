package com.platform.admin.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "字典类型信息")
public class DictVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典ID */
    @Schema(description = "字典ID")
    private Long id;

    /** 字典名称（如：用户性别） */
    @Schema(description = "字典名称")
    private String dictName;

    /** 字典类型（如：sys_user_gender） */
    @Schema(description = "字典类型")
    private String dictType;

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
