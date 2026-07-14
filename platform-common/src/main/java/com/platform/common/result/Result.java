package com.platform.common.result;

import com.platform.common.enums.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装类
 *
 * <p>所有接口统一返回 {@code Result<T>} 格式，包含 code、msg、data 三个字段。</p>
 *
 * @param <T> 返回数据泛型
 * @author platform
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回（自定义提示 + 数据）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回（根据错误码）
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    /**
     * 失败返回（自定义错误码 + 提示）
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回（错误码 + 自定义提示）
     */
    public static <T> Result<T> fail(ErrorCode errorCode, String msg) {
        return new Result<>(errorCode.getCode(), msg, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ErrorCode.SUCCESS.getCode().equals(this.code);
    }
}
