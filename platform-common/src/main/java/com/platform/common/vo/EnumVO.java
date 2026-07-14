package com.platform.common.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举选项 VO
 *
 * <p>用于向前端返回枚举的可选项列表（code + desc），
 * 供下拉选择、状态筛选等场景使用。</p>
 *
 * @author platform
 */
@Data
public class EnumVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 枚举值 */
    private Integer code;

    /** 枚举描述 */
    private String desc;
}
