package com.platform.component.job;

import com.platform.common.entity.admin.SysJob;
import com.platform.common.utils.Assert;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务方法反射调用工具
 *
 * <p>解析 {@code invokeTarget}（如 {@code ryTask.ryParams('ry')} 或 {@code ryTask.ryNoParams}），
 * 通过 {@link SpringUtil} 获取目标 Spring Bean 并反射执行对应方法。</p>
 *
 * @author platform
 */
@Slf4j
public class JobInvokeUtil {

    private JobInvokeUtil() {
    }

    /**
     * 反射调用任务目标方法
     *
     * @param sysJob 任务实体（含 invokeTarget）
     * @throws Exception 解析或反射调用异常
     */
    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        Assert.notBlank(invokeTarget, "调用目标字符串为空");

        // 拆分为 beanName 与方法部分
        int dotIndex = invokeTarget.indexOf('.');
        Assert.isTrue(dotIndex > 0, "调用目标格式错误：{}", invokeTarget);
        String beanName = invokeTarget.substring(0, dotIndex).trim();
        String methodPart = invokeTarget.substring(dotIndex + 1).trim();

        int left = methodPart.indexOf('(');
        int right = methodPart.lastIndexOf(')');
        Assert.isTrue(left > 0 && right > left, "调用目标方法格式错误：{}", invokeTarget);
        String methodName = methodPart.substring(0, left).trim();
        String argsStr = methodPart.substring(left + 1, right).trim();

        Object bean = SpringUtil.getBean(beanName);
        Assert.notNull(bean, "未找到任务Bean：{}", beanName);

        List<Object> args = parseArgs(argsStr);
        Method method = findMethod(bean.getClass(), methodName, args);
        Assert.notNull(method, "未找到可执行方法：{}", methodName);
        method.invoke(bean, args.toArray());
    }

    /**
     * 在 Bean 的所有方法中查找名称与参数数量匹配、且参数类型兼容的方法。
     * 兼容基本类型与包装类型（如 int 与 Integer）。
     */
    private static Method findMethod(Class<?> beanClass, String methodName, List<Object> args) {
        for (Method method : beanClass.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != args.size()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (!isCompatible(paramTypes[i], args.get(i))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return method;
            }
        }
        return null;
    }

    /**
     * 判断方法形参类型与实参是否兼容（含基本类型与包装类型）
     */
    private static boolean isCompatible(Class<?> paramType, Object arg) {
        if (paramType.isPrimitive()) {
            if (paramType == int.class) return arg instanceof Integer;
            if (paramType == long.class) return arg instanceof Long;
            if (paramType == double.class) return arg instanceof Double;
            if (paramType == float.class) return arg instanceof Float || arg instanceof Double;
            if (paramType == boolean.class) return arg instanceof Boolean;
            if (paramType == short.class) return arg instanceof Short || arg instanceof Integer;
            if (paramType == byte.class) return arg instanceof Byte || arg instanceof Integer;
            if (paramType == char.class) return arg instanceof Character;
            return false;
        }
        return paramType.isAssignableFrom(arg.getClass());
    }

    /**
     * 解析参数列表
     *
     * <p>支持单引号/双引号字符串、布尔、整数、长整、小数；无参时返回空列表。</p>
     */
    private static List<Object> parseArgs(String argsStr) {
        List<Object> args = new ArrayList<>();
        if (argsStr == null || argsStr.isBlank()) {
            return args;
        }
        String[] raw = argsStr.split(",");
        for (String s : raw) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // 去外层引号 -> 字符串
            if ((trimmed.startsWith("'") && trimmed.endsWith("'"))
                    || (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
                args.add(trimmed.substring(1, trimmed.length() - 1));
                continue;
            }
            // 布尔
            if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                args.add(Boolean.parseBoolean(trimmed));
                continue;
            }
            // 数字
            try {
                if (trimmed.contains(".")) {
                    args.add(Double.parseDouble(trimmed));
                } else {
                    long l = Long.parseLong(trimmed);
                    args.add(l <= Integer.MAX_VALUE ? (int) l : l);
                }
                continue;
            } catch (NumberFormatException ignored) {
                // 非数字，按原字符串处理
            }
            args.add(trimmed);
        }
        return args;
    }
}
