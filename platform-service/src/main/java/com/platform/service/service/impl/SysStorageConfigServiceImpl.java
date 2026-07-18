package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.StorageConfigStatusEnum;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysStorageConfigMapper;
import com.platform.service.service.SysStorageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件存储配置表 服务实现类
 * </p>
 *
 * @author platform
 * @since 2026-07-14
 */
@Service
@RequiredArgsConstructor
public class SysStorageConfigServiceImpl extends ServiceImpl<SysStorageConfigMapper, SysStorageConfig> implements SysStorageConfigService {

    @Override
    public SysStorageConfig findById(Long id) {
        SysStorageConfig config = lambdaQuery()
                .eq(SysStorageConfig::getId, id)
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(config, "无效的存储配置id:{}", id);
        return config;
    }

    @Override
    public SysStorageConfig findDefault() {
        SysStorageConfig config = lambdaQuery()
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysStorageConfig::getIsDefault, 1)
                .one();
        Assert.notNull(config, "未配置默认存储，请先在存储配置中设为默认");
        if (StorageConfigStatusEnum.DISABLED.getCode().equals(config.getStatus())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "默认上传配置已被停用");
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        // 1. 校验目标配置存在
        findById(id);
        // 2. 清掉所有默认标记
        lambdaUpdate()
                .set(SysStorageConfig::getIsDefault, 0)
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .update();
        // 3. 仅当前配置标记为默认
        lambdaUpdate()
                .set(SysStorageConfig::getIsDefault, 1)
                .eq(SysStorageConfig::getId, id)
                .update();
    }

    @Override
    public void paging(Paging<SysStorageConfig> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        lambdaQuery()
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysStorageConfig::getStatus, status)
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysStorageConfig::getConfigName, keyword)
                .orderByDesc(SysStorageConfig::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysStorageConfig> paging, String keyword) {
        lambdaQuery()
                .select(SysStorageConfig::getId, SysStorageConfig::getConfigName)
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysStorageConfig::getStatus, StorageConfigStatusEnum.ENABLED.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysStorageConfig::getConfigName, keyword)
            .orderByDesc(SysStorageConfig::getId)
            .page(paging);
    }

    @Override
    public Map<Long, String> mapConfigNameByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return lambdaQuery()
                .select(SysStorageConfig::getId, SysStorageConfig::getConfigName)
                .in(SysStorageConfig::getId, ids)
                .eq(SysStorageConfig::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .list()
                .stream()
                .collect(Collectors.toMap(SysStorageConfig::getId, SysStorageConfig::getConfigName, (a, b) -> a));
    }
}
