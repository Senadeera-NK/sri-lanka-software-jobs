package lk.jobs.db;

import lk.jobs.utils.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        // Use a synchronized block if you ever plan to use threads
        if (connection == null || connection.isClosed()) {
            String url = Config.get("db.url");
            String user = Config.get("db.username");
            String pass = Config.get("db.password");

            System.out.println("DEBUG: Opening new connection to Supabase...");
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DEBUG: Connection closed safely.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}