
package main.java.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.java.database.DBConnection;
import main.java.model.Student;

public class StudentDAO {

    public boolean addStudent(Student student) {

        String sql = """
                INSERT INTO students (student_id, student_name, email)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, student.getStudentId());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getEmail());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw databaseException("add student", e);
        }
    }

    public boolean updateStudent(Student student) {

        String sql = """
                UPDATE students
                SET student_name = ?, email = ?
                WHERE student_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getStudentName());
            statement.setString(2, student.getEmail());
            statement.setInt(3, student.getStudentId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new IllegalArgumentException(
                        "Student with ID " + student.getStudentId() + " was not found.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("update student", e);
        }
    }

    public boolean deleteStudent(int studentId) {

        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted == 0) {
                throw new IllegalArgumentException(
                        "Student with ID " + studentId + " was not found.");
            }

            return true;

        } catch (SQLException e) {
            throw databaseException("delete student", e);
        }
    }

    public void displayAllStudents() {
        List<Student> students = getAllStudents();

        for (Student student : students) {
            System.out.println(
                    "ID: " + student.getStudentId()
                            + " | Name: " + student.getStudentName()
                            + " | Email: " + student.getEmail());
        }
    }

    public List<Student> getAllStudents() {

        List<Student> students = new ArrayList<>();

        String sql = """
                SELECT student_id, student_name, email
                FROM students
                ORDER BY student_id
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                students.add(new Student(
                        resultSet.getInt("student_id"),
                        resultSet.getString("student_name"),
                        resultSet.getString("email")));
            }

            return students;

        } catch (SQLException e) {
            throw databaseException("retrieve students", e);
        }
    }

    public Student findStudentById(int studentId) {

        String sql = """
                SELECT student_id, student_name, email
                FROM students
                WHERE student_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Student(
                            resultSet.getInt("student_id"),
                            resultSet.getString("student_name"),
                            resultSet.getString("email"));
                }
            }

            return null;

        } catch (SQLException e) {
            throw databaseException("find student", e);
        }
    }

    private IllegalStateException databaseException(
            String operation, SQLException e) {

        if (e.getErrorCode() == 1062) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("email")) {
                return new IllegalStateException(
                        "This email address is already registered.");
            }

            return new IllegalStateException(
                    "A student with this ID already exists.");
        }

        if (e.getErrorCode() == 1451) {
            return new IllegalStateException(
                    "This student cannot be deleted because related records exist.");
        }

        return new IllegalStateException(
                "Could not " + operation + ". Please try again.", e);
    }
}
