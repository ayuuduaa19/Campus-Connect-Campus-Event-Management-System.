
package main.java.manager;

import java.util.List;
import java.util.regex.Pattern;

import main.java.dao.StudentDAO;
import main.java.model.Student;

public class StudentManager {

    private final StudentDAO studentDAO;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public StudentManager() {
        studentDAO = new StudentDAO();
    }

    public boolean addStudent(Student student) {
        validateStudent(student);

        if (student.getStudentId() <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be a positive number.");
        }

        return studentDAO.addStudent(student);
    }

    public boolean updateStudent(Student student) {
        validateStudent(student);

        if (student.getStudentId() <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be a positive number.");
        }

        return studentDAO.updateStudent(student);
    }

    public boolean deleteStudent(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be a positive number.");
        }

        return studentDAO.deleteStudent(studentId);
    }

    public Student findStudentById(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be a positive number.");
        }

        return studentDAO.findStudentById(studentId);
    }

    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }

    public void displayAllStudents() {
        studentDAO.displayAllStudents();
    }

    private void validateStudent(Student student) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student details cannot be empty.");
        }

        String name = student.getStudentName();
        String email = student.getEmail();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Student name cannot be empty.");
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Student name cannot exceed 100 characters.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Email address cannot be empty.");
        }

        if (email.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Email address cannot exceed 150 characters.");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address.");
        }
    }
}
