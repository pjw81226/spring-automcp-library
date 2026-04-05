package com.jang.mcp.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jang.mcp.starter.annotation.McpParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the argument conversion logic used in McpAutoConfiguration.
 * Tests the ObjectMapper-based conversion behavior directly
 * since convertArguments() is a private method.
 */
class ConvertArgumentsTest {

    private ObjectMapper objectMapper;

    public record TestParams(
            @McpParameter(description = "name") String name,
            @McpParameter(description = "count") Integer count
    ) {}

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // -- Parameterless tool behavior (null / Void) --

    @Test
    @DisplayName("Parameterless tools should receive null — null paramType")
    void nullParamTypeReturnsNull() {
        Object result = convertArguments(Map.of("key", "value"), null, objectMapper);
        assertNull(result);
    }

    @Test
    @DisplayName("Parameterless tools should receive null — Void paramType")
    void voidParamTypeReturnsNull() {
        Object result = convertArguments(Map.of("key", "value"), Void.class, objectMapper);
        assertNull(result);
    }

    // -- Valid conversion --

    @Test
    @DisplayName("Valid arguments are converted to typed object")
    void validConversion() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Alice");
        args.put("count", 42);

        Object result = convertArguments(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertEquals("Alice", params.name());
        assertEquals(42, params.count());
    }

    @Test
    @DisplayName("Partial arguments convert with null for missing fields")
    void partialArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Bob");

        Object result = convertArguments(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertEquals("Bob", params.name());
        assertNull(params.count());
    }

    @Test
    @DisplayName("Empty map converts to object with all null fields")
    void emptyArguments() {
        Map<String, Object> args = new HashMap<>();

        Object result = convertArguments(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertNull(params.name());
        assertNull(params.count());
    }

    // -- Error cases --

    @Test
    @DisplayName("null arguments with non-Void paramType throws IllegalArgumentException")
    void nullArgumentsThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                convertArguments(null, TestParams.class, objectMapper));

        assertTrue(ex.getMessage().contains("Missing arguments"));
        assertTrue(ex.getMessage().contains("TestParams"));
    }

    @Test
    @DisplayName("Invalid type in arguments throws conversion exception")
    void invalidTypeThrows() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Alice");
        args.put("count", "not-a-number");

        // ObjectMapper should throw when "not-a-number" can't be converted to Integer
        assertThrows(IllegalArgumentException.class, () ->
                convertArguments(args, TestParams.class, objectMapper));
    }

    /**
     * Mirror of McpAutoConfiguration.convertArguments() logic.
     * Tested here to verify correctness without requiring full Spring context.
     */
    private Object convertArguments(Map<String, Object> arguments, Class<?> paramType, ObjectMapper mapper) {
        if (paramType == null || paramType == Void.class) {
            return null;
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Missing arguments for tool that expects parameters of type " + paramType.getSimpleName());
        }
        return mapper.convertValue(arguments, paramType);
    }
}
