package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysConfig;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysConfigMapper;
import com.platform.service.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author platform
 * @since 2026-07-09
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public SysConfig findById(Long id) {
        SysConfig config = lambdaQuery()
                .eq(SysConfig::getId, id)
                .eq(SysConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(config, "无效的配置id:{}", id);
        return config;
    }

    @Override
    public void paging(Paging<SysConfig> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer configType = (Integer) paramsMap.get("configType");
        lambdaQuery()
                .eq(SysConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(configType), SysConfig::getConfigType, configType)
                .and(Objects.nonNull(keyword) && !keyword.isBlank(),
                        w -> w.like(SysConfig::getConfigName, keyword)
                                .or()
                                .like(SysConfig::getConfigKey, keyword))
                .orderByDesc(SysConfig::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysConfig> paging, String keyword) {
        lambdaQuery()
                .select(SysConfig::getId, SysConfig::getConfigName)
                .eq(SysConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysConfig::getConfigName, keyword)
                .orderByDesc(SysConfig::getId)
                .page(paging);
    }

    @Override
    public SysConfig findByKey(String configKey) {
        return lambdaQuery()
                .eq(SysConfig::getConfigKey, configKey)
                .eq(SysConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
    }
}
