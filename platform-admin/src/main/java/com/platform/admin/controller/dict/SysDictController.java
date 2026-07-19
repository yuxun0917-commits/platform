package com.platform.admin.controller.dict;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.admin.vo.dict.*;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysDict;
import com.platform.common.entity.admin.SysDictItem;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysDictStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
import com.platform.common.utils.JacksonUtil;
import com.platform.common.vo.EnumVO;
import com.platform.component.dict.DictComponent;
import com.platform.service.service.SysDictItemService;
import com.platform.service.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 字典类型管理控制器
 *
 * <p>提供字典类型的分页查询、新增、编辑、删除，以及按字典类型编码获取字典项列表等接口。
 * 字典项列表查询走 Redis 缓存，增删改后异步清除缓存。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /dict/page        - 字典类型列表（分页查询）</li>
 *   <li>GET  /dict/select-list - 字典类型选择列表（性能查询）</li>
 *   <li>GET  /dict/view        - 字典类型详情</li>
 *   <li>GET  /dict/items       - 根据字典类型编码获取字典项列表（前端下拉用，带缓存）</li>
 *   <li>GET  /dict/enums       - 字典相关枚举列表</li>
 *   <li>POST /dict/add         - 添加字典类型</li>
 *   <li>POST /dict/edit        - 编辑字典类型</li>
 *   <li>POST /dict/delete      - 删除字典类型（逻辑删除）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "字典管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dict")
public class SysDictController {

    private final SysDictService sysDictService;
    private final SysDictItemService sysDictItemService;
    private final MapperFacade mapperFacade;
    private final DictComponent dictComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询字典类型列表
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param status   状态（1正常 0禁用），可选筛选条件
     * @param keyword  模糊匹配关键词（匹配字典名称、字典类型），可选
     * @return 字典类型分页列表
     */
    @Operation(summary = "字典类型列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Integer status, String keyword) {
        Paging<SysDict> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysDictService.paging(paging, paramsMap);
        paging.convert(dict -> {
            DictVO vo = mapperFacade.map(dict, DictVO.class);
            vo.setStatusText(SysDictStatusEnum.getDescByCode(dict.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 字典类型选择列表
     *
     * <p>性能接口：仅返回 id、字典名称两个字段。用于下拉选择等场景。</p>
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param keyword  模糊匹配关键词（匹配字典名称），可选
     * @return 字典类型选择列表（分页）
     */
    @Operation(summary = "字典类型选择列表")
    @SaCheckPermission("system:dict:select-list")
    @GetMapping("/select-list")
    public Result selectList(Integer page, Integer pageSize, String keyword) {
        Paging<SysDict> paging = new Paging<>(page, pageSize);
        sysDictService.selectList(paging, StrUtil.trim(keyword));
        paging.convert(dict -> mapperFacade.map(dict, DictSelectVO.class));
        return Result.success(paging);
    }

    /**
     * 字典类型详情
     *
     * @param id 字典ID
     * @return 字典类型详细信息
     */
    @Operation(summary = "字典类型详情")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/view")
    public Result view(@NotNull(message = "字典ID不能为空") Long id) {
        SysDict dict = sysDictService.findById(id);
        DictVO vo = mapperFacade.map(dict, DictVO.class);
        vo.setStatusText(SysDictStatusEnum.getDescByCode(dict.getStatus()));
        return Result.success(vo);
    }

    /**
     * 根据字典类型编码获取字典项列表
     *
     * <p>字典核心接口，供前端下拉选择使用。优先读 Redis 缓存，缓存未命中查库并写入缓存。
     * 只返回正常状态的字典项，按 display_order 升序排列。</p>
     *
     * @param dictType 字典类型编码（如 sys_user_gender）
     * @return 字典项列表
     */
    @Operation(summary = "根据字典类型获取字典项")
    @GetMapping("/items")
    public Result items(@NotNull(message = "字典类型不能为空") String dictType) {
        // 1. 优先读缓存
        Object cached = dictComponent.getDictItems(dictType);
        if (Objects.nonNull(cached)) {
            List<DictItemVO> cachedList = JacksonUtil.parseTypeRef(cached.toString(), new TypeReference<List<DictItemVO>>() {});
            if (Objects.nonNull(cachedList)) {
                return Result.success(cachedList);
            }
        }
        // 2. 缓存未命中：查库
        List<SysDictItem> items = sysDictItemService.listByDictType(dictType);
        List<DictItemVO> vos = items.stream().map(item -> {
            DictItemVO vo = mapperFacade.map(item, DictItemVO.class);
            vo.setStatusText(SysDictStatusEnum.getDescByCode(item.getStatus()));
            return vo;
        }).toList();
        // 3. 写入缓存
        dictComponent.cacheDictItems(dictType, JacksonUtil.toJsonString(vos));
        return Result.success(vos);
    }

    /**
     * 字典相关枚举列表
     *
     * <p>返回字典模块前端需要的枚举选项（字典状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "字典相关枚举列表")
    @GetMapping("/enums")
    public Result enums() {
        List<EnumVO> vos = Arrays.stream(SysDictStatusEnum.values())
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
     * 添加字典类型
     *
     * <p>校验字典类型编码唯一性后保存。</p>
     *
     * @param saveVO 字典类型添加入参
     * @return 操作结果
     */
    @Operation(summary = "添加字典类型")
    @SaCheckPermission("system:dict:add")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody DictSaveVO saveVO) {
        // 1. 校验字典类型编码唯一性
        SysDict existing = sysDictService.findByDictType(saveVO.getDictType());
        Assert.isNull(existing, "字典类型【{}】已存在", saveVO.getDictType());
        // 2. 校验状态合法性
        Assert.notNull(SysDictStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 3. 保存
        SysDict dict = mapperFacade.map(saveVO, SysDict.class);
        dict.setCreateBy(SecurityUser.getUserId())
                .setCreateTime(LocalDateTime.now())
                .setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysDictService.save(dict);
        return Result.success();
    }

    /**
     * 编辑字典类型
     *
     * <p>修改字典类型信息，字典类型编码变更时同步清除新旧缓存。</p>
     *
     * @param editVO 字典类型编辑入参
     * @return 操作结果
     */
    @Operation(summary = "编辑字典类型")
    @SaCheckPermission("system:dict:edit")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody DictEditVO editVO) {
        // 1. 校验字典类型是否存在
        SysDict dict = sysDictService.findById(editVO.getId());
        // 2. 如果字典类型编码变更，校验新编码唯一性
        if (!Objects.equals(dict.getDictType(), editVO.getDictType())) {
            SysDict existing = sysDictService.findByDictType(editVO.getDictType());
            Assert.isNull(existing, "字典类型【{}】已存在", editVO.getDictType());
        }
        // 3. 校验状态合法性
        Assert.notNull(SysDictStatusEnum.getByCode(editVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 4. 更新
        SysDict updateDict = mapperFacade.map(editVO, SysDict.class);
        updateDict.setUpdateBy(SecurityUser.getUserId())
                .setUpdateTime(LocalDateTime.now());
        sysDictService.updateById(updateDict);
        // 5. 清除缓存（旧编码和新编码都清）
        dictComponent.cleanDictCache(dict.getDictType());
        if (!Objects.equals(dict.getDictType(), editVO.getDictType())) {
            dictComponent.cleanDictCache(editVO.getDictType());
        }
        return Result.success();
    }

    /**
     * 删除字典类型（逻辑删除）
     *
     * <p>存在字典项的字典类型不可删除，需先删除其下字典项。</p>
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的字典类型") Long id) {
        // 1. 校验字典类型是否存在
        SysDict dict = sysDictService.findById(id);
        // 2. 校验是否存在字典项
        List<SysDictItem> items = sysDictItemService.listByDictId(id);
        Assert.isTrue(items.isEmpty(), "该字典类型下存在字典项，无法删除");
        // 3. 逻辑删除
        sysDictService.lambdaUpdate()
                .eq(SysDict::getId, id)
                .set(SysDict::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .set(SysDict::getUpdateBy, SecurityUser.getUserId())
                .set(SysDict::getUpdateTime, LocalDateTime.now())
                .update();
        // 4. 清除缓存
        dictComponent.cleanDictCache(dict.getDictType());
        return Result.success();
    }
}
