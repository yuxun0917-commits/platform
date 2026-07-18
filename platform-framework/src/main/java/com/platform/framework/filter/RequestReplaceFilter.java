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
        String contentType = request.getContentType();
        // multipart 上传请求不能包装/提前读取 body，否则 MyServletRequestWrapper 会把流读光，
        // 导致后续 StandardServletMultipartResolver 解析时 Stream closed。必须在包装之前就放行。
        if (Objects.nonNull(contentType) && contentType.toLowerCase().startsWith(ContentType.MULTIPART.getValue())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!(request instanceof MyServletRequestWrapper)) {
            request = new MyServletRequestWrapper(request);
        }
        filterChain.doFilter(request, response);
    }

}