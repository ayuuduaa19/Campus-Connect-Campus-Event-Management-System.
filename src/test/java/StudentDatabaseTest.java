package test.java;

import main.java.dao.StudentDAO;
import main.java.model.Student;

public class StudentDatabaseTest {

    public static void main(String[] args) {

        StudentDAO studentDAO = new StudentDAO();

        Student student1 = new Student(
                201,
                "Aarav Sharma",
                "aarav201@example.com");

        Student student2 = new Student(
                202,
                "Priya Sharma",
                "priya202@example.com");

        studentDAO.addStudent(student1);
        studentDAO.addStudent(student2);

        System.out.println("\nStudents stored in MySQL:");

        studentDAO.displayAllStudents();
    }
}
