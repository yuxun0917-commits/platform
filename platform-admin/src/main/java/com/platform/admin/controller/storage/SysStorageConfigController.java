package com.platform.admin.controller.storage;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.storage.StorageConfigEditVO;
import com.platform.admin.vo.storage.StorageConfigSaveVO;
import com.platform.admin.vo.storage.StorageConfigVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.StorageConfigStatusEnum;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.vo.EnumVO;
import com.platform.service.service.SysAttachmentService;
import com.platform.service.service.SysStorageConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 存储配置管理控制器
 *
 * <p>提供存储配置的分页、选择列表、详情、枚举、新增、编辑、删除、设为默认、启停等接口。
 * 后台管理员可在此人为切换默认存储后端（本地/OSS/COS/MinIO），无需改 yml 重启。</p>
 *
 * <ul>
 *   <li>GET  /storage-config/page        - 存储配置列表（分页）</li>
 *   <li>GET  /storage-config/select-list - 存储配置选择列表（仅 id+名称）</li>
 *   <li>GET  /storage-config/view        - 存储配置详情（密钥脱敏）</li>
 *   <li>GET  /storage-config/enums       - 存储类型与状态枚举</li>
 *   <li>POST /storage-config/add         - 新增存储配置</li>
 *   <li>POST /storage-config/edit        - 编辑存储配置</li>
 *   <li>POST /storage-config/delete      - 删除存储配置（逻辑删除，默认配置/有附件引用不可删）</li>
 *   <li>POST /storage-config/set-default - 设为默认存储</li>
 *   <li>POST /storage-config/edit-status - 启停存储配置</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "存储配置管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/storage-config")
public class SysStorageConfigController {

    private final SysStorageConfigService storageConfigService;
    private final SysAttachmentService attachmentService;
    private final MapperFacade mapperFacade;

    /**
     * 分页查询存储配置
     */
    @Operation(summary = "存储配置列表")
    @SaCheckPermission("system:storage:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysStorageConfig> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        storageConfigService.paging(paging, paramsMap);
        paging.convert(cfg -> {
            StorageConfigVO vo = mapperFacade.map(cfg, StorageConfigVO.class);
            vo.setStorageTypeText(StorageTypeEnum.getDescByCode(cfg.getStorageType()));
            vo.setStatusText(StorageConfigStatusEnum.getDescByCode(cfg.getStatus()));
            vo.setAccessKey(mask(cfg.getAccessKey()));
            vo.setSecretKey(mask(cfg.getSecretKey()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 存储配置选择列表（仅返回 id + 配置名称，启用状态）
     */
    @Operation(summary = "存储配置选择列表")
    @SaCheckPermission("system:storage:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysStorageConfig> paging = new Paging<>(page, pageSize);
        storageConfigService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(cfg -> mapperFacade.map(cfg, StorageConfigVO.class));
        return Result.success(paging);
    }

    /**
     * 存储配置详情（密钥脱敏）
     */
    @Operation(summary = "存储配置详情")
    @SaCheckPermission("system:storage:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "配置ID不能为空") Long id) {
        SysStorageConfig cfg = storageConfigService.findById(id);
        StorageConfigVO vo = mapperFacade.map(cfg, StorageConfigVO.class);
        vo.setStorageTypeText(StorageTypeEnum.getDescByCode(cfg.getStorageType()));
        vo.setStatusText(StorageConfigStatusEnum.getDescByCode(cfg.getStatus()));
        vo.setAccessKey(mask(cfg.getAccessKey()));
        vo.setSecretKey(mask(cfg.getSecretKey()));
        return Result.success(vo);
    }

    /**
     * 存储类型与状态枚举（前端下拉用）
     */
    @Operation(summary = "存储配置相关枚举")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> types = Arrays.stream(StorageTypeEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                })
                .toList();
        List<EnumVO> statuses = Arrays.stream(StorageConfigStatusEnum.values())
                .map(e -> {
                    EnumVO vo = new EnumVO();
                    vo.setCode(e.getCode());
                    vo.setDesc(e.getDesc());
                    return vo;
                })
                .toList();
        Map<String, Object> map = new HashMap<>(4);
        map.put("storageType", types);
        map.put("status", statuses);
        return Result.success(map);
    }

    /**
     * 新增存储配置
     */
    @Operation(summary = "新增存储配置")
    @SaCheckPermission("system:storage:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody StorageConfigSaveVO saveVO) {
        Assert.notNull(StorageTypeEnum.getByCode(saveVO.getStorageType()), "存储类型不合法");
        Assert.notNull(StorageConfigStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法");
        SysStorageConfig config = mapperFacade.map(saveVO, SysStorageConfig.class);
        config.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        storageConfigService.save(config);
        return Result.success();
    }

    /**
     * 编辑存储配置
     */
    @Operation(summary = "编辑存储配置")
    @SaCheckPermission("system:storage:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody StorageConfigEditVO editVO) {
        storageConfigService.findById(editVO.getId());
        Assert.notNull(StorageTypeEnum.getByCode(editVO.getStorageType()), "存储类型不合法");
        Assert.notNull(StorageConfigStatusEnum.getByCode(editVO.getStatus()), "状态值不合法");
        SysStorageConfig updateConfig = mapperFacade.map(editVO, SysStorageConfig.class);
        updateConfig.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        storageConfigService.updateById(updateConfig);
        return Result.success();
    }

    /**
     * 删除存储配置（逻辑删除）
     */
    @Operation(summary = "删除存储配置")
    @SaCheckPermission("system:storage:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的存储配置") Long id) {
        SysStorageConfig config = storageConfigService.findById(id);
        Assert.isTrue(config.getIsDefault() != 1, "默认存储配置不可删除");
        long count = attachmentService.countByConfigId(id);
        Assert.isTrue(count == 0, "该存储配置下仍有{}个附件，无法删除", count);
        storageConfigService.lambdaUpdate()
                .eq(SysStorageConfig::getId, id)
                .set(SysStorageConfig::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysStorageConfig::getUpdateBy, SecurityUser.getUserId())
                .set(SysStorageConfig::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success();
    }

    /**
     * 设为默认存储
     */
    @Operation(summary = "设为默认存储")
    @SaCheckPermission("system:storage:setDefault")
    @PostMapping("/set-default")
    public Result setDefault(@NotNull(message = "配置ID不能为空") Long id) {
        storageConfigService.setDefault(id);
        return Result.success();
    }

    /**
     * 启停存储配置
     */
    @Operation(summary = "启停存储配置")
    @SaCheckPermission("system:storage:editStatus")
    @PostMapping("/edit-status")
    public Result editStatus(@NotNull(message = "配置ID不能为空") Long id,
                             @NotNull(message = "状态不能为空") Integer status) {
        storageConfigService.findById(id);
        Assert.notNull(StorageConfigStatusEnum.getByCode(status), "状态值不合法");
        storageConfigService.lambdaUpdate()
                .eq(SysStorageConfig::getId, id)
                .set(SysStorageConfig::getStatus, status)
                .set(SysStorageConfig::getUpdateBy, SecurityUser.getUserId())
                .set(SysStorageConfig::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success();
    }

    /**
     * 密钥脱敏：保留前后各2位，中间用 **** 替代
     */
    private String mask(String value) {
        if (StrUtil.isBlank(value) || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
