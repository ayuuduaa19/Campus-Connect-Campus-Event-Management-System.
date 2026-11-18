package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.java.database.DBConnection;
import main.java.model.Registration;
import main.java.model.Student;
import main.java.model.Event;

public class RegistrationDAO {

    // Admin registration with manually supplied registration ID
    public boolean registerStudent(
            int registrationId, int studentId, int eventId) {

        if (registrationId <= 0 || studentId <= 0 || eventId <= 0) {
            throw new IllegalArgumentException(
                    "Registration, student and event IDs must be positive.");
        }

        String insertQuery = """
                INSERT INTO registrations
                    (registration_id, student_id, event_id, attendance_status)
                VALUES (?, ?, ?, 'Not Marked')
                """;

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                checkEventCapacity(connection, eventId);

                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {

                    statement.setInt(1, registrationId);
                    statement.setInt(2, studentId);
                    statement.setInt(3, eventId);

                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);

                if (e instanceof SQLException sqlException) {
                    throw databaseException("register student", sqlException);
                }

                throw e;
            }

        } catch (SQLException e) {
            throw databaseException("connect to the database", e);
        }
    }

    // Student registration with MySQL-generated registration ID
    public boolean registerStudentForStudent(int studentId, int eventId) {

        if (studentId <= 0 || eventId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID and event ID must be positive.");
        }

        String insertQuery = """
                INSERT INTO registrations
                    (student_id, event_id, attendance_status)
                VALUES (?, ?, 'Not Marked')
                """;

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                checkEventCapacity(connection, eventId);

                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {

                    statement.setInt(1, studentId);
                    statement.setInt(2, eventId);

                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);

                if (e instanceof SQLException sqlException) {
                    throw databaseException("register student", sqlException);
                }

                throw e;
            }

        } catch (SQLException e) {
            throw databaseException("connect to the database", e);
        }
    }

    // Checks event existence and capacity while locking the event row
    private void checkEventCapacity(Connection connection, int eventId)
            throws SQLException {

        String capacityQuery = "SELECT capacity FROM events WHERE event_id = ? FOR UPDATE";

        String countQuery = "SELECT COUNT(*) FROM registrations WHERE event_id = ?";

        int capacity;

        try (PreparedStatement statement = connection.prepareStatement(capacityQuery)) {

            statement.setInt(1, eventId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "The selected event does not exist.");
                }

                capacity = rs.getInt("capacity");
            }
        }

        int registeredCount;

        try (PreparedStatement statement = connection.prepareStatement(countQuery)) {

            statement.setInt(1, eventId);

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                registeredCount = rs.getInt(1);
            }
        }

        if (registeredCount >= capacity) {
            throw new IllegalStateException(
                    "This event has reached its registration capacity.");
        }
    }

    private void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }

    // Get all registrations
    public List<Registration> getAllRegistrations() {

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
                JOIN students s ON r.student_id = s.student_id
                JOIN events e ON r.event_id = e.event_id
                ORDER BY r.registration_id
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                registrations.add(mapRegistration(rs));
            }

            return registrations;

        } catch (SQLException e) {
            throw databaseException("retrieve registrations", e);
        }
    }

    // Get registrations belonging to one student
    public List<Registration> getRegistrationsByStudent(int studentId) {

        if (studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be a positive number.");
        }

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
                JOIN students s ON r.student_id = s.student_id
                JOIN events e ON r.event_id = e.event_id
                WHERE r.student_id = ?
                ORDER BY r.registration_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    registrations.add(mapRegistration(rs));
                }
            }

            return registrations;

        } catch (SQLException e) {
            throw databaseException("retrieve student registrations", e);
        }
    }

    // Convert database result into a Registration object
    private Registration mapRegistration(ResultSet rs) throws SQLException {

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

        return new Registration(
                rs.getInt("registration_id"),
                student,
                event,
                rs.getString("attendance_status"));
    }

    // Display all registrations in console
    public void displayAllRegistrations() {

        List<Registration> registrations = getAllRegistrations();

        for (Registration registration : registrations) {
            System.out.println(
                    "Registration ID: " + registration.getRegistrationId()
                            + " | Student: "
                            + registration.getStudent().getStudentName()
                            + " | Event: "
                            + registration.getEvent().getEventName()
                            + " | Attendance: "
                            + registration.getAttendanceStatus());
        }
    }

    // Display registrations by student in console
    public void displayRegistrationsByStudent(int studentId) {

        List<Registration> registrations = getRegistrationsByStudent(studentId);

        if (registrations.isEmpty()) {
            System.out.println(
                    "No registrations found for Student ID: " + studentId);
            return;
        }

        for (Registration registration : registrations) {
            System.out.println(
                    "\nStudent ID: "
                            + registration.getStudent().getStudentId()
                            + "\nStudent Name: "
                            + registration.getStudent().getStudentName()
                            + "\nRegistration ID: "
                            + registration.getRegistrationId()
                            + "\nEvent: "
                            + registration.getEvent().getEventName()
                            + "\nVenue: "
                            + registration.getEvent().getVenue()
                            + "\nAttendance: "
                            + registration.getAttendanceStatus());
        }
    }

    // Admin cancellation
    public boolean cancelRegistration(int registrationId) {

        if (registrationId <= 0) {
            throw new IllegalArgumentException(
                    "Registration ID must be a positive number.");
        }

        String sql = "DELETE FROM registrations WHERE registration_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Registration with ID " + registrationId
                                + " was not found.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("cancel registration", e);
        }
    }

    // Student cancellation with ownership verification
    public boolean cancelStudentRegistration(
            int registrationId, int studentId) {

        if (registrationId <= 0 || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Registration ID and student ID must be positive.");
        }

        String sql = """
                DELETE FROM registrations
                WHERE registration_id = ? AND student_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);
            statement.setInt(2, studentId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Registration was not found or does not belong to this student.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("cancel student registration", e);
        }
    }

    // Mark attendance for one registration
    public boolean markAttendance(int registrationId, boolean present) {

        if (registrationId <= 0) {
            throw new IllegalArgumentException(
                    "Registration ID must be a positive number.");
        }

        String status = present ? "Present" : "Absent";

        String sql = """
                UPDATE registrations
                SET attendance_status = ?
                WHERE registration_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, registrationId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Registration with ID " + registrationId
                                + " was not found.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("update attendance", e);
        }
    }

    // Mark all participants of one event Present or Absent
    public int markAttendanceForEvent(int eventId, boolean present) {

        if (eventId <= 0) {
            throw new IllegalArgumentException(
                    "Event ID must be a positive number.");
        }

        String status = present ? "Present" : "Absent";

        String sql = """
                UPDATE registrations
                SET attendance_status = ?
                WHERE event_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, eventId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw databaseException("update event attendance", e);
        }
    }

    // Check whether a registration is eligible for a participation certificate
    public boolean isCertificateEligible(int registrationId) {

        if (registrationId <= 0) {
            throw new IllegalArgumentException(
                    "Registration ID must be a positive number.");
        }

        String sql = """
                SELECT COUNT(*)
                FROM registrations r
                JOIN events e ON r.event_id = e.event_id
                WHERE r.registration_id = ?
                  AND r.attendance_status = 'Present'
                  AND e.certificate_enabled = TRUE
                  AND e.is_cancelled = FALSE
                  AND TIMESTAMP(e.event_date, e.end_time) < NOW()
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }

                return false;
            }

        } catch (SQLException e) {
            throw databaseException("check certificate eligibility", e);
        }
    }

    // Convert SQL exceptions into understandable messages
    private IllegalStateException databaseException(
            String operation, SQLException e) {

        if (e.getErrorCode() == 1062) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("student_id")) {
                return new IllegalStateException(
                        "This student is already registered for this event.");
            }

            return new IllegalStateException(
                    "This registration ID already exists.");
        }

        if (e.getErrorCode() == 1452) {
            return new IllegalStateException(
                    "The selected student or event does not exist.");
        }

        return new IllegalStateException(
                "Could not " + operation + ". Please try again.", e);
    }
}