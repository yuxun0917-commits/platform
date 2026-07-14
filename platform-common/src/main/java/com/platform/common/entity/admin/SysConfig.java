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
 * 系统配置表
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 参数名称（如：主框架页-默认皮肤样式名称）
     */
    @TableField("config_name")
    private String configName;

    /**
     * 参数键名（如：sys.index.skinName）
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 参数键值（如：skin-blue）
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 系统内置（1是 0否，内置参数不可删除）
     */
    @TableField("config_type")
    private Integer configType;

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
