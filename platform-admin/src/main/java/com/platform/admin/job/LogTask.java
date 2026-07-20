package com.platform.admin.job;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 日志清理定时任务
 *
 * <p>配合 sys_job 表预置数据，invoke_target 形如：
 * <ul>
 *   <li>{@code logTask.cleanOperateLog()} —— 每周清理操作日志（TRUNCATE sys_log）</li>
 *   <li>{@code logTask.cleanLoginLog()}  —— 每月清理登录日志（TRUNCATE sys_login_log）</li>
 * </ul>
 * 使用 MyBatis-Plus {@link SqlRunner} 直接截断表，简单高效。两表均无外键引用，MySQL 下 TRUNCATE 安全。</p>
 *
 * @author platform
 */
@Slf4j
@Component("logTask")
@RequiredArgsConstructor
public class LogTask {

    /**
     * 每周清理操作日志（对应 sys_job 中 cron：每周一凌晨 4 点）
     */
    public void cleanOperateLog() {
        truncate("sys_log", "操作日志");
    }

    /**
     * 每月清理登录日志（对应 sys_job 中 cron：每月 1 号凌晨 4 点）
     */
    public void cleanLoginLog() {
        truncate("sys_login_log", "登录日志");
    }

    /**
     * 直接截断指定表并记录结果
     *
     * <p>注意：TRUNCATE 是 DDL，JDBC 的 executeUpdate 返回受影响行数 0，
     * 而 MyBatis-Plus 的 {@code SqlRunner.db().update()} 经 {@code SqlHelper.retBool}
     * 判定（{@code result >= 1} 才为 true），因此该调用恒返回 false，
     * 不能据此判断成败。故改为截断后校验剩余行数来确认是否真正清空。</p>
     *
     * @param table 表名（硬编码，无外部输入，不存在注入风险）
     * @param desc  日志描述
     */
    private void truncate(String table, String desc) {
        try {
            SqlRunner.db().update("TRUNCATE TABLE " + table);
            long remain = SqlRunner.db().selectCount("SELECT COUNT(*) FROM " + table);
            if (remain == 0) {
                log.info("[LogTask] {}清理完成（已清空）", desc);
            } else {
                log.warn("[LogTask] {}清理后仍有 {} 条数据，请检查", desc, remain);
            }
        } catch (Exception e) {
            log.error("[LogTask] {}清理失败", desc, e);
        }
    }
}
