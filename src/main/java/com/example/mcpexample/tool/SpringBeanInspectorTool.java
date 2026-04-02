package com.example.mcpexample.tool;

import com.jang.mcp.starter.annotation.McpParameter;
import com.jang.mcp.starter.tool.McpToolProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Exposes the list of registered Spring beans so that the AI Agent
 * can understand the application architecture (controllers, services, repositories, etc.).
 */
@Component
public class SpringBeanInspectorTool implements McpToolProvider {

    private final ApplicationContext applicationContext;

    public SpringBeanInspectorTool(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public record Params(
        @McpParameter(
            description = "Filter beans by layer: 'controller', 'service', 'repository', 'component', or 'all'. Default is 'all'.",
            required = false
        )
        String layer
    ) {}

    @Override
    public String getName() {
        return "get_spring_beans";
    }

    @Override
    public String getDescription() {
        return "Lists registered Spring beans grouped by layer (Controller, Service, Repository, Component). " +
               "Helps understand the application architecture and available components.";
    }

    @Override
    public Class<?> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        String layer = (String) arguments.getOrDefault("layer", "all");
        if (layer == null) layer = "all";

        Map<String, List<String>> grouped = new LinkedHashMap<>();
        grouped.put("Controller", new ArrayList<>());
        grouped.put("Service", new ArrayList<>());
        grouped.put("Repository", new ArrayList<>());
        grouped.put("Component", new ArrayList<>());

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Class<?> beanClass = bean.getClass();

                // Skip Spring internal beans
                String className = beanClass.getName();
                if (className.startsWith("org.springframework.") || className.startsWith("spring.")) {
                    continue;
                }

                String simpleName = beanClass.getSimpleName();
                // Handle CGLIB proxies
                if (simpleName.contains("$$")) {
                    simpleName = simpleName.substring(0, simpleName.indexOf("$$"));
                    className = className.substring(0, className.indexOf("$$"));
                }

                String entry = simpleName + " (" + className + ")";

                if (beanClass.isAnnotationPresent(RestController.class) || beanClass.isAnnotationPresent(Controller.class)) {
                    grouped.get("Controller").add(entry);
                } else if (beanClass.isAnnotationPresent(Service.class)) {
                    grouped.get("Service").add(entry);
                } else if (beanClass.isAnnotationPresent(Repository.class)) {
                    grouped.get("Repository").add(entry);
                } else if (beanClass.isAnnotationPresent(Component.class)) {
                    grouped.get("Component").add(entry);
                }
            } catch (Exception ignored) {
                // Skip beans that can't be introspected
            }
        }

        StringJoiner sb = new StringJoiner("\n");
        sb.add("=== Application Beans ===");

        boolean filterAll = "all".equalsIgnoreCase(layer);
        int total = 0;

        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            String category = entry.getKey();
            List<String> beans = entry.getValue();

            if (!filterAll && !category.equalsIgnoreCase(layer)) {
                continue;
            }

            sb.add("");
            sb.add("--- " + category + " (" + beans.size() + ") ---");
            if (beans.isEmpty()) {
                sb.add("  (none)");
            } else {
                beans.stream().sorted().forEach(b -> sb.add("  • " + b));
            }
            total += beans.size();
        }

        sb.add("");
        sb.add("Total: " + total + " bean(s)");

        return sb.toString();
    }
}
