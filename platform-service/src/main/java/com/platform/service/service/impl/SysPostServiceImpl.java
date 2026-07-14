package com.platform.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysPost;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysPostStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysPostMapper;
import com.platform.service.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 岗位表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    @Override
    public SysPost findById(Long id) {
        SysPost post = lambdaQuery()
                .eq(SysPost::getId, id)
                .eq(SysPost::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(post, "无效的岗位id:{}", id);
        return post;
    }

    @Override
    public void paging(Paging<SysPost> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysPost::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysPost::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysPost::getPostName, keyword)
                                .or()
                                .like(SysPost::getPostCode, keyword))
                .orderByDesc(SysPost::getId)
                .orderByAsc(SysPost::getDisplayOrder)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysPost> paging, String keyword) {
        lambdaQuery()
                .select(SysPost::getId, SysPost::getPostName)
                .eq(SysPost::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysPost::getStatus, SysPostStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysPost::getPostName, keyword)
                .orderByDesc(SysPost::getId)
                .page(paging);
    }

    @Override
    public SysPost findByPostCode(String postCode) {
        return lambdaQuery()
                .eq(SysPost::getPostCode, postCode)
                .eq(SysPost::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
    }

    @Override
    public Map<Long, SysPost> findPartMapByIds(Set<Long> pIds) {
        if (CollUtil.isEmpty(pIds)) {
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .select(SysPost::getId, SysPost::getPostName, SysPost::getPostCode)
                .in(SysPost::getId, pIds)
                .eq(SysPost::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .list().stream()
                .collect(Collectors.toMap(SysPost::getId, Function.identity()));
    }
}
