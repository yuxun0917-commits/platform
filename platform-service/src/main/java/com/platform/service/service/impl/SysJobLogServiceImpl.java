package com.platform.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysJobLog;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysJobLogMapper;
import com.platform.service.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 定时任务日志表 服务实现类
 * </p>
 *
 * @author platform
 * @since 2026-07-13
 */
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    @Override
    public SysJobLog findById(Long id) {
        SysJobLog log = lambdaQuery()
                .eq(SysJobLog::getId, id)
                .one();
        Assert.notNull(log, "无效的日志id:{}", id);
        return log;
    }

    @Override
    public void paging(Paging<SysJobLog> paging, Map<String, Object> paramsMap) {
        Long jobId = (Long) paramsMap.get("jobId");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                // 以定时任务维度：按 jobId 精确匹配
                .eq(SysJobLog::getJobId, jobId)
                .eq(Objects.nonNull(status), SysJobLog::getStatus, status)
                .orderByDesc(SysJobLog::getId)
                .page(paging);
    }

    @Override
    public void clean() {
        remove(new LambdaQueryWrapper<>());
    }
}
