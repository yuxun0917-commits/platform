package com.platform.common.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典项表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_dict_item")
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型ID（关联 sys_dict.id）
     */
    @TableField("dict_id")
    private Long dictId;

    /**
     * 字典类型（冗余，避免联查）
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 字典标签（如：男）
     */
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 字典键值（如：1）
     */
    @TableField("dict_value")
    private String dictValue;

    /**
     * 样式属性（如：danger/primary，前端标签颜色）
     */
    @TableField("css_class")
    private String cssClass;

    /**
     * 排序（升序）
     */
    @TableField("display_order")
    private Integer displayOrder;

    /**
     * 状态（1正常 0禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除（0未删除 1已删除）
     */
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 创建人（0=系统操作）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（0=系统操作）
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
