package com.platform.admin.controller.sysconfig;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.sysconfig.SysConfigEditVO;
import com.platform.admin.vo.sysconfig.SysConfigSaveVO;
import com.platform.admin.vo.sysconfig.SysConfigSelectVO;
import com.platform.admin.vo.sysconfig.SysConfigVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysConfig;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysConfigTypeEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.component.admin.config.SysConfigComponent;
import com.platform.service.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 系统配置管理控制器
 *
 * <p>提供系统参数配置的分页查询、新增、编辑、删除、按 key 查询等接口。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /sysConfig/page        - 配置列表（分页查询）</li>
 *   <li>GET  /sysConfig/select-list - 配置选择列表（性能查询）</li>
 *   <li>GET  /sysConfig/view        - 配置详情</li>
 *   <li>GET  /sysConfig/getByKey    - 按配置键名查询配置值</li>
 *   <li>GET  /sysConfig/enums       - 配置相关枚举列表</li>
 *   <li>POST /sysConfig/add         - 添加配置</li>
 *   <li>POST /sysConfig/edit        - 编辑配置</li>
 *   <li>POST /sysConfig/delete      - 删除配置（逻辑删除）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "系统配置")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/sysConfig")
public class SysConfigController {

    private final SysConfigService sysConfigService;
    private final MapperFacade mapperFacade;
    private final SysConfigComponent sysConfigComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询系统配置列表
     *
     * @param page        页码
     * @param pageSize    页大小
     * @param configType  配置类型（1系统内置 0非内置），可选筛选条件
     * @param keyword     模糊匹配关键词（匹配参数名称、参数键名），可选
     * @return            系统配置分页列表
     */
    @Operation(summary = "配置列表")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer configType, String keyword) {
        Paging<SysConfig> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("configType", configType);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysConfigService.paging(paging, paramsMap);
        paging.convert(config -> {
            SysConfigVO vo = mapperFacade.map(config, SysConfigVO.class);
            vo.setConfigTypeText(SysConfigTypeEnum.getDescByCode(config.getConfigType()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 配置选择列表
     *
     * <p>性能接口：仅返回 id、参数名称两个字段。用于下拉选择等场景。</p>
     *
     * @param page      页码
     * @param pageSize  页大小
     * @param keyword   模糊匹配关键词（匹配参数名称），可选
     * @return          配置选择列表（分页）
     */
    @Operation(summary = "配置选择列表")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysConfig> paging = new Paging<>(page, pageSize);
        sysConfigService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(config -> mapperFacade.map(config, SysConfigSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 配置详情
     *
     * @param id    配置ID
     * @return      配置详细信息
     */
    @Operation(summary = "配置详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "配置ID不能为空") Long id) {
        SysConfig config = sysConfigService.findById(id);
        SysConfigVO vo = mapperFacade.map(config, SysConfigVO.class);
        vo.setConfigTypeText(SysConfigTypeEnum.getDescByCode(config.getConfigType()));
        return Result.success(vo);
    }

    /**
     * 按配置键名查询配置值
     *
     * <p>优先读缓存，缓存未命中查库并写入缓存。</p>
     *
     * @param configKey  配置键名（如 sys.user.initPassword）
     * @return           配置值
     */
    @Operation(summary = "按键名查询配置值")
    @GetMapping("/getByKey")
    public Result getByKey(@NotNull(message = "配置键名不能为空") String configKey) {
        return Result.success(sysConfigComponent.getConfig(configKey));
    }

    /**
     * 配置相关枚举列表
     *
     * <p>返回系统配置模块前端需要的枚举选项（是否内置），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "配置相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(SysConfigTypeEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                }).toList();
        return Result.success(vos);
    }

    // ============================ 数据修改接口 ============================

    /**
     * 添加配置
     *
     * <p>校验配置键名唯一性后保存。</p>
     *
     * @param saveVO    配置添加入参
     * @return          操作结果
     */
    @Operation(summary = "添加配置")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody SysConfigSaveVO saveVO) {
        // 1. 校验配置键名唯一性
        SysConfig existing = sysConfigService.findByKey(saveVO.getConfigKey());
        Assert.isNull(existing, "配置键名【{}】已存在", saveVO.getConfigKey());
        // 2. 校验配置类型合法性
        Assert.notNull(SysConfigTypeEnum.getByCode(saveVO.getConfigType()), "配置类型值不合法（0否 1是）");
        // 3. 保存
        SysConfig config = mapperFacade.map(saveVO, SysConfig.class);
        sysConfigService.save(config);
        // 4. 写入缓存
        sysConfigComponent.setConfig(config.getConfigKey(), config.getConfigValue());
        return Result.success();
    }

    /**
     * 编辑配置
     *
     * <p>修改配置信息，同步更新缓存。</p>
     *
     * @param editVO    配置编辑入参
     * @return          操作结果
     */
    @Operation(summary = "编辑配置")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody SysConfigEditVO editVO) {
        // 1. 校验配置是否存在
        SysConfig config = sysConfigService.findById(editVO.getId());
        // 2. 如果键名变更，校验新键名唯一性
        if (!Objects.equals(config.getConfigKey(), editVO.getConfigKey())) {
            SysConfig existing = sysConfigService.findByKey(editVO.getConfigKey());
            Assert.isNull(existing, "配置键名【{}】已存在", editVO.getConfigKey());
        }
        // 3. 校验配置类型合法性
        Assert.notNull(SysConfigTypeEnum.getByCode(editVO.getConfigType()), "配置类型值不合法（0否 1是）");
        // 4. 更新
        SysConfig update = mapperFacade.map(editVO, SysConfig.class);
        sysConfigService.lambdaUpdate()
                .eq(SysConfig::getId, editVO.getId())
                .update(update);
        // 5. 清除旧缓存，写入新缓存
        sysConfigComponent.cleanConfigCache(config.getConfigKey());
        sysConfigComponent.setConfig(editVO.getConfigKey(), editVO.getConfigValue());
        return Result.success();
    }

    /**
     * 删除配置（逻辑删除）
     *
     * <p>系统内置配置不可删除。</p>
     *
     * @param id    配置ID
     * @return      操作结果
     */
    @Operation(summary = "删除配置")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的配置") Long id) {
        // 1. 校验配置是否存在
        SysConfig config = sysConfigService.findById(id);
        // 2. 系统内置配置不可删除
        Assert.isTrue(SysConfigTypeEnum.NO.fromCode(config.getConfigType()), "系统内置配置不可删除");
        // 3. 逻辑删除
        sysConfigService.lambdaUpdate()
                .set(SysConfig::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .eq(SysConfig::getId, id)
                .update();
        // 4. 清除缓存
        sysConfigComponent.cleanConfigCache(config.getConfigKey());
        return Result.success();
    }
}
