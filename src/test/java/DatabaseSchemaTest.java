package test.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import main.java.database.DBConnection;

public class DatabaseSchemaTest {

    public static void main(String[] args) {

        String sql = "SHOW CREATE TABLE registrations";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                System.out.println("========== REGISTRATIONS TABLE STRUCTURE ==========");
                System.out.println(rs.getString("Create Table"));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
