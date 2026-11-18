
package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.database.DBConnection;
import main.java.model.DashboardStats;

public class DashboardStatsDAO {

    public DashboardStats getDashboardStats() {

        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM events) AS total_events,
                    (SELECT COUNT(*) FROM students) AS total_students,
                    (SELECT COUNT(*) FROM registrations) AS total_registrations,
                    (
                        (SELECT COALESCE(SUM(capacity), 0) FROM events)
                        -
                        (SELECT COUNT(*) FROM registrations)
                    ) AS available_seats,
                    (
                        SELECT COUNT(*) FROM registrations
                        WHERE attendance_status = 'Present'
                    ) AS present_count,
                    (
                        SELECT COUNT(*) FROM registrations
                        WHERE attendance_status = 'Absent'
                    ) AS absent_count,
                    (
                        SELECT COUNT(*) FROM registrations
                        WHERE attendance_status = 'Not Marked'
                    ) AS not_marked_count
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return new DashboardStats(
                        rs.getInt("total_events"),
                        rs.getInt("total_students"),
                        rs.getInt("total_registrations"),
                        Math.max(0, rs.getInt("available_seats")),
                        rs.getInt("present_count"),
                        rs.getInt("absent_count"),
                        rs.getInt("not_marked_count"));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve dashboard statistics.", e);
        }

        return new DashboardStats(0, 0, 0, 0, 0, 0, 0);
    }
}
