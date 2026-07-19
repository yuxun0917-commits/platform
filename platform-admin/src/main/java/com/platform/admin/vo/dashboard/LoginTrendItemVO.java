package com.platform.admin.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 登录趋势项展示 VO
 *
 * @author platform
 */
@Data
@Schema(description = "登录趋势项")
public class LoginTrendItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录日期")
    private LocalDate loginDate;

    @Schema(description = "登录次数")
    private Long count;
}
