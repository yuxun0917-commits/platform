package com.platform.admin.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门树形结构 VO
 *
 * <p>用于部门树形展示，通过 parent_id 自关联递归构建 children。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "部门树形结构")
public class DeptTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @Schema(description = "部门ID")
    private Long id;

    /** 父部门ID（0=顶级） */
    @Schema(description = "父部门ID")
    private Long parentId;

    /** 部门名称 */
    @Schema(description = "部门名称")
    private String deptName;

    /** 负责人 */
    @Schema(description = "负责人")
    private String leader;

    /** 联系电话 */
    @Schema(description = "联系电话")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 排序（升序） */
    @Schema(description = "排序")
    private Integer displayOrder;

    /** 状态（1正常 0禁用） */
    @Schema(description = "状态")
    private Integer status;

    /** 状态描述（用于前端展示） */
    @Schema(description = "状态描述")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 子部门列表 */
    @Schema(description = "子部门列表")
    private List<DeptTreeVO> children;
}
