package com.example.mcpexample.tool;

import com.jang.mcp.starter.tool.McpToolProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * Exposes server environment information so that the AI Agent
 * can understand the runtime context of the backend application.
 */
@Component
public class ServerInfoTool implements McpToolProvider {

    private final Environment environment;

    public ServerInfoTool(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getName() {
        return "get_server_info";
    }

    @Override
    public String getDescription() {
        return "Returns server runtime information including JVM version, OS, memory usage, " +
               "uptime, and active Spring profiles. Useful for understanding the deployment environment.";
    }

    @Override
    public Class<?> getParameterType() {
        return null; // no parameters needed
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        long uptimeMs = runtime.getUptime();
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;

        long heapUsed = memory.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memory.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long nonHeapUsed = memory.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);

        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";

        StringJoiner sb = new StringJoiner("\n");
        sb.add("=== Server Information ===");
        sb.add("Application : " + environment.getProperty("spring.application.name", "N/A"));
        sb.add("Active Profiles : " + profiles);
        sb.add("");
        sb.add("--- JVM ---");
        sb.add("Java Version : " + runtime.getSpecVersion());
        sb.add("JVM Name     : " + runtime.getVmName());
        sb.add("JVM Vendor   : " + runtime.getVmVendor());
        sb.add("");
        sb.add("--- OS ---");
        sb.add("OS           : " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        sb.add("Architecture : " + System.getProperty("os.arch"));
        sb.add("");
        sb.add("--- Memory ---");
        sb.add("Heap Used    : " + heapUsed + " MB / " + heapMax + " MB");
        sb.add("Non-Heap Used: " + nonHeapUsed + " MB");
        sb.add("");
        sb.add("--- Uptime ---");
        sb.add("Uptime       : " + hours + "h " + minutes + "m " + seconds + "s");
        sb.add("Processors   : " + Runtime.getRuntime().availableProcessors());

        return sb.toString();
    }
}
