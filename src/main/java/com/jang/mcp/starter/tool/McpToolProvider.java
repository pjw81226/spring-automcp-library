package com.jang.mcp.starter.tool;

import java.util.Map;

/**
 * Common interface for defining MCP tools.
 * Spring beans implementing this interface are automatically registered to the MCP server.
 *
 * MCP 도구를 정의하기 위한 공통 인터페이스.
 * 이 인터페이스를 구현한 스프링 빈은 자동으로 MCP 서버에 등록된다.
 */
public interface McpToolProvider {

    /**
     * Unique name of the tool. MCP clients invoke the tool using this name.
     * 
     * 도구의 고유 이름. MCP 클라이언트가 이 이름으로 도구를 호출한다.
     */
    String getName();

    /**
     * Description of the tool. The LLM reads this to determine the tool's purpose.
     * 
     * 도구에 대한 설명. LLM이 이 설명을 보고 도구의 용도를 판단한다.
     */
    String getDescription();

    /**
     * Returns the Record class that defines the tool's parameters.
     * May return null if the tool has no parameters.
     * Apply @McpParameter to the Record's fields to add descriptions.
     * 
     * 도구의 파라미터를 정의하는 Record 클래스를 반환한다.
     * 파라미터가 없으면 null을 반환할 수 있다.
     * Record의 필드에 @McpParameter를 적용하여 설명을 추가한다.
     */
    Class<?> getParameterType();

    /**
     * Executes the tool.
     *
     * @param arguments argument map received from the MCP client
     * @return result string delivered to the LLM as text
     */
    String execute(Map<String, Object> arguments);
}
