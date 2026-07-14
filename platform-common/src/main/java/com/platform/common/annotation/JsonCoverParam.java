package com.platform.common.annotation;

import java.lang.annotation.*;

/**
 * json方法参数
 *
 * @author yuxun
 * @date 2023-12-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonCoverParam {
}
