
package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import main.java.database.DBConnection;
import main.java.model.User;
import main.java.utils.PasswordUtil;

public class UserDAO {

    public boolean createUser(
            String username,
            String password,
            String role,
            Integer studentId) {

        String sql = """
                INSERT INTO users
                    (username, password_hash, password_salt, role, student_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, salt);
            statement.setString(4, role);

            if (studentId == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, studentId);
            }

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw databaseException("create user account", e);
        }
    }

    public boolean adminExists() {

        String sql = """
                SELECT user_id FROM users
                WHERE role = 'ADMIN'
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next();

        } catch (SQLException e) {
            throw databaseException("check administrator account", e);
        }
    }

    public User findUserByUsername(String username) {

        String sql = """
                SELECT user_id, username, password_hash, password_salt,
                       role, student_id, must_change_password
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw databaseException("find user", e);
        }
    }

    public User findUserById(int userId) {

        String sql = """
                SELECT user_id, username, password_hash, password_salt,
                       role, student_id, must_change_password
                FROM users
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw databaseException("find user by ID", e);
        }
    }

    public boolean usernameExists(String username) {

        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw databaseException("check username", e);
        }
    }

    public boolean updatePassword(int userId, String newPassword) {

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    password_salt = ?,
                    must_change_password = FALSE
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(newPassword, salt);

            statement.setString(1, hash);
            statement.setString(2, salt);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw databaseException("update password", e);
        }
    }

    // Reset a student's password and mark it as temporary.
    public boolean resetStudentPassword(int studentId, String temporaryPassword) {

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    password_salt = ?,
                    must_change_password = TRUE
                WHERE student_id = ?
                  AND role = 'STUDENT'
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(temporaryPassword, salt);

            statement.setString(1, hash);
            statement.setString(2, salt);
            statement.setInt(3, studentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw databaseException("reset student password", e);
        }
    }

    // Returns student ID -> username for students who have login accounts.
    public Map<Integer, String> getStudentAccountUsernames() {

        String sql = """
                SELECT student_id, username
                FROM users
                WHERE role = 'STUDENT'
                  AND student_id IS NOT NULL
                """;

        Map<Integer, String> accounts = new HashMap<>();

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                accounts.put(
                        resultSet.getInt("student_id"),
                        resultSet.getString("username"));
            }

            return accounts;

        } catch (SQLException e) {
            throw databaseException("load student usernames", e);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {

        int studentIdValue = resultSet.getInt("student_id");

        Integer studentId = resultSet.wasNull()
                ? null
                : studentIdValue;

        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_salt"),
                resultSet.getString("role"),
                studentId,
                resultSet.getBoolean("must_change_password"));
    }

    private IllegalStateException databaseException(
            String operation, SQLException e) {

        if (e.getErrorCode() == 1062) {
            return new IllegalStateException(
                    "This username is already taken.");
        }

        if (e.getErrorCode() == 1452) {
            return new IllegalStateException(
                    "The selected student does not exist.");
        }

        return new IllegalStateException(
                "Could not " + operation
                        + ". Please check your database connection and try again.",
                e);
    }
}
