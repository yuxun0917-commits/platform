package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysDict;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysDictStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysDictMapper;
import com.platform.service.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 字典类型表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public SysDict findById(Long id) {
        SysDict dict = lambdaQuery()
                .eq(SysDict::getId, id)
                .eq(SysDict::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(dict, "无效的字典id:{}", id);
        return dict;
    }

    @Override
    public void paging(Paging<SysDict> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysDict::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysDict::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysDict::getDictName, keyword)
                                .or()
                                .like(SysDict::getDictType, keyword))
                .orderByDesc(SysDict::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysDict> paging, String keyword) {
        lambdaQuery()
                .select(SysDict::getId, SysDict::getDictName)
                .eq(SysDict::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysDict::getStatus, SysDictStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysDict::getDictName, keyword)
                .orderByDesc(SysDict::getId)
                .page(paging);
    }

    @Override
    public SysDict findByDictType(String dictType) {
        return lambdaQuery()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
    }
}
