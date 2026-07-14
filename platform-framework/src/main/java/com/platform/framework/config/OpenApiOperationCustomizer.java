package com.platform.framework.config;

import com.platform.common.annotation.JsonCoverParam;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SpringDoc OpenAPI 文档自定义配置
 *
 * <p>处理两个文档生成问题：</p>
 * <ol>
 *   <li>无注解参数（如 {@code Integer page}）默认标记为非必填</li>
 *   <li>{@code @JsonCoverParam} 注解的 POST 方法，将参数识别为 JSON 请求体</li>
 * </ol>
 *
 * 确保 springdoc 在构建分组文档时一定会调用。</p>
 *
 * @author platform
 */
@Configuration
public class OpenApiOperationCustomizer {

    /**
     * 注册全局 OperationCustomizer Bean
     *
     * <p>同时被 {@code GroupedOpenApi.addOperationCustomizer()} 显式引用，
     * 双重保障 customizer 一定会被 springdoc 调用。</p>
     *
     * @return OperationCustomizer 实例
     */
    @Bean
    public OperationCustomizer platformOperationCustomizer() {
        return new PlatformOperationCustomizer();
    }

    /**
     * 平台 OperationCustomizer 实现（命名类，避免匿名类导致的类型识别问题）
     */
    public static class PlatformOperationCustomizer implements OperationCustomizer {

        private static final Logger log = LoggerFactory.getLogger(PlatformOperationCustomizer.class);

        /**
         * 参数名发现器（HandlerMethod 的 MethodParameter 可能未初始化参数名）
         */
        private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

        private static final Map<Class<?>, String> TYPE_MAP = new HashMap<>();
        private static final Map<Class<?>, String> FORMAT_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(Integer.class, "integer");
            TYPE_MAP.put(int.class, "integer");
            TYPE_MAP.put(Long.class, "integer");
            TYPE_MAP.put(long.class, "integer");
            TYPE_MAP.put(Double.class, "number");
            TYPE_MAP.put(double.class, "number");
            TYPE_MAP.put(Float.class, "number");
            TYPE_MAP.put(float.class, "number");
            TYPE_MAP.put(Boolean.class, "boolean");
            TYPE_MAP.put(boolean.class, "boolean");
            TYPE_MAP.put(String.class, "string");
            TYPE_MAP.put(Character.class, "string");
            TYPE_MAP.put(char.class, "string");
            TYPE_MAP.put(BigDecimal.class, "number");
            TYPE_MAP.put(LocalDate.class, "string");
            TYPE_MAP.put(LocalDateTime.class, "string");
            TYPE_MAP.put(Date.class, "string");

            FORMAT_MAP.put(Integer.class, "int32");
            FORMAT_MAP.put(int.class, "int32");
            FORMAT_MAP.put(Long.class, "int64");
            FORMAT_MAP.put(long.class, "int64");
            FORMAT_MAP.put(Float.class, "float");
            FORMAT_MAP.put(float.class, "float");
            FORMAT_MAP.put(Double.class, "double");
            FORMAT_MAP.put(double.class, "double");
            FORMAT_MAP.put(LocalDate.class, "date");
            FORMAT_MAP.put(LocalDateTime.class, "date-time");
        }

        @Override
        public Operation customize(Operation operation, HandlerMethod handlerMethod) {
            String methodName = handlerMethod.getMethod().getName();
            // 初始化参数名发现（HandlerMethod 的 MethodParameter 默认未绑定 ParameterNameDiscoverer）
            for (MethodParameter mp : handlerMethod.getMethodParameters()) {
                mp.initParameterNameDiscovery(NAME_DISCOVERER);
            }

            // 1. 处理 @JsonCoverParam：将参数转为 JSON 请求体
            if (handlerMethod.hasMethodAnnotation(JsonCoverParam.class)) {
                log.info("[Swagger] @JsonCoverParam 处理: {}", methodName);
                customizeJsonCoverParam(operation, handlerMethod);
                return operation;
            }

            // 2. 处理无注解参数：标记为非必填
            customizeUnannotatedParams(operation, handlerMethod, methodName);

            return operation;
        }

        /**
         * 将 @JsonCoverParam 方法的参数转换为 JSON 请求体
         */
        private void customizeJsonCoverParam(Operation operation, HandlerMethod handlerMethod) {
            MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
            if (methodParameters.length == 0) {
                return;
            }

            Schema<?> bodySchema = new Schema<>();
            bodySchema.setType("object");

            List<String> requiredFields = new ArrayList<>();

            for (MethodParameter methodParam : methodParameters) {
                if (methodParam.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)
                        || methodParam.hasParameterAnnotation(PathVariable.class)) {
                    continue;
                }

                String paramName = methodParam.getParameterName();
                if (Objects.isNull(paramName)) {
                    paramName = "param" + methodParam.getParameterIndex();
                    log.warn("[Swagger] 参数名为null，使用回退名: {}", paramName);
                }
                Class<?> paramType = methodParam.getParameterType();

                Schema<?> propSchema = buildSchema(paramType);
                bodySchema.addProperties(paramName, propSchema);

                if (methodParam.hasParameterAnnotation(NotNull.class)) {
                    requiredFields.add(paramName);
                }
            }

            if (!requiredFields.isEmpty()) {
                bodySchema.setRequired(requiredFields);
            }

            MediaType mediaType = new MediaType();
            mediaType.setSchema(bodySchema);

            Content content = new Content();
            content.addMediaType("application/json", mediaType);

            RequestBody requestBody = new RequestBody();
            requestBody.setContent(content);
            requestBody.setRequired(true);

            operation.setRequestBody(requestBody);
            operation.setParameters(null);
        }

        /**
         * 无注解参数标记为非必填
         */
        private void customizeUnannotatedParams(Operation operation, HandlerMethod handlerMethod, String methodName) {
            List<Parameter> parameters = operation.getParameters();
            if (Objects.isNull(parameters) || parameters.isEmpty()) {
                return;
            }

            Set<String> unannotatedParamNames = new HashSet<>();
            for (MethodParameter methodParam : handlerMethod.getMethodParameters()) {
                boolean hasNoAnnotation = !methodParam.hasParameterAnnotation(RequestParam.class)
                        && !methodParam.hasParameterAnnotation(PathVariable.class)
                        && !methodParam.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class);
                if (hasNoAnnotation) {
                    String pName = methodParam.getParameterName();
                    if (Objects.isNull(pName)) {
                        pName = "param" + methodParam.getParameterIndex();
                    }
                    unannotatedParamNames.add(pName);
                }
            }

            if (!unannotatedParamNames.isEmpty()) {
                log.info("[Swagger] 无注解参数处理: {} | 参数={}", methodName, unannotatedParamNames);
            }

            for (Parameter param : parameters) {
                if (unannotatedParamNames.contains(param.getName())) {
                    log.info("[Swagger] 设置 {} required=false", param.getName());
                    param.setRequired(false);
                }
            }
        }

        /**
         * 根据Java类型构建OpenAPI Schema
         */
        private Schema<?> buildSchema(Class<?> type) {
            Schema<?> schema = new Schema<>();

            if (type.isEnum()) {
                schema.setType("string");
                Object[] constants = type.getEnumConstants();
                if (Objects.nonNull(constants)) {
                    List<Object> enumValues = new ArrayList<>();
                    for (Object constant : constants) {
                        enumValues.add(constant.toString());
                    }
                    ((Schema) schema).setEnum(enumValues);
                }
                return schema;
            }

            String schemaType = TYPE_MAP.get(type);
            if (Objects.nonNull(schemaType)) {
                schema.setType(schemaType);
                String format = FORMAT_MAP.get(type);
                if (Objects.nonNull(format)) {
                    schema.setFormat(format);
                }
                return schema;
            }

            return new StringSchema();
        }
    }
}
