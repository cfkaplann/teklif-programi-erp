package com.teklif.db;

import java.sql.Connection;
import java.sql.DriverManager;
import com.teklif.system.AppPathManager;

public class ConnectionManager {

    public static Connection getConnection() throws Exception {

        Class.forName("org.sqlite.JDBC");

        String dbPath =
                AppPathManager.getDatabaseFile().getAbsolutePath();

        System.out.println("DB PATH = " + dbPath);

        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}