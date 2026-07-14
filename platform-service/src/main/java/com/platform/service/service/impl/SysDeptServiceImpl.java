package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysDept;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.DeptStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysDeptMapper;
import com.platform.service.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-06
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public SysDept findById(Long id) {
        SysDept sysDept = lambdaQuery()
                .eq(SysDept::getId, id)
                .eq(SysDept::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(sysDept, "无效的部门id:{}", id);
        return sysDept;
    }

    @Override
    public List<SysDept> listTree() {
        return lambdaQuery()
                .eq(SysDept::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .orderByAsc(SysDept::getDisplayOrder)
                .orderByDesc(SysDept::getId)
                .list();
    }

    @Override
    public void selectList(Paging<SysDept> paging, String keyword) {
        lambdaQuery()
                .select(SysDept::getId, SysDept::getDeptName)
                .eq(SysDept::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysDept::getStatus, DeptStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysDept::getDeptName, keyword)
                .orderByDesc(SysDept::getId)
                .page(paging);
    }
}
