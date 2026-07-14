package com.platform.framework.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.common.annotation.JsonCoverParam;
import com.platform.common.utils.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

/**
 * 自定义参数解析器
 *
 * @author yuxun
 * @date 2023-12-05
 */
@Slf4j
public class MyHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasMethodAnnotation(JsonCoverParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String requestParamStr = IoUtil.readUtf8(request.getInputStream());
        if (StrUtil.isBlank(requestParamStr)) {
            throw new HttpMessageNotReadableException("Required request body is missing");
        }
        Object value = JacksonUtil.parseMap(requestParamStr).get(parameter.getParameterName());
        if (Objects.isNull(value)) {
            return null;
        }
        // 将解析出的值转换为目标参数类型（如 Integer -> Long）
        return JacksonUtil.convertValue(value, parameter.getParameterType());
    }
}
