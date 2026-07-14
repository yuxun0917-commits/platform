package com.platform.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.common.entity.admin.SysJob;
import com.platform.common.result.Paging;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 定时任务表 服务类
 * </p>
 *
 * @author platform
 * @since 2026-07-13
 */
public interface SysJobService extends IService<SysJob> {

    /**
     * 根据ID查询任务（过滤逻辑删除）
     *
     * @param id 任务ID
     * @return 任务实体
     */
    SysJob findById(Long id);

    /**
     * 分页查询任务列表
     *
     * @param paging    分页对象
     * @param paramsMap 查询参数（keyword、status）
     */
    void paging(Paging<SysJob> paging, Map<String, Object> paramsMap);

    /**
     * 性能查询：仅返回 id 与任务名称
     *
     * @param paging  分页对象
     * @param keyword 模糊匹配关键词
     */
    void selectList(Paging<SysJob> paging, String keyword);

    /**
     * 查询所有正常状态且未删除的任务（供启动恢复使用）
     *
     * @return 正常运行的任务列表
     */
    List<SysJob> listNormalRunningJobs();
}
