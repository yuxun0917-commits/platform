package com.platform.service.service;

import com.platform.common.entity.admin.SysNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.result.Paging;

import java.util.Map;

/**
 * <p>
 * 通知公告表 服务类
 * </p>
 *
 * @author yuxun
 * @since 2026-07-09
 */
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 通过 id 查询未删除的通知公告
     *
     * @param id 通知id
     * @return 通知公告实体
     */
    SysNotice findById(Long id);

    /**
     * 分页查询通知公告
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status、noticeType、position 等）
     */
    void paging(Paging<SysNotice> paging, Map<String, Object> paramsMap);

    /**
     * 通知公告选择列表（性能查询）
     *
     * <p>仅查询 id、通知标题两个字段，只返回未删除的正常状态通知。
     * 用于下拉选择等性能敏感场景。支持按通知标题模糊匹配。</p>
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词（匹配通知标题），可为null
     */
    void selectList(Paging<SysNotice> paging, String keyword);

}
