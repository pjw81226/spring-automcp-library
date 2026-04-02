# Spring MCP Starter - Example

This is a minimal Spring Boot application that demonstrates how to use [spring-mcp-starter](https://github.com/pjw81226/spring-mcp-starter).

It showcases custom MCP tools that help AI agents understand the backend server's context.

## Included Tools

| Tool | Description |
|---|---|
| `get_server_info` | JVM version, OS, memory usage, uptime, active profiles |
| `get_db_schema` | Database tables and column details from the DataSource |
| `get_spring_beans` | Lists registered Spring beans by layer (Controller/Service/Repository/Component) |
| `read_backend_log` | Built-in — reads application log tail |

## Run

```bash
./gradlew bootRun
```

The MCP server will start at:
- **SSE**: `http://localhost:8080/mcp/sse`
- **Message**: `http://localhost:8080/mcp/message`

## Connect with MCP Client

### Cursor / Claude Desktop

Add the following to your MCP config:

```json
{
  "mcpServers": {
    "example-mcp-server": {
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

## Example Usage

Once connected, an AI agent can:

1. **`get_server_info`** — Understand the deployment environment
2. **`get_db_schema`** — Explore the data model (tables, columns, foreign keys)
3. **`get_spring_beans`** — Understand the application architecture
4. **`read_backend_log`** — Read recent log output for debugging
