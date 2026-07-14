package com.platform.admin.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 岗位排序入参 VO
 *
 * <p>传入岗位ID列表（按排序顺序）和起始排序值，后端按 startOrder + index 赋值 displayOrder。
 * 分页场景下前端传当前页的起始排序值，各页互不干扰。</p>
 *
 * @author platform
 */
@Data
@Schema(description = "岗位排序参数")
public class PostSortVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 起始排序值（当前页第一条数据的 displayOrder） */
    @Schema(description = "起始排序值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "起始排序值不能为空")
    private Integer startOrder;

    /** 岗位ID列表（按排序顺序排列） */
    @Schema(description = "岗位ID列表（按排序顺序）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "岗位ID列表不能为空")
    private List<Long> ids;
}
