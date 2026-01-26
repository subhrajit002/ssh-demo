package com.restAPIJAVA.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restAPIJAVA.demo.service.DatabaseManager.DatabaseManagerService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/database-manager")
public class DatabaseManagerController {

    private final DatabaseManagerService databaseManagerService;

    @GetMapping("/table-info")
    public Map<String, Object> getTableInfo(
            @RequestParam String schema,
            @RequestParam String table) {
        return databaseManagerService.getTableInfo(schema, table);
    }

    @GetMapping("/column-info")
    public List<Map<String, Object>> getColumnInfo(
            @RequestParam String schema,
            @RequestParam String table) {
        return databaseManagerService.getColumnInfo(schema, table);
    }

    @PostMapping("/create-table")
    public String createTable(
            @RequestParam String schema,
            @RequestParam String table,
            @RequestParam List<String> columns, // colName:datatype
            @RequestParam(defaultValue = "false") boolean isUcct,
            @RequestParam(defaultValue = "true") boolean isUserUpdatable,
            @RequestParam(defaultValue = "true") boolean isDqEnabled) {

        databaseManagerService.createTable(
                schema,
                table,
                columns,
                isUcct,
                isUserUpdatable,
                isDqEnabled);

        return "Table created successfully";
    }

    @PostMapping("/add-column")
    public String addColumn(
            @RequestParam String schema,
            @RequestParam String table,
            @RequestParam String column,
            @RequestParam String datatype,
            @RequestParam(defaultValue = "false") boolean isUcc,
            @RequestParam(defaultValue = "true") boolean isUserUpdatable,
            @RequestParam(required = false) String dataFieldName) {

        databaseManagerService.addColumn(
                schema,
                table,
                column,
                datatype,
                isUcc,
                isUserUpdatable,
                dataFieldName);

        return "Column added successfully";
    }

}
