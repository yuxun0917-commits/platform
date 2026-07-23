package com.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 *
 * <p>错误码规则：5位数字，前2位表示模块，后3位表示具体错误。
 * 例如：10xxx 表示通用模块，20xxx 表示用户模块。</p>
 *
 * @author platform
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 通用失败 */
    FAIL(500, "操作失败"),

    /** 通用模块 10xxx */
    PARAM_ERROR(10001, "参数校验失败"),
    PARAM_MISSING(10002, "缺少必要参数"),
    UNAUTHORIZED(10003, "未登录或登录已过期"),
    FORBIDDEN(10004, "无权限访问"),
    NOT_FOUND(10005, "资源不存在"),
    METHOD_NOT_ALLOWED(10006, "请求方式不支持"),
    SYSTEM_ERROR(10007, "系统内部错误"),
    TOO_MANY_REQUESTS(10008, "请求过于频繁，请稍后再试"),
    SECOND_AUTH(10009, "需要二次认证"),

    /** 用户模块 20xxx */
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_NAME_EXIST(20002, "用户名已存在"),
    USER_PASSWORD_ERROR(20003, "密码错误"),
    USER_DISABLED(20004, "用户已被禁用"),
    USER_EMAIL_EXIST(20005, "邮箱已存在"),
    NEED_CHANGE_PWD(20006, "请先修改密码后再继续操作"),


    /** 消息模块 30xxx */
    MQ_SEND_FAIL(30001, "消息发送失败"),
    MQ_CONSUME_FAIL(30002, "消息消费失败"),

    /** 文件模块 40xxx */
    FILE_UPLOAD_FAIL(40001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(40002, "文件类型不允许"),
    FILE_SIZE_EXCEED(40003, "文件大小超出限制"),

    /** 字典模块 50xxx */
    DICT_NOT_EXIST(50001, "字典类型不存在"),
    DICT_TYPE_EXIST(50002, "字典类型已存在"),
    DICT_ITEM_NOT_EXIST(50003, "字典项不存在"),
    DICT_HAS_ITEMS(50004, "该字典类型下存在字典项，无法删除"),

    /** 岗位模块 60xxx */
    POST_NOT_EXIST(60001, "岗位不存在"),
    POST_CODE_EXIST(60002, "岗位编码已存在"),
    POST_HAS_USERS(60003, "该岗位下存在用户，无法删除"),

    /** 通知模块 70xxx */
    NOTICE_NOT_EXIST(70001, "通知公告不存在"),

    /** 验证码模块 80xxx */
    CAPTCHA_GENERATE_FAIL(80001, "验证码生成失败"),
    CAPTCHA_EXPIRED(80002, "验证码已过期，请重新获取"),
    CAPTCHA_ERROR(80003, "验证码错误"),
    CAPTCHA_REQUIRED(80004, "请输入验证码");

    /** 错误码 */
    private final Integer code;

    /** 错误信息 */
    private final String msg;
}
