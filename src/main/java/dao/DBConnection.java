package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String url;
    private static String username;
    private static String password;
    private static String driverClassName;

    public static void configure(String datasourceUrl, String datasourceUsername, String datasourcePassword,
            String datasourceDriverClassName) {
        url = datasourceUrl;
        username = datasourceUsername;
        password = datasourcePassword;
        driverClassName = datasourceDriverClassName;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return DriverManager.getConnection(url, username, password);
    }
}
