package com.jang.mcp.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applied to Record fields to define MCP tool parameter descriptions.
 * McpSchemaGenerator reads this annotation and converts it into JSON Schema description.
 *
 * Record 필드에 적용하여 MCP 도구 파라미터의 설명을 정의하는 어노테이션.
 * McpSchemaGenerator가 이 어노테이션을 읽어 JSON Schema의 description으로 변환한다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpParameter {

    /**
     * Description of the parameter. The LLM uses this to determine what value to provide.
     * 
     * 파라미터에 대한 설명. LLM이 이 설명을 보고 어떤 값을 넣어야 하는지 판단한다.
     */
    String description();

    /**
     * Whether this parameter is required. Defaults to true.
     * 
     * 필수 파라미터 여부. 기본값은 true.
     */
    boolean required() default true;
}
