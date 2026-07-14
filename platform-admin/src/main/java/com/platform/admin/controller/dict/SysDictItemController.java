package com.platform.admin.controller.dict;

import cn.hutool.core.util.StrUtil;
import com.platform.admin.vo.dict.DictItemEditVO;
import com.platform.admin.vo.dict.DictItemSaveVO;
import com.platform.admin.vo.dict.DictItemVO;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.entity.admin.SysDict;
import com.platform.common.entity.admin.SysDictItem;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysDictStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.result.Result;
import com.platform.common.utils.Assert;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 字典项管理控制器
 *
 * <p>提供字典项的分页查询、新增、编辑、删除接口。字典项增删改后异步清除对应字典类型缓存。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *   <li>GET  /dictItem/page   - 字典项列表（分页查询，按 dictId 筛选）</li>
 *   <li>GET  /dictItem/view   - 字典项详情</li>
 *   <li>GET  /dictItem/enums  - 字典项相关枚举列表</li>
 *   <li>POST /dictItem/add    - 添加字典项</li>
 *   <li>POST /dictItem/edit   - 编辑字典项</li>
 *   <li>POST /dictItem/delete - 删除字典项（逻辑删除）</li>
 * </ul>
 *
 * @author platform
 */
@Tag(name = "字典项管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dictItem")
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;
    private final SysDictService sysDictService;
    private final MapperFacade mapperFacade;
    private final DictComponent dictComponent;

    // ============================ 查询接口 ============================

    /**
     * 分页查询字典项列表
     *
     * @param page     页码
     * @param pageSize 页大小
     * @param dictId   字典类型ID，可选筛选条件
     * @param status   状态（1正常 0禁用），可选筛选条件
     * @param keyword  模糊匹配关键词（匹配字典标签、字典键值），可选
     * @return 字典项分页列表
     */
    @Operation(summary = "字典项列表")
    @GetMapping("/page")
    public Result page(Integer page, Integer pageSize, Long dictId, Integer status, String keyword) {
        Paging<SysDictItem> paging = new Paging<>(page, pageSize);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("dictId", dictId);
        paramsMap.put("status", status);
        paramsMap.put("keyword", StrUtil.trim(keyword));
        sysDictItemService.paging(paging, paramsMap);
        paging.convert(item -> {
            DictItemVO vo = mapperFacade.map(item, DictItemVO.class);
            vo.setStatusText(SysDictStatusEnum.getDescByCode(item.getStatus()));
            return vo;
        });
        return Result.success(paging);
    }

    /**
     * 字典项详情
     *
     * @param id 字典项ID
     * @return 字典项详细信息
     */
    @Operation(summary = "字典项详情")
    @GetMapping("/view")
    public Result view(@NotNull(message = "字典项ID不能为空") Long id) {
        SysDictItem item = sysDictItemService.findById(id);
        DictItemVO vo = mapperFacade.map(item, DictItemVO.class);
        vo.setStatusText(SysDictStatusEnum.getDescByCode(item.getStatus()));
        return Result.success(vo);
    }

    /**
     * 字典项相关枚举列表
     *
     * <p>返回字典项模块前端需要的枚举选项（字典状态），供下拉选择使用。</p>
     *
     * @return 枚举选项列表
     */
    @Operation(summary = "字典项相关枚举列表")
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
     * 添加字典项
     *
     * <p>校验字典类型存在性及编码一致性后保存，清除对应字典类型缓存。</p>
     *
     * @param saveVO 字典项添加入参
     * @return 操作结果
     */
    @Operation(summary = "添加字典项")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody DictItemSaveVO saveVO) {
        // 1. 校验字典类型存在
        SysDict dict = sysDictService.findById(saveVO.getDictId());
        // 2. 校验字典类型编码与传入一致（冗余字段一致性校验）
        Assert.isTrue(Objects.equals(dict.getDictType(), saveVO.getDictType()), "字典类型编码与字典类型不匹配");
        // 3. 校验状态合法性
        Assert.notNull(SysDictStatusEnum.getByCode(saveVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 4. 保存
        SysDictItem item = mapperFacade.map(saveVO, SysDictItem.class);
        sysDictItemService.save(item);
        // 5. 清除缓存
        dictComponent.cleanDictCache(saveVO.getDictType());
        return Result.success();
    }

    /**
     * 编辑字典项
     *
     * <p>修改字典项信息，字典类型变更时同步清除新旧字典类型缓存。</p>
     *
     * @param editVO 字典项编辑入参
     * @return 操作结果
     */
    @Operation(summary = "编辑字典项")
    @PostMapping("/edit")
    public Result edit(@Valid @RequestBody DictItemEditVO editVO) {
        // 1. 校验字典项是否存在
        SysDictItem item = sysDictItemService.findById(editVO.getId());
        // 2. 校验字典类型存在
        SysDict dict = sysDictService.findById(editVO.getDictId());
        // 3. 校验字典类型编码一致性
        Assert.isTrue(Objects.equals(dict.getDictType(), editVO.getDictType()), "字典类型编码与字典类型不匹配");
        // 4. 校验状态合法性
        Assert.notNull(SysDictStatusEnum.getByCode(editVO.getStatus()), "状态值不合法（0禁用 1正常）");
        // 5. 更新
        SysDictItem update = mapperFacade.map(editVO, SysDictItem.class);
        sysDictItemService.lambdaUpdate()
                .eq(SysDictItem::getId, editVO.getId())
                .update(update);
        // 6. 清除缓存（旧编码和新编码）
        dictComponent.cleanDictCache(item.getDictType());
        if (!Objects.equals(item.getDictType(), editVO.getDictType())) {
            dictComponent.cleanDictCache(editVO.getDictType());
        }
        return Result.success();
    }

    /**
     * 删除字典项（逻辑删除）
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典项")
    @PostMapping("/delete")
    @JsonCoverParam
    public Result delete(@NotNull(message = "请选择需要删除的字典项") Long id) {
        // 1. 校验字典项是否存在
        SysDictItem item = sysDictItemService.findById(id);
        // 2. 逻辑删除
        sysDictItemService.lambdaUpdate()
                .set(SysDictItem::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .eq(SysDictItem::getId, id)
                .update();
        // 3. 清除缓存
        dictComponent.cleanDictCache(item.getDictType());
        return Result.success();
    }
}
