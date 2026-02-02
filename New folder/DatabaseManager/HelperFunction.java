package com.restAPIJAVA.demo.service.DatabaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HelperFunction {

    private final JdbcTemplate jdbcTemplate;

    private static final int MAX_COLUMNS_PER_TABLE = 5; // example, configurable later

    public Map<String, Object> fetchTableMetadata(String schema, String table) {
        if (schema == null || table == null) {
            throw new IllegalArgumentException("Schema and table name must not be null");
        }

        return jdbcTemplate.queryForMap(
                DatabaseManagerQuery.GET_TABLE_INFO,
                schema,
                table);
    }

    public List<Map<String, Object>> fetchColumnMetadata(String schema, String table) {
        if (schema == null || table == null) {
            throw new IllegalArgumentException("Schema and table must not be null");
        }

        return jdbcTemplate.queryForList(
                DatabaseManagerQuery.GET_COLUMN_INFO,
                schema,
                table);
    }

    @Transactional
    public void createTableWithMetadata(
            String schema,
            String table,
            List<String> columns,
            boolean isUcct,
            boolean isUserUpdatable,
            boolean isDqEnabled) {

        validateInputs(schema, table, columns);

        // 1. CREATE TABLE
        String createTableSql = buildCreateTableQuery(schema, table, columns);
        jdbcTemplate.execute(createTableSql);

        // 2. INSERT TABLE METADATA
        jdbcTemplate.update(
                DatabaseManagerQuery.INSERT_TABLE_INFO,
                schema,
                table,
                isUcct,
                isUserUpdatable,
                isDqEnabled);

        // 3. INSERT COLUMN METADATA
        for (String col : columns) {
            String[] parts = col.split(":");

            jdbcTemplate.update(
                    DatabaseManagerQuery.INSERT_COLUMN_INFO,
                    schema,
                    table,
                    parts[0], // column name
                    false, // is_ucc
                    true, // is_user_updatable
                    parts[1], // syn_datatype
                    parts[0] // data_field_name
            );
        }
    }

    @Transactional
    public void addColumnWithMetadata(
            String schema,
            String table,
            String column,
            String datatype,
            boolean isUcc,
            boolean isUserUpdatable,
            String dataFieldName) {

        validateAddColumnInputs(schema, table, column, datatype);
        validateTableIsUpdatable(schema, table);

        String physicalTable = resolvePhysicalTableForNewColumn(schema, table);

        String alterSql = "ALTER TABLE " + schema + "." + physicalTable +
                " ADD " + column + " " + datatype;

        jdbcTemplate.execute(alterSql);

        jdbcTemplate.update(
                DatabaseManagerQuery.INSERT_COLUMN_INFO,
                schema,
                physicalTable, // IMPORTANT
                column,
                isUcc,
                isUserUpdatable,
                datatype,
                dataFieldName != null ? dataFieldName : column);
    }

    // @Transactional
    // public void addColumnWithMetadata(
    // String schema,
    // String table,
    // String column,
    // String datatype,
    // boolean isUcc,
    // boolean isUserUpdatable,
    // String dataFieldName) {

    // validateAddColumnInputs(schema, table, column, datatype);

    // // NEW LINE 🔥
    // validateTableIsUpdatable(schema, table);

    // Integer count = jdbcTemplate.queryForObject(
    // DatabaseManagerQuery.CHECK_TABLE_EXISTS,
    // Integer.class,
    // schema,
    // table);

    // if (count == null || count == 0) {
    // throw new IllegalStateException("Table does not exist");
    // }

    // String alterSql = buildAlterTableQuery(schema, table, column, datatype);
    // jdbcTemplate.execute(alterSql);

    // jdbcTemplate.update(
    // DatabaseManagerQuery.INSERT_COLUMN_INFO,
    // schema,
    // table,
    // column,
    // isUcc,
    // isUserUpdatable,
    // datatype,
    // dataFieldName != null ? dataFieldName : column);
    // }

    private String resolvePhysicalTableForNewColumn(String schema, String baseTable) {

        Integer columnCount = jdbcTemplate.queryForObject(
                DatabaseManagerQuery.COUNT_COLUMNS,
                Integer.class,
                schema,
                baseTable);

        if (columnCount < MAX_COLUMNS_PER_TABLE) {
            return baseTable; // base table still has space
        }

        // Need extension table
        Integer extCount;

        try {
            extCount = jdbcTemplate.queryForObject(
                    DatabaseManagerQuery.GET_DP_EXT_COUNT,
                    Integer.class,
                    baseTable);
        } catch (Exception e) {
            extCount = 0;
            jdbcTemplate.update(
                    DatabaseManagerQuery.INSERT_DP_EXT_CONFIG,
                    baseTable,
                    0,
                    "Initial extension");
        }

        String extTable = baseTable + "_EXT_" + (extCount + 1);

        // Check if extension table exists physically
        if (!doesTableExist(schema, extTable)) {

            String createExtSql = "CREATE TABLE " + schema + "." + extTable + " ()";
            jdbcTemplate.execute(createExtSql);

            jdbcTemplate.update(
                    DatabaseManagerQuery.UPDATE_DP_EXT_CONFIG,
                    extCount + 1,
                    baseTable);
        }

        return extTable;
    }

    private boolean doesTableExist(String schema, String table) {

        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.tables
                        WHERE table_schema = ?
                          AND table_name = ?
                        """,
                Integer.class,
                schema,
                table);

        return count != null && count > 0;
    }

    private void validateInputs(String schema, String table, List<String> columns) {

        if (schema == null || table == null || columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Invalid input");
        }

        if (!table.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid table name");
        }
    }

    private String buildCreateTableQuery(
            String schema,
            String table,
            List<String> columns) {

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ")
                .append(schema)
                .append(".")
                .append(table)
                .append(" (");

        for (String col : columns) {
            String[] parts = col.split(":");
            sql.append(parts[0])
                    .append(" ")
                    .append(parts[1])
                    .append(", ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(")");

        return sql.toString();
    }

    private void validateAddColumnInputs(
            String schema,
            String table,
            String column,
            String datatype) {

        if (schema == null || table == null || column == null || datatype == null) {
            throw new IllegalArgumentException("Invalid input");
        }

        if (!column.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid column name");
        }
    }

    public Map<String, Object> handleTableExtension(
            String schema,
            String table,
            int newExtCount,
            String configType,
            boolean forceDrop) {

        validateInputs(schema, table, newExtCount, configType);
        validateTableIsUpdatable(schema, table);

        int currentExtCount = fetchCurrentExtCount(table, configType);

        if (newExtCount == currentExtCount) {
            return response("SUCCESS", "No change in extension count", currentExtCount, newExtCount);
        }

        if (newExtCount > currentExtCount) {
            addTermColumns(schema, table, currentExtCount + 1, newExtCount);
        } else {
            boolean hasData = checkDataExists(
                    schema, table, newExtCount + 1, currentExtCount);

            if (hasData && !forceDrop) {
                return warningResponse(newExtCount + 1, currentExtCount);
            }

            dropTermColumns(schema, table, newExtCount + 1, currentExtCount);
        }

        updateExtCount(table, newExtCount, configType);

        return response("SUCCESS", "Extension updated successfully", currentExtCount, newExtCount);
    }

    /* ======================= HELPERS ======================= */

    private void validateInputs(String schema, String table, int newExtCount, String configType) {
        if (schema == null || table == null || configType == null) {
            throw new IllegalArgumentException("Invalid input");
        }
        if (newExtCount < 0) {
            throw new IllegalArgumentException("ext_count cannot be negative");
        }
    }

    private void validateTableIsUpdatable(String schema, String table) {
        Boolean updatable = jdbcTemplate.queryForObject(
                DatabaseManagerQuery.CHECK_TABLE_UPDATABLE,
                Boolean.class,
                schema,
                table);

        if (updatable == null) {
            throw new IllegalStateException("Table metadata not found");
        }

        if (!updatable) {
            throw new IllegalStateException(
                    "Table is not user updatable. ALTER operations are not allowed.");
        }
    }

    private int fetchCurrentExtCount(String table, String configType) {
        String sql = configType.equalsIgnoreCase("DP")
                ? DatabaseManagerQuery.GET_DP_EXT_COUNT
                : DatabaseManagerQuery.GET_RP_EXT_COUNT;

        return jdbcTemplate.queryForObject(sql, Integer.class, table);
    }

    private void addTermColumns(String schema, String table, int start, int end) {
        for (int i = start; i <= end; i++) {
            jdbcTemplate.execute(
                    "ALTER TABLE " + schema + "." + table +
                            " ADD term" + i + " VARCHAR(255)");
        }
    }

    private boolean checkDataExists(String schema, String table, int start, int end) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM " + schema + "." + table + " WHERE ");

        for (int i = start; i <= end; i++) {
            sql.append("term").append(i).append(" IS NOT NULL");
            if (i < end)
                sql.append(" OR ");
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return count != null && count > 0;
    }

    private void dropTermColumns(String schema, String table, int start, int end) {
        for (int i = end; i >= start; i--) {
            jdbcTemplate.execute(
                    "ALTER TABLE " + schema + "." + table +
                            " DROP COLUMN term" + i);
        }
    }

    private void updateExtCount(String table, int newExtCount, String configType) {
        String sql = configType.equalsIgnoreCase("DP")
                ? DatabaseManagerQuery.UPDATE_DP_EXT_COUNT
                : DatabaseManagerQuery.UPDATE_RP_EXT_COUNT;

        jdbcTemplate.update(sql, newExtCount, table);
    }

    private Map<String, Object> response(
            String status, String message, int oldVal, int newVal) {

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("message", message);
        map.put("oldExtCount", oldVal);
        map.put("newExtCount", newVal);
        return map;
    }

    private Map<String, Object> warningResponse(int start, int end) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "WARNING");
        map.put("message",
                "Data exists in term columns term" + start +
                        " to term" + end +
                        ". Re-submit with forceDrop=true to proceed.");
        return map;
    }
}
