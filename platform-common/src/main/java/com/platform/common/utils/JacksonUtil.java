package com.platform.common.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.platform.common.exception.BusinessException;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

/**
 * jackson 工具类
 *
 * @author yuxun
 * @date 2023-12-04
 */
public class JacksonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 默认不序列化空字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 转javaBean忽略不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JSR310Module());
        // 序列化对象驼峰转下划线
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * 文本 转 javaBean
     *
     * @param text      要解析的文本
     * @param clazz     解析成最终的对象
     * @return T
     */
    public static <T> T parseObj(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(StrUtil.format("json转对象异常：【{}】", text));
        }
    }

    /**
     * 文本 转 泛型对象（支持 List、Map 等复杂泛型类型）
     *
     * @param text          要解析的文本
     * @param typeReference 泛型类型引用（如 new TypeReference&lt;List&lt;DeptTreeVO&gt;&gt;(){}）
     * @return T
     */
    public static <T> T parseTypeRef(String text, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (JsonProcessingException e) {
            throw new BusinessException(StrUtil.format("json转对象异常：【{}】", text));
        }
    }

    /**
     * 文本 转 数组
     *
     * @param text
     * @param clazz
     * @return java.util.Collection<T>
     */
    public static <T> Collection<T> parseArrays(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text, new TypeReference<Collection<T>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(StrUtil.format("json转数组异常：【{}】", text));
        }
    }

    /**
     * 实体 转 json
     *
     * @param t
     * @return java.lang.String
     */
    public static <T> String toJsonString(T t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new BusinessException(StrUtil.format("实体转json异常：【{}】", t));
        }
    }

    /**
     * 文本 转 map
     *
     * @param text
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public static Map<String, Object> parseMap(String text) {
        try {
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(StrUtil.format("json转Map异常：【{}】", text));
        }
    }

    /**
     * json 转 对象树
     *
     * @param text
     * @return com.fasterxml.jackson.databind.JsonNode
     */
    public static JsonNode parseNode(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            throw new BusinessException(StrUtil.format("解析json异常：【{}】", text));
        }
    }

    /**
     * 将对象转换为目标类型
     *
     * <p>用于解决 JSON 解析后类型不匹配问题（如 Integer 转 Long）。</p>
     *
     * @param value     原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    public static <T> T convertValue(Object value, Class<T> targetType) {
        return objectMapper.convertValue(value, targetType);
    }
}
