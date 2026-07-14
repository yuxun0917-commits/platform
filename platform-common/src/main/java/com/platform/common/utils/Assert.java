package com.platform.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.common.exception.BusinessException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 断言
 *
 * @author yuxun
 * @date 2023-12-01
 */
public class Assert {

    public Assert() {
    }

    public static void isTrue(boolean expression, Supplier<? extends BusinessException> supplier) throws BusinessException {
        if (!expression) {
            throw supplier.get();
        }
    }

    public static void isTrue(boolean expression, String errorMsgTemplate, Object... params) throws BusinessException {
        isTrue(expression, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static void isTrue(boolean expression) throws BusinessException {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    public static void isFalse(boolean expression, Supplier<BusinessException> errorSupplier) throws BusinessException {
        if (expression) {
            throw errorSupplier.get();
        }
    }

    public static void isFalse(boolean expression, String errorMsgTemplate, Object... params) throws BusinessException {
        isFalse(expression, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static void isFalse(boolean expression) throws BusinessException {
        isFalse(expression, "[Assertion failed] - this expression must be false");
    }

    public static void isNull(Object object, Supplier<BusinessException> errorSupplier) throws BusinessException {
        if (null != object) {
            throw errorSupplier.get();
        }
    }

    public static void isNull(Object object, String errorMsgTemplate, Object... params) throws BusinessException {
        isNull(object, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static void isNull(Object object) throws BusinessException {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    public static <T, BusinessException extends Throwable> T notNull(T object, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (null == object) {
            throw errorSupplier.get();
        } else {
            return object;
        }
    }

    public static <T> T notNull(T object, String errorMsgTemplate, Object... params) throws BusinessException {
        return notNull(object, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <T> T notNull(T object) throws BusinessException {
        return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static <T extends CharSequence, BusinessException extends Throwable> T notEmpty(T text, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (StrUtil.isEmpty(text)) {
            throw errorSupplier.get();
        } else {
            return text;
        }
    }

    public static <T extends CharSequence> T notEmpty(T text, String errorMsgTemplate, Object... params) throws BusinessException {
        return notEmpty(text, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <T extends CharSequence> T notEmpty(T text) throws BusinessException {
        return notEmpty(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    public static <T extends CharSequence, BusinessException extends Throwable> T notBlank(T text, Supplier<com.platform.common.exception.BusinessException> errorMsgSupplier) throws com.platform.common.exception.BusinessException {
        if (StrUtil.isBlank(text)) {
            throw errorMsgSupplier.get();
        } else {
            return text;
        }
    }

    public static <T extends CharSequence> T notBlank(T text, String errorMsgTemplate, Object... params) throws BusinessException {
        return notBlank(text, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <T extends CharSequence> T notBlank(T text) throws BusinessException {
        return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    public static <T extends CharSequence, BusinessException extends Throwable> T notContain(CharSequence textToSearch, T substring, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (StrUtil.contains(textToSearch, substring)) {
            throw errorSupplier.get();
        } else {
            return substring;
        }
    }

    public static String notContain(String textToSearch, String substring, String errorMsgTemplate, Object... params) throws BusinessException {
        return (String)notContain(textToSearch, substring, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static String notContain(String textToSearch, String substring) throws BusinessException {
        return notContain(textToSearch, substring, "[Assertion failed] - this String argument must not contain the substring [{}]", substring);
    }

    public static <T, BusinessException extends Throwable> T[] notEmpty(T[] array, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (ArrayUtil.isEmpty(array)) {
            throw errorSupplier.get();
        } else {
            return array;
        }
    }

    public static <T> T[] notEmpty(T[] array, String errorMsgTemplate, Object... params) throws BusinessException {
        return notEmpty(array, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <T> T[] notEmpty(T[] array) throws BusinessException {
        return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    public static <T, BusinessException extends Throwable> T[] noNullElements(T[] array, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (ArrayUtil.hasNull(array)) {
            throw errorSupplier.get();
        } else {
            return array;
        }
    }

    public static <T> T[] noNullElements(T[] array, String errorMsgTemplate, Object... params) throws BusinessException {
        return noNullElements(array, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <T> T[] noNullElements(T[] array) throws BusinessException {
        return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    public static <E, T extends Iterable<E>, BusinessException extends Throwable> T notEmpty(T collection, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (CollUtil.isEmpty(collection)) {
            throw errorSupplier.get();
        } else {
            return collection;
        }
    }

    public static <E, T extends Iterable<E>> T notEmpty(T collection, String errorMsgTemplate, Object... params) throws BusinessException {
        return notEmpty(collection, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <E, T extends Iterable<E>> T notEmpty(T collection) throws BusinessException {
        return notEmpty(collection, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    public static <K, V, T extends Map<K, V>, BusinessException extends Throwable> T notEmpty(T map, Supplier<com.platform.common.exception.BusinessException> errorSupplier) throws com.platform.common.exception.BusinessException {
        if (MapUtil.isEmpty(map)) {
            throw errorSupplier.get();
        } else {
            return map;
        }
    }

    public static <K, V, T extends Map<K, V>> T notEmpty(T map, String errorMsgTemplate, Object... params) throws BusinessException {
        return notEmpty(map, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static <K, V, T extends Map<K, V>> T notEmpty(T map) throws BusinessException {
        return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }

    public static <T> T isInstanceOf(Class<?> type, T obj) {
        return isInstanceOf(type, obj, "Object [{}] is not instanceof [{}]", obj, type);
    }

    public static <T> T isInstanceOf(Class<?> type, T obj, String errorMsgTemplate, Object... params) throws BusinessException {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new BusinessException(StrUtil.format(errorMsgTemplate, params));
        } else {
            return obj;
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType) throws BusinessException {
        isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, String errorMsgTemplate, Object... params) throws BusinessException {
        notNull(superType, "Type to check against must not be null");
        if (Objects.isNull(subType) || !superType.isAssignableFrom(subType)) {
            throw new BusinessException(StrUtil.format(errorMsgTemplate, params));
        }
    }

    public static void state(boolean expression, Supplier<String> errorMsgSupplier) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException((String)errorMsgSupplier.get());
        }
    }

    public static void state(boolean expression, String errorMsgTemplate, Object... params) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(StrUtil.format(errorMsgTemplate, params));
        }
    }

    public static void state(boolean expression) throws IllegalStateException {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    public static int checkIndex(int index, int size) throws BusinessException, IndexOutOfBoundsException {
        return checkIndex(index, size, "[Assertion failed]");
    }

    public static int checkIndex(int index, int size, String errorMsgTemplate, Object... params) throws BusinessException, IndexOutOfBoundsException {
        if (index >= 0 && index < size) {
            return index;
        } else {
            throw new IndexOutOfBoundsException(badIndexMsg(index, size, errorMsgTemplate, params));
        }
    }

    public static int checkBetween(int value, int min, int max, Supplier<? extends BusinessException> errorSupplier) throws BusinessException {
        if (value >= min && value <= max) {
            return value;
        } else {
            throw errorSupplier.get();
        }
    }

    public static int checkBetween(int value, int min, int max, String errorMsgTemplate, Object... params) {
        return checkBetween(value, min, max, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static int checkBetween(int value, int min, int max) {
        return checkBetween(value, min, max, "The value must be between {} and {}.", min, max);
    }

    public static long checkBetween(long value, long min, long max, Supplier<? extends BusinessException> errorSupplier) throws BusinessException {
        if (value >= min && value <= max) {
            return value;
        } else {
            throw errorSupplier.get();
        }
    }

    public static long checkBetween(long value, long min, long max, String errorMsgTemplate, Object... params) {
        return checkBetween(value, min, max, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static long checkBetween(long value, long min, long max) {
        return checkBetween(value, min, max, "The value must be between {} and {}.", min, max);
    }

    public static double checkBetween(double value, double min, double max, Supplier<? extends BusinessException> errorSupplier) throws BusinessException {
        if (!(value < min) && !(value > max)) {
            return value;
        } else {
            throw errorSupplier.get();
        }
    }

    public static double checkBetween(double value, double min, double max, String errorMsgTemplate, Object... params) {
        return checkBetween(value, min, max, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static double checkBetween(double value, double min, double max) {
        return checkBetween(value, min, max, "The value must be between {} and {}.", min, max);
    }

    public static Number checkBetween(Number value, Number min, Number max) {
        notNull(value);
        notNull(min);
        notNull(max);
        double valueDouble = value.doubleValue();
        double minDouble = min.doubleValue();
        double maxDouble = max.doubleValue();
        if (!(valueDouble < minDouble) && !(valueDouble > maxDouble)) {
            return value;
        } else {
            throw new BusinessException(StrUtil.format("The value must be between {} and {}.", new Object[]{min, max}));
        }
    }

    public static void notEquals(Object obj1, Object obj2) {
        notEquals(obj1, obj2, "({}) must be not equals ({})", obj1, obj2);
    }

    public static void notEquals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) throws BusinessException {
        notEquals(obj1, obj2, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static void notEquals(Object obj1, Object obj2, Supplier<BusinessException> errorSupplier) throws BusinessException {
        if (ObjectUtil.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    public static void equals(Object obj1, Object obj2) {
        equals(obj1, obj2, "({}) must be equals ({})", obj1, obj2);
    }

    public static void equals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) throws BusinessException {
        equals(obj1, obj2, () -> {
            return new BusinessException(StrUtil.format(errorMsgTemplate, params));
        });
    }

    public static void equals(Object obj1, Object obj2, Supplier<BusinessException> errorSupplier) throws BusinessException {
        if (ObjectUtil.notEqual(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    private static String badIndexMsg(int index, int size, String desc, Object... params) {
        if (index < 0) {
            return StrUtil.format("{} ({}) must not be negative", new Object[]{StrUtil.format(desc, params), index});
        } else if (size < 0) {
            throw new BusinessException("negative size: " + size);
        } else {
            return StrUtil.format("{} ({}) must be less than size ({})", new Object[]{StrUtil.format(desc, params), index, size});
        }
    }
}
