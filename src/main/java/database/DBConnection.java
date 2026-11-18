package main.java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "campus_event_mngt";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to Local MySQL successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
