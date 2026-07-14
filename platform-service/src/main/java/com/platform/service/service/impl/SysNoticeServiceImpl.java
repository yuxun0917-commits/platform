package com.platform.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.entity.admin.SysNotice;
import com.platform.common.enums.DeleteStatusEnum;
import com.platform.common.enums.SysNoticeStatusEnum;
import com.platform.common.result.Paging;
import com.platform.common.utils.Assert;
import com.platform.service.mapper.SysNoticeMapper;
import com.platform.service.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public SysNotice findById(Long id) {
        SysNotice notice = lambdaQuery()
                .eq(SysNotice::getId, id)
                .eq(SysNotice::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .one();
        Assert.notNull(notice, "无效的通知id:{}", id);
        return notice;
    }

    @Override
    public void paging(Paging<SysNotice> paging, Map<String, Object> paramsMap) {
        String keyword = (String) paramsMap.get("keyword");
        Integer status = (Integer) paramsMap.get("status");
        Integer position = (Integer) paramsMap.get("position");
        lambdaQuery()
                .eq(SysNotice::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(Objects.nonNull(status), SysNotice::getStatus, status)
                .eq(Objects.nonNull(position), SysNotice::getPosition, position)
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysNotice::getTitle, keyword)
                .orderByDesc(SysNotice::getId)
                .page(paging);
    }

    @Override
    public void selectList(Paging<SysNotice> paging, String keyword) {
        lambdaQuery()
                .select(SysNotice::getId, SysNotice::getTitle)
                .eq(SysNotice::getIsDelete, DeleteStatusEnum.NORMAL.getCode())
                .eq(SysNotice::getStatus, SysNoticeStatusEnum.NORMAL.getCode())
                .like(Objects.nonNull(keyword) && !keyword.isBlank(), SysNotice::getTitle, keyword)
                .orderByDesc(SysNotice::getId)
                .page(paging);
    }
}
