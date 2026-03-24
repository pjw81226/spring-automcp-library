package com.jang.mcp.starter.util;

import com.jang.mcp.starter.annotation.McpParameter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Record 클래스와 @McpParameter 어노테이션을 읽어 MCP JSON Schema로 변환하는 유틸리티.
 */
public final class McpSchemaGenerator {

    private McpSchemaGenerator() {
    }

    /**
     * Record 클래스를 JSON Schema 형태의 Map으로 변환한다.
     * MCP SDK의 McpSchema.JsonSchema 생성에 사용할 수 있는 형태를 반환한다.
     *
     * @param parameterType @McpParameter가 적용된 Record 클래스
     * @return JSON Schema를 표현하는 Map (type, properties, required)
     */
    public static Map<String, Object> generate(Class<?> parameterType) {
        if (parameterType == null) {
            return createEmptySchema();
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Field field : parameterType.getDeclaredFields()) {
            Map<String, Object> fieldSchema = new LinkedHashMap<>();
            fieldSchema.put("type", resolveJsonType(field.getType()));

            McpParameter annotation = field.getAnnotation(McpParameter.class);
            if (annotation != null) {
                fieldSchema.put("description", annotation.description());
                if (annotation.required()) {
                    required.add(field.getName());
                }
            }

            properties.put(field.getName(), fieldSchema);
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * Java 타입을 JSON Schema 타입 문자열로 변환한다.
     */
    private static String resolveJsonType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class) {
            return "integer";
        } else if (type == float.class || type == Float.class
                || type == double.class || type == Double.class) {
            return "number";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else if (type.isArray() || List.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "string"; // fallback
        }
    }

    private static Map<String, Object> createEmptySchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", new LinkedHashMap<>());
        return schema;
    }
}
