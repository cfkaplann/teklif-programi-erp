package com.teklif.db;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init() {

        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement()) {

            // =====================================================
            // HAM FİYAT TABLOLARI
            // =====================================================

            st.execute("""
                CREATE TABLE IF NOT EXISTS price_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sheet_name TEXT,
                    prefix TEXT,
                    strategy TEXT
                );
            """);

            // ⭐ Axis tablosu artık STRING + NUMERIC destekli
            st.execute("""
                CREATE TABLE IF NOT EXISTS price_axis (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    table_id INTEGER,
                    axis TEXT,
                    value_num REAL,
                    value_str TEXT
                );
            """);

            // ⭐ Cell tablosu artık STRING + NUMERIC destekli
            st.execute("""
                CREATE TABLE IF NOT EXISTS price_cell (
                    table_id INTEGER,
                    row_value REAL,
                    col_value REAL,
                    row_value_str TEXT,
                    col_value_str TEXT,
                    price REAL
                );
            """);

            // =====================================================
            // ÖZELLİK ORANLARI
            // =====================================================

            st.execute("""
                CREATE TABLE IF NOT EXISTS feature_ratio (
                    feature_type TEXT,
                    option_name TEXT,
                    ratio REAL
                );
            """);

            System.out.println("TABLOLAR OLUSTURULDU");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
