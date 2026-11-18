
package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.java.database.DBConnection;
import main.java.model.Event;
import main.java.model.Student;
import main.java.model.Registration;

public class DashboardOverviewDAO {

    public List<Event> getUpcomingEvents() {

        List<Event> events = new ArrayList<>();

        String sql = """
                SELECT e.*,
                       COUNT(r.registration_id) AS registered_count
                FROM events e
                LEFT JOIN registrations r
                    ON e.event_id = r.event_id
                WHERE e.is_cancelled = FALSE
                  AND (
                      e.event_date > CURRENT_DATE
                      OR (
                          e.event_date = CURRENT_DATE
                          AND e.end_time >= CURRENT_TIME
                      )
                  )
                GROUP BY e.event_id
                ORDER BY e.event_date, e.start_time
                LIMIT 5
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve upcoming events.", e);
        }

        return events;
    }

    public List<Registration> getRecentRegistrations() {

        List<Registration> registrations = new ArrayList<>();

        String sql = """
                SELECT
                    r.registration_id,
                    r.attendance_status,
                    s.student_id,
                    s.student_name,
                    s.email,
                    e.event_id,
                    e.event_name,
                    e.venue,
                    e.event_date,
                    e.capacity
                FROM registrations r
                JOIN students s
                    ON r.student_id = s.student_id
                JOIN events e
                    ON r.event_id = e.event_id
                ORDER BY r.registration_id DESC
                LIMIT 5
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                Student student = new Student(
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("email"));

                Event event = new Event(
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getString("venue"),
                        rs.getString("event_date"),
                        rs.getInt("capacity"));

                registrations.add(new Registration(
                        rs.getInt("registration_id"),
                        student,
                        event,
                        rs.getString("attendance_status")));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve recent registrations.", e);
        }

        return registrations;
    }

    public Map<String, Integer> getCategoryCounts() {

        Map<String, Integer> counts = new LinkedHashMap<>();

        String sql = """
                SELECT category, COUNT(*) AS event_count
                FROM events
                GROUP BY category
                ORDER BY event_count DESC
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                counts.put(
                        rs.getString("category"),
                        rs.getInt("event_count"));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve event categories.", e);
        }

        return counts;
    }

    private Event mapEvent(ResultSet rs) throws SQLException {

        return new Event(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("venue"),
                rs.getDate("event_date").toString(),
                rs.getInt("capacity"),
                rs.getInt("registered_count"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getTime("start_time").toString(),
                rs.getTime("end_time").toString(),
                rs.getDate("registration_deadline").toString(),
                rs.getString("poster_path"),
                rs.getBoolean("is_cancelled"));
    }
}
