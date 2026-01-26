package com.restAPIJAVA.demo.service.DatabaseManager;

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

    private String buildAlterTableQuery(
            String schema,
            String table,
            String column,
            String datatype) {

        return "ALTER TABLE " +
                schema + "." + table +
                " ADD " + column + " " + datatype;
    }

    private void validateTableIsUpdatable(String schema, String table) {

        Boolean isUpdatable = jdbcTemplate.queryForObject(
                DatabaseManagerQuery.CHECK_TABLE_UPDATABLE,
                Boolean.class,
                schema,
                table);

        if (isUpdatable == null) {
            throw new IllegalStateException("Table metadata not found");
        }

        if (!isUpdatable) {
            throw new IllegalStateException(
                    "Table is not user updatable. ALTER operations are not allowed.");
        }
    }

}