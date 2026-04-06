package com.example.mcpexample.tool;

import com.jang.mcp.starter.annotation.McpParameter;
import com.jang.mcp.starter.tool.McpToolProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.StringJoiner;

/**
 * Exposes the database schema (tables and columns) so that the AI Agent
 * can understand the data model of the backend application.
 */
@Component
public class DatabaseSchemaTool implements McpToolProvider<DatabaseSchemaTool.Params> {

    private final DataSource dataSource;

    public DatabaseSchemaTool(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public record Params(
        @McpParameter(description = "Specific table name to inspect. If empty, lists all tables.", required = false)
        String tableName
    ) {}

    @Override
    public String getName() {
        return "get_db_schema";
    }

    @Override
    public String getDescription() {
        return "Retrieves database schema information. Without a table name, lists all tables. " +
               "With a table name, shows column details (name, type, nullable, key). " +
               "Helps understand the data model of the application.";
    }

    @Override
    public Class<Params> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Params params) {
        String tableName = params != null ? params.tableName() : null;

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            StringJoiner sb = new StringJoiner("\n");

            sb.add("=== Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion() + " ===");
            sb.add("");

            if (tableName == null || tableName.isBlank()) {
                return listAllTables(meta, sb);
            } else {
                return describeTable(meta, tableName.toUpperCase(), sb);
            }
        } catch (Exception e) {
            return "Error reading database schema: " + e.getMessage();
        }
    }

    private String listAllTables(DatabaseMetaData meta, StringJoiner sb) throws Exception {
        sb.add("--- Tables ---");

        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            int count = 0;
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String name = rs.getString("TABLE_NAME");
                String type = rs.getString("TABLE_TYPE");
                sb.add(String.format("  %-30s  %-10s  (schema: %s)", name, type, schema));
                count++;
            }
            sb.add("");
            sb.add("Total: " + count + " table(s)");
            sb.add("Tip: Call this tool with a specific tableName to see column details.");
        }
        return sb.toString();
    }

    private String describeTable(DatabaseMetaData meta, String tableName, StringJoiner sb) throws Exception {
        sb.add("--- Table: " + tableName + " ---");
        sb.add(String.format("  %-25s %-15s %-8s %-5s", "COLUMN", "TYPE", "NULLABLE", "KEY"));
        sb.add("  " + "-".repeat(55));

        // Get primary key columns
        java.util.Set<String> pkColumns = new java.util.HashSet<>();
        try (ResultSet pk = meta.getPrimaryKeys(null, null, tableName)) {
            while (pk.next()) {
                pkColumns.add(pk.getString("COLUMN_NAME"));
            }
        }

        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            int count = 0;
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                String type = rs.getString("TYPE_NAME");
                int size = rs.getInt("COLUMN_SIZE");
                String nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable ? "YES" : "NO";
                String key = pkColumns.contains(col) ? "PK" : "";

                sb.add(String.format("  %-25s %-15s %-8s %-5s", col, type + "(" + size + ")", nullable, key));
                count++;
            }
            if (count == 0) {
                sb.add("  (table not found or no columns)");
            }
        }

        // Foreign keys
        try (ResultSet fk = meta.getImportedKeys(null, null, tableName)) {
            boolean hasFk = false;
            while (fk.next()) {
                if (!hasFk) {
                    sb.add("");
                    sb.add("  --- Foreign Keys ---");
                    hasFk = true;
                }
                sb.add(String.format("  %s -> %s.%s",
                        fk.getString("FKCOLUMN_NAME"),
                        fk.getString("PKTABLE_NAME"),
                        fk.getString("PKCOLUMN_NAME")));
            }
        }

        return sb.toString();
    }
}
