package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysJob;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.JobStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysJobMapper;
import com.platform.service.service.SysJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 定时任务表 服务实现类
 * </p>
 *
 * @author platform
 * @since 2026-07-13
 */
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    @Override
    public SysJob findById(Long id) {
        SysJob job = lambdaQuery()
                .eq(SysJob::getId, id)
                .eq(SysJob::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(job, "无效的任务id:{}", id);
        return job;
    }

    @Override
    public void paging(Paging<SysJob> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysJob::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysJob::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysJob::getJobName, keyword)
                                .or()
                                .like(SysJob::getJobGroup, keyword))
                .orderByDesc(SysJob::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysJob> paging, String keyword) {
        lambdaQuery()
                .select(SysJob::getId, SysJob::getJobName)
                .eq(SysJob::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysJob::getJobName, keyword)
                .orderByDesc(SysJob::getId)
                .page(paging);
    }

    @Override
    public List<SysJob> listNormalRunningJobs() {
        return lambdaQuery()
                .eq(SysJob::getStatus, JobStatusEnum.NORMAL.getCode())
                .eq(SysJob::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .list();
    }
}
