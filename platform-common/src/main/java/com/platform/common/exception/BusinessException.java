package com.platform.common.exception;

import com.platform.common.enums.ErrorCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义业务异常
 *
 * <p>继承 {@link RuntimeException}，由 {@code GlobalExceptionHandler} 统一捕获
 * 并返回标准的 {@code Result} 格式。</p>
 *
 * @author platform
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.PARAM_ERROR.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
