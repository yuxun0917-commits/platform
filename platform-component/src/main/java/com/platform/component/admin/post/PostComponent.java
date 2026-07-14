package com.platform.component.admin.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * 岗位业务组合组件
 *
 * <p>封装岗位相关的跨模块组合操作，供 Controller 层调用。
 * 岗位为扁平结构、无缓存，当前仅提供事务包装能力（如批量排序）。</p>
 *
 * @author platform
 */
@Component
@RequiredArgsConstructor
public class PostComponent {

    /**
     * 在事务中运行
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T doSomethingInTransactional(Supplier<T> supplier) {
        return supplier.get();
    }
}
