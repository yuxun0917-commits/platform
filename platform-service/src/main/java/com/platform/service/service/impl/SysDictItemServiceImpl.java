package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysDictItem;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysDictStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysDictItemMapper;
import com.platform.service.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 字典项表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public SysDictItem findById(Long id) {
        SysDictItem item = lambdaQuery()
                .eq(SysDictItem::getId, id)
                .eq(SysDictItem::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(item, "无效的字典项id:{}", id);
        return item;
    }

    @Override
    public void paging(Paging<SysDictItem> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Long dictId = (Long) paramsMap.get("dictId");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysDictItem::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(dictId), SysDictItem::getDictId, dictId)
                .eq(Objects.nonNull(status), SysDictItem::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysDictItem::getDictLabel, keyword)
                                .or()
                                .like(SysDictItem::getDictValue, keyword))
                .orderByAsc(SysDictItem::getDisplayOrder)
                .orderByDesc(SysDictItem::getId)
                .page(paging);
    }

    @Override
    public List<SysDictItem> listByDictType(String dictType) {
        return lambdaQuery()
                .eq(SysDictItem::getDictType, dictType)
                .eq(SysDictItem::getStatus, SysDictStatusEnum.NORMAL.getCode())
                .eq(SysDictItem::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .orderByAsc(SysDictItem::getDisplayOrder)
                .list();
    }

    @Override
    public List<SysDictItem> listByDictId(Long dictId) {
        return lambdaQuery()
                .eq(SysDictItem::getDictId, dictId)
                .eq(SysDictItem::getStatus, SysDictStatusEnum.NORMAL.getCode())
                .eq(SysDictItem::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .orderByAsc(SysDictItem::getDisplayOrder)
                .list();
    }
}
