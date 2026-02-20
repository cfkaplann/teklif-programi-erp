package com.teklif.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {

    public static Connection getConnection() throws Exception {

        return DriverManager.getConnection("jdbc:sqlite:data/teklif.db");
    }
}
