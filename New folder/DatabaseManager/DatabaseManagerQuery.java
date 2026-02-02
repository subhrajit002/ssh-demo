package com.restAPIJAVA.demo.service.DatabaseManager;

public class DatabaseManagerQuery {

        public static final String GET_TABLE_INFO = """
                            SELECT schema_name,
                                   table_name,
                                   is_ucct,
                                   is_user_updatable,
                                   is_dq_enabled
                            FROM sclpl.PL_DM_TABLE_INFO
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
                            FROM sclpl.pl_dm_column_info
                            WHERE schema_name = ?
                              AND table_name = ?
                            ORDER BY column_name
                        """;

        public static final String INSERT_TABLE_INFO = """
                            INSERT INTO sclpl.PL_DM_TABLE_INFO
                            (schema_name, table_name, is_ucct, is_user_updatable, is_dq_enabled)
                            VALUES (?, ?, ?, ?, ?)
                        """;

        public static final String INSERT_COLUMN_INFO = """
                            INSERT INTO sclpl.pl_dm_column_info
                            (schema_name, table_name, column_name, is_ucc, is_user_updatable, syn_datatype, data_field_name)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;

        public static final String CHECK_TABLE_EXISTS = """
                            SELECT COUNT(1)
                            FROM sclpl.PL_DM_TABLE_INFO
                            WHERE schema_name = ?
                              AND table_name = ?
                        """;


        public static final String COUNT_COLUMNS = """
                            SELECT COUNT(1)
                            FROM sclpl.pl_dm_column_info
                            WHERE schema_name = ?
                              AND table_name = ?
                        """;


        public static final String INSERT_DP_EXT_CONFIG = """
                            INSERT INTO sclpl.DP_TABLE_EXT_CONFIG (table_name, ext_count, comment)
                            VALUES (?, ?, ?)
                        """;

        public static final String UPDATE_DP_EXT_CONFIG = """
                            UPDATE sclpl.DP_TABLE_EXT_CONFIG
                            SET ext_count = ?
                            WHERE table_name = ?
                        """;

        public static final String CHECK_TABLE_UPDATABLE = """
                            SELECT is_user_updatable
                            FROM sclpl.PL_DM_TABLE_INFO
                            WHERE schema_name = ?
                              AND table_name = ?
                        """;

        public static final String GET_DP_EXT_COUNT = """
                            SELECT ext_count
                            FROM sclpl.dp_table_ext_config
                            WHERE table_name = ?
                        """;

        public static final String GET_RP_EXT_COUNT = """
                            SELECT ext_count
                            FROM sclrp.RP_TABLE_EXT_CONFIG
                            WHERE table_name = ?
                        """;

        public static final String UPDATE_DP_EXT_COUNT = """
                            UPDATE sclpl.dp_table_ext_config
                            SET ext_count = ?
                            WHERE table_name = ?
                        """;

        public static final String UPDATE_RP_EXT_COUNT = """
                            UPDATE sclrp.RP_TABLE_EXT_CONFIG
                            SET ext_count = ?
                            WHERE table_name = ?
                        """;

}
