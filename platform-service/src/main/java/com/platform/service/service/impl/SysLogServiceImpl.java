package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysLog;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysLogMapper;
import com.platform.service.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-08
 */
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Override
    public void paging(Paging<SysLog> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(Objects.nonNull(status), SysLog::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysLog::getTitle, keyword)
                                .or()
                                .like(SysLog::getOperName, keyword))
                .orderByDesc(SysLog::getOperTime)
                .page(paging);
    }

    @Override
    public SysLog findById(Long id) {
        SysLog sysLog = getById(id);
        Assert.notNull(sysLog, "无效的日志id:{}", id);
        return sysLog;
    }
}
