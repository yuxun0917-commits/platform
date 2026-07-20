package com.platform.admin.config.aspect;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.platform.common.annotation.IgnoreLog;
import com.platform.common.context.SecurityUser;
import com.platform.common.entity.admin.SysLog;
import com.platform.common.enums.OperLogStatusEnum;
import com.platform.common.utils.JacksonUtil;
import com.platform.framework.manager.AsyncManager;
import com.platform.service.service.SysLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Controller 层统一日志切面
 *
 * <p>整合控制台日志打印、敏感字段脱敏、操作日志入库三大功能。</p>
 *
 * <p>拦截所有 Controller 方法，仅对 POST 请求记录操作日志：</p>
 * <ol>
 *   <li>控制台打印请求入参和响应出参（脱敏后）</li>
 *   <li>采集请求信息（URL、方法、参数、操作人、IP、浏览器等）</li>
 *   <li>异步写入 sys_log 表</li>
 * </ol>
 *
 * <p>脱敏字段：密码、手机号、身份证、银行卡、邮箱等。</p>
 *
 * @author platform
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /** 需要脱敏的字段名（小写匹配） */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "pwd", "oldpassword", "newpassword",
            "mobile", "phone", "telephone",
            "idcard", "idcardno",
            "bankcard", "cardno",
            "email"
    );

    /** 手机号脱敏正则：保留前3后4 */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");

    /** 邮箱脱敏正则：保留首字符与域名 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1})\\w*@(\\w+\\.\\w+)");

    private final SysLogService sysLogService;
    private final AsyncManager asyncManager;

    /**
     * 切点：所有 Controller 包下的方法
     */
    @Pointcut("execution(* com.platform..controller..*.*(..))")
    public void pointcut() {
    }

    /**
     * 环绕通知：仅对 POST 请求记录请求与响应日志并异步入库；其余方法直接放行
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果类或方法标注了 @IgnoreLog，直接放行不记录日志
        if (isIgnoreLog(joinPoint)) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = getRequest();
        String httpMethod = Objects.nonNull(request) ? request.getMethod() : "UNKNOWN";
        String uri = Objects.nonNull(request) ? request.getRequestURI() : "UNKNOWN";

        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 1. 控制台打印请求入参（脱敏）
        String consoleArgs = desensitizeArgs(joinPoint.getArgs());
        log.info("[请求入参] {} {} | 方法={} | 入参={}", httpMethod, uri, signature.toShortString(), consoleArgs);

        // 2. 采集操作日志信息
        SysLog sysLog = new SysLog()
                .setTitle(resolveTitle(joinPoint))
                .setMethod(signature.getDeclaringTypeName() + "." + signature.getName())
                .setRequestUrl(uri)
                .setRequestMethod(httpMethod)
                .setRequestParam(truncateArgs(joinPoint.getArgs()))
                .setOperId(SecurityUser.getUserIdAsLongOrNull())
                .setOperName(SecurityUser.getNicknameAsNull())
                .setOperIp(Objects.nonNull(request) ? JakartaServletUtil.getClientIP(request) : "")
                .setBrowser(Objects.nonNull(request) ? getBrowser(request) : "")
                .setOs(Objects.nonNull(request) ? getOs(request) : "");

        // 3. 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            sysLog.setStatus(OperLogStatusEnum.NORMAL.getCode());
            sysLog.setResponseData(truncate(JacksonUtil.toJsonString(result), 2000));
            // 控制台打印响应出参（脱敏）
            long cost = System.currentTimeMillis() - startTime;
            log.info("[响应出参] {} {} | 耗时={}ms | 出参={}", httpMethod, uri, cost, desensitize(result));
        } catch (Throwable e) {
            sysLog.setStatus(OperLogStatusEnum.ABNORMAL.getCode());
            sysLog.setErrorMsg(truncate(e.getMessage(), 2000));
            throw e;
        } finally {
            sysLog.setCostTime(System.currentTimeMillis() - startTime);
            sysLog.setOperTime(LocalDateTime.now());
            // 4. 异步写入数据库
            saveAsync(sysLog);
        }
        return result;
    }

    // ============================ 日志忽略判断 ============================

    /**
     * 判断是否忽略日志记录
     *
     * <p>方法级优先级高于类级。</p>
     *
     * @param joinPoint 连接点
     * @return true 忽略日志 / false 记录日志
     */
    private boolean isIgnoreLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (signature.getMethod().isAnnotationPresent(IgnoreLog.class)) {
            return true;
        }
        return joinPoint.getTarget().getClass().isAnnotationPresent(IgnoreLog.class);
    }

    // ============================ 模块标题解析 ============================

    /**
     * 解析模块标题
     *
     * <p>从类上的 @Tag 注解取，兜底用类名</p>
     */
    private String resolveTitle(ProceedingJoinPoint joinPoint) {
        Tag tag = joinPoint.getTarget().getClass().getAnnotation(Tag.class);
        if (Objects.nonNull(tag) && !tag.name().isEmpty()) {
            return tag.name();
        }
        return joinPoint.getTarget().getClass().getSimpleName();
    }

    // ============================ 数据库写入 ============================

    /**
     * 异步保存操作日志
     */
    private void saveAsync(SysLog sysLog) {
        asyncManager.execute(() -> {
            try {
                sysLogService.save(sysLog);
            } catch (Exception e) {
                log.error("[OperLog] 操作日志保存失败: {}", e.getMessage());
            }
        });
    }

    // ============================ 请求工具 ============================

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.isNull(attributes) ? null : attributes.getRequest();
    }

    /**
     * 序列化请求参数入库（排除 MultipartFile，截断防超长）
     */
    private String truncateArgs(Object[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (isSkippableArg(arg)) {
                sb.append("[skip],");
            } else {
                try {
                    sb.append(JacksonUtil.toJsonString(arg)).append(",");
                } catch (Exception e) {
                    sb.append(arg.toString()).append(",");
                }
            }
        }
        return truncate(sb.toString(), 2000);
    }

    /**
     * 不可序列化/不应入库的参数（避免 Jackson 反射调用 getter 副作用，
     * 例如序列化 HttpServletResponse 会触发 getWriter() 占用响应流，导致流式下载接口崩溃）
     */
    private boolean isSkippableArg(Object arg) {
        return Objects.isNull(arg)
                || arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof HttpSession
                || arg instanceof Principal
                || arg instanceof InputStream
                || arg instanceof OutputStream
                || arg instanceof MultipartFile
                || arg instanceof byte[];
    }

    /**
     * 截断字符串，防止超长字段
     */
    private String truncate(String str, int maxLen) {
        if (Objects.isNull(str)) {
            return "";
        }
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }

    // ============================ 脱敏处理 ============================

    /**
     * 序列化参数并脱敏（用于控制台打印）
     */
    private String desensitizeArgs(Object[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Object arg : args) {
            sb.append(desensitize(arg)).append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 对象序列化并脱敏
     */
    private String desensitize(Object obj) {
        if (isSkippableArg(obj)) {
            return "[skip]";
        }
        try {
            String json = JacksonUtil.toJsonString(obj);
            return desensitizeJson(json);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * JSON 字符串脱敏
     */
    private String desensitizeJson(String json) {
        String result = json;
        for (String field : SENSITIVE_FIELDS) {
            result = result.replaceAll(
                    "(?i)(\"(" + field + ")\"\\s*:\\s*\")([^\"]*)(\")",
                    "$1******$4");
        }
        result = MOBILE_PATTERN.matcher(result).replaceAll("$1****$2");
        result = EMAIL_PATTERN.matcher(result).replaceAll("$1****@$2");
        return result;
    }

    // ============================ UA 解析 ============================

    /**
     * 从 User-Agent 解析浏览器类型
     */
    private String getBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (Objects.isNull(ua)) {
            return "";
        }
        if (ua.contains("Edg")) {
            return "Edge";
        } else if (ua.contains("Chrome")) {
            return "Chrome";
        } else if (ua.contains("Firefox")) {
            return "Firefox";
        } else if (ua.contains("Safari")) {
            return "Safari";
        } else if (ua.contains("MSIE") || ua.contains("Trident")) {
            return "IE";
        }
        return "Unknown";
    }

    /**
     * 从 User-Agent 解析操作系统
     */
    private String getOs(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (Objects.isNull(ua)) {
            return "";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        } else if (ua.contains("Mac")) {
            return "macOS";
        } else if (ua.contains("Linux")) {
            return "Linux";
        } else if (ua.contains("Android")) {
            return "Android";
        } else if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        return "Unknown";
    }
}
