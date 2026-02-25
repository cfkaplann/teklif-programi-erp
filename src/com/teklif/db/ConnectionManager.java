package com.teklif.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {

    private static final String BASE =
            new java.io.File("").getAbsolutePath();

    public static Connection getConnection() throws Exception {

        Class.forName("org.sqlite.JDBC");

        String dbPath = BASE + "/data/teklif.db";

        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

}
