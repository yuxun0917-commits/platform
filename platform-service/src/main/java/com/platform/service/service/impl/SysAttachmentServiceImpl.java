package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysAttachment;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.utils.Assert;
import com.platform.common.result.Paging;
import com.platform.service.mapper.SysAttachmentMapper;
import com.platform.service.service.SysAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author platform
 * @since 2026-07-14
 */
@Slf4j
@Service
public class SysAttachmentServiceImpl extends ServiceImpl<SysAttachmentMapper, SysAttachment> implements SysAttachmentService {

    @Override
    public SysAttachment findById(Long id) {
        SysAttachment attachment = lambdaQuery()
                .eq(SysAttachment::getId, id)
                .eq(SysAttachment::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(attachment, "无效的附件id:{}", id);
        return attachment;
    }

    @Override
    public void paging(Paging<SysAttachment> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        String bizType = (String) paramsMap.get("bizType");
        Long configId = (Long) paramsMap.get("configId");
        lambdaQuery()
                .eq(SysAttachment::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(configId), SysAttachment::getConfigId, configId)
                .eq(Objects.nonNull(bizType) && !bizType.isBlank(), SysAttachment::getBizType, bizType)
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysAttachment::getFileName, keyword)
                .orderByDesc(SysAttachment::getId)
                .page(paging);
    }

    @Override
    public void removeAttachment(Long id) {
        boolean updated = lambdaUpdate()
                .set(SysAttachment::getIsDelete, DeleteStatusEnum.DELETED.getCode())
                .eq(SysAttachment::getId, id)
                .eq(SysAttachment::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .update();
        Assert.isTrue(updated, "附件不存在或已被删除:{}", id);
    }

    @Override
    public long countByConfigId(Long configId) {
        return lambdaQuery()
                .eq(SysAttachment::getConfigId, configId)
                .eq(SysAttachment::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .count();
    }
}
