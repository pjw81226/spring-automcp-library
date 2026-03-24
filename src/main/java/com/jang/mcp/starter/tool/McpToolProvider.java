package com.jang.mcp.starter.tool;

import java.util.Map;

/**
 * MCP 도구를 정의하기 위한 공통 인터페이스.
 * 이 인터페이스를 구현한 스프링 빈은 자동으로 MCP 서버에 등록된다.
 */
public interface McpToolProvider {

    /**
     * 도구의 고유 이름. MCP 클라이언트가 이 이름으로 도구를 호출한다.
     */
    String getName();

    /**
     * 도구에 대한 설명. LLM이 이 설명을 보고 도구의 용도를 판단한다.
     */
    String getDescription();

    /**
     * 도구의 파라미터를 정의하는 Record 클래스를 반환한다.
     * 파라미터가 없으면 null을 반환할 수 있다.
     * Record의 필드에 @McpParameter를 적용하여 설명을 추가한다.
     */
    Class<?> getParameterType();

    /**
     * 도구를 실행한다.
     *
     * @param arguments MCP 클라이언트에서 전달된 인자 맵
     * @return 실행 결과 문자열 (텍스트 형태로 LLM에 전달됨)
     */
    String execute(Map<String, Object> arguments);
}
