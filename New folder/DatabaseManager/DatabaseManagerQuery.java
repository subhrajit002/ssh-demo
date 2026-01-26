package com.restAPIJAVA.demo.service.DatabaseManager;

public class DatabaseManagerQuery {

    public static final String GET_TABLE_INFO = """
                SELECT schema_name,
                       table_name,
                       is_ucct,
                       is_user_updatable,
                       is_dq_enabled
                FROM PL_DM_TABLE_INFO
                WHERE schema_name = ?
                  AND table_name = ?
            """;

    public static final String GET_COLUMN_INFO = """
                SELECT schema_name,
                       table_name,
                       column_name,
                       is_ucc,
                       is_user_updatable,
                       syn_datatype,
                       data_field_name
                FROM PL_DM_COLUMN_INFO
                WHERE schema_name = ?
                  AND table_name = ?
                ORDER BY column_name
            """;

    public static final String INSERT_TABLE_INFO = """
                INSERT INTO PL_DM_TABLE_INFO
                (schema_name, table_name, is_ucct, is_user_updatable, is_dq_enabled)
                VALUES (?, ?, ?, ?, ?)
            """;

    public static final String INSERT_COLUMN_INFO = """
                INSERT INTO PL_DM_COLUMN_INFO
                (schema_name, table_name, column_name, is_ucc, is_user_updatable, syn_datatype, data_field_name)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public static final String CHECK_TABLE_EXISTS = """
                SELECT COUNT(1)
                FROM PL_DM_TABLE_INFO
                WHERE schema_name = ?
                  AND table_name = ?
            """;

    public static final String CHECK_TABLE_UPDATABLE = """
                SELECT is_user_updatable
                FROM PL_DM_TABLE_INFO
                WHERE schema_name = ?
                  AND table_name = ?
            """;

    public static final String COUNT_COLUMNS = """
                SELECT COUNT(1)
                FROM PL_DM_COLUMN_INFO
                WHERE schema_name = ?
                  AND table_name = ?
            """;

    public static final String GET_DP_EXT_COUNT = """
                SELECT ext_count
                FROM DP_TABLE_EXT_CONFIG
                WHERE table_name = ?
            """;

    public static final String INSERT_DP_EXT_CONFIG = """
                INSERT INTO DP_TABLE_EXT_CONFIG (table_name, ext_count, comment)
                VALUES (?, ?, ?)
            """;

    public static final String UPDATE_DP_EXT_CONFIG = """
                UPDATE DP_TABLE_EXT_CONFIG
                SET ext_count = ?
                WHERE table_name = ?
            """;

}
