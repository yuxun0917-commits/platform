package com.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略日志记录注解
 *
 * <p>标注在 Controller 类或方法上，用于跳过 {@code LogAspect} 的请求/响应日志打印和操作日志入库。</p>
 *
 * <p>使用规则：</p>
 * <ul>
 *   <li>标注在类上：该类下所有方法均不记录日志</li>
 *   <li>标注在方法上：仅该方法不记录日志</li>
 * </ul>
 *
 * @author platform
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreLog {
}
