package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysLoginLog;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysLoginLogMapper;
import com.platform.service.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 登录日志表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-08
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public void paging(Paging<SysLoginLog> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer loginType = (Integer) paramsMap.get("loginType");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(Objects.nonNull(loginType), SysLoginLog::getLoginType, loginType)
                .eq(Objects.nonNull(status), SysLoginLog::getStatus, status)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysLoginLog::getUsername, keyword)
                                .or()
                                .like(SysLoginLog::getLoginIp, keyword))
                .orderByDesc(SysLoginLog::getLoginTime)
                .page(paging);
    }

    @Override
    public SysLoginLog findById(Long id) {
        SysLoginLog sysLoginLog = getById(id);
        Assert.notNull(sysLoginLog, "无效的日志id:{}", id);
        return sysLoginLog;
    }
}
