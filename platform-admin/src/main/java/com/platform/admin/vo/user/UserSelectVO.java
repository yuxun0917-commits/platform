package com.platform.admin.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户选择列表 VO
 *
 * <p>用于下拉选择等性能敏感场景，仅返回必要的3个字段：
 * id、昵称、部门名称。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "用户选择列表")
public class UserSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long id;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String nickname;

    /** 部门名称 */
    @Schema(description = "部门名称")
    private String deptName;
}
