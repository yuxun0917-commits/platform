package com.platform.framework.filter;

import cn.hutool.http.ContentType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 过滤器包装原有的request对象
 *
 * @author yuxun
 * @date 2023-12-05
 */
@Component
public class RequestReplaceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!(request instanceof MyServletRequestWrapper)) {
            request = new MyServletRequestWrapper(request);
        }
        //如果有文件上传的业务场景，需要用下面的代码进行处理，不然文件上传的流会有问题
        String contentType = request.getContentType();
        //如果contentType是空
        //或者contentType是多媒体的上传类型则忽略，不进行包装，直接return
        if (Objects.isNull(contentType)) {
            filterChain.doFilter(request, response);
            return;
        } else if(Objects.equals(contentType, ContentType.MULTIPART.getValue())){
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

}