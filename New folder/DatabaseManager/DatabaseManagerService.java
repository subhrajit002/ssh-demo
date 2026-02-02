package com.restAPIJAVA.demo.service.DatabaseManager;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DatabaseManagerService {

    private final HelperFunction helperFunction;

    public Map<String, Object> getTableInfo(String schema, String table) {
        return helperFunction.fetchTableMetadata(schema, table);
    }

    public List<Map<String, Object>> getColumnInfo(String schema, String table) {
        return helperFunction.fetchColumnMetadata(schema, table);
    }

    public void createTable(
            String schema,
            String table,
            List<String> columns,
            boolean isUcct,
            boolean isUserUpdatable,
            boolean isDqEnabled) {

        helperFunction.createTableWithMetadata(
                schema,
                table,
                columns,
                isUcct,
                isUserUpdatable,
                isDqEnabled);
    }

    public void addColumn(
            String schema,
            String table,
            String column,
            String datatype,
            boolean isUcc,
            boolean isUserUpdatable,
            String dataFieldName) {

        helperFunction.addColumnWithMetadata(
                schema,
                table,
                column,
                datatype,
                isUcc,
                isUserUpdatable,
                dataFieldName);
    }

    @Transactional
    public Map<String, Object> updateTableExtension(
            String schema,
            String table,
            int newExtCount,
            String configType,
            boolean forceDrop) {

        return helperFunction.handleTableExtension(
                schema, table, newExtCount, configType, forceDrop);
    }

}
