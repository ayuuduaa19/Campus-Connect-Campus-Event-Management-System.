
package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import main.java.database.DBConnection;
import main.java.model.Event;

public class EventDAO {

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
                rs.getBoolean("is_cancelled"),
                rs.getBoolean("certificate_enabled"));
    }

    private String selectQuery() {
        return """
                SELECT e.*,
                       COUNT(r.registration_id) AS registered_count
                FROM events e
                LEFT JOIN registrations r
                    ON e.event_id = r.event_id
                """;
    }

    public Event findEventById(int eventId) {

        String sql = selectQuery() + """
                WHERE e.event_id = ?
                GROUP BY e.event_id
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }

        } catch (SQLException e) {
            throw databaseException("find event", e);
        }

        return null;
    }

    public boolean addEvent(Event event) {

        String sql = """
                INSERT INTO events
                (event_id, event_name, venue, event_date, capacity,
                 category, description, start_time, end_time,
                 registration_deadline, poster_path, is_cancelled,
                 certificate_enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            setEventParameters(ps, event, true);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            throw databaseException("add event", e);
        }
    }

    public boolean updateEvent(Event event) {

        String countSql = """
                SELECT COUNT(*) FROM registrations WHERE event_id = ?
                """;

        String sql = """
                UPDATE events SET
                    event_name = ?,
                    venue = ?,
                    event_date = ?,
                    capacity = ?,
                    category = ?,
                    description = ?,
                    start_time = ?,
                    end_time = ?,
                    registration_deadline = ?,
                    poster_path = ?,
                    is_cancelled = ?,
                    certificate_enabled = ?
                WHERE event_id = ?
                """;

        try (Connection con = DBConnection.getConnection()) {

            int registeredCount;

            try (PreparedStatement ps = con.prepareStatement(countSql)) {
                ps.setInt(1, event.getEventId());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    registeredCount = rs.getInt(1);
                }
            }

            if (event.getCapacity() < registeredCount) {
                throw new IllegalArgumentException(
                        "Capacity cannot be less than current registrations ("
                                + registeredCount + ").");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, event.getEventName().trim());
                ps.setString(2, event.getVenue().trim());
                ps.setDate(3, Date.valueOf(event.getDate()));
                ps.setInt(4, event.getCapacity());
                ps.setString(5, event.getCategory());
                ps.setString(6, event.getDescription());
                ps.setTime(7, Time.valueOf(event.getStartTime()));
                ps.setTime(8, Time.valueOf(event.getEndTime()));
                ps.setDate(9, Date.valueOf(event.getRegistrationDeadline()));
                ps.setString(10, event.getPosterPath());
                ps.setBoolean(11, event.isCancelled());
                ps.setBoolean(12, event.isCertificateEnabled());
                ps.setInt(13, event.getEventId());

                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Event not found.");
                }

                return true;
            }

        } catch (SQLException e) {
            throw databaseException("update event", e);
        }
    }

    private void setEventParameters(
            PreparedStatement ps, Event event, boolean includeId)
            throws SQLException {

        int i = 1;

        if (includeId) {
            ps.setInt(i++, event.getEventId());
        }

        ps.setString(i++, event.getEventName().trim());
        ps.setString(i++, event.getVenue().trim());
        ps.setDate(i++, Date.valueOf(event.getDate()));
        ps.setInt(i++, event.getCapacity());
        ps.setString(i++, event.getCategory());
        ps.setString(i++, event.getDescription());
        ps.setTime(i++, Time.valueOf(event.getStartTime()));
        ps.setTime(i++, Time.valueOf(event.getEndTime()));
        ps.setDate(i++, Date.valueOf(event.getRegistrationDeadline()));
        ps.setString(i++, event.getPosterPath());
        ps.setBoolean(i++, event.isCancelled());
        ps.setBoolean(i, event.isCertificateEnabled());
    }

    public boolean deleteEvent(int eventId) {

        String sql = "DELETE FROM events WHERE event_id = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Event not found.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("delete event", e);
        }
    }

    public List<Event> getAllEvents() {

        List<Event> events = new ArrayList<>();

        String sql = selectQuery() + """
                GROUP BY e.event_id
                ORDER BY e.event_date, e.start_time
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }

        } catch (SQLException e) {
            throw databaseException("retrieve events", e);
        }

        return events;
    }

    public void displayAllEvents() {

        for (Event event : getAllEvents()) {
            event.displayDetails();
            System.out.println("Available Seats: "
                    + event.getAvailableSeats());
            System.out.println("----------------------------");
        }
    }

    private RuntimeException databaseException(
            String operation, SQLException e) {

        if (e.getErrorCode() == 1062) {
            return new IllegalStateException(
                    "An event with this ID already exists.", e);
        }

        if (e.getErrorCode() == 1451) {
            return new IllegalStateException(
                    "This event cannot be deleted because students are registered.", e);
        }

        return new IllegalStateException(
                "Could not " + operation
                        + ". Please check your database connection and try again.",
                e);
    }
}
