package com.platform.admin.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门选择列表 VO
 *
 * <p>用于下拉选择等性能敏感场景，仅返回必要的2个字段：id、部门名称。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "部门选择列表")
public class DeptSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @Schema(description = "部门ID")
    private Long id;

    /** 部门名称 */
    @Schema(description = "部门名称")
    private String deptName;
}
