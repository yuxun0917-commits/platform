package com.platform.framework.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一捕获各类异常并返回标准的 {@code Result} 格式。</p>
 *
 * @author platform
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[业务异常] uri={}, code={}, msg={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("[未登录] uri={}, type={}", request.getRequestURI(), e.getType());
        e.printStackTrace();
        return Result.fail(ErrorCode.UNAUTHORIZED);
    }

    /**
     * Sa-Token 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("[无权限] uri={}, code={}", request.getRequestURI(), e.getCode());
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    /**
     * Sa-Token 角色不符异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.warn("[角色不符] uri={}, role={}", request.getRequestURI(), e.getRole());
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    /**
     * 参数校验异常（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数校验失败] {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 参数校验异常（表单绑定）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数绑定失败] {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 参数校验异常（@Validated 标注在 Controller 上）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[约束校验失败] {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 缺少必要参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("[缺少必要参数] {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_MISSING, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 请求体不可读
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("[请求体解析失败] {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_ERROR, "请求体格式错误");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[请求方式不支持] {}", e.getMessage());
        return Result.fail(ErrorCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> NoResourceFoundException(NoResourceFoundException e) {
        log.warn("[请求路径不存在] {}", e.getMessage());
        return Result.fail(ErrorCode.NOT_FOUND);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] uri={}", request.getRequestURI(), e);
        return Result.fail(ErrorCode.SYSTEM_ERROR);
    }
}
