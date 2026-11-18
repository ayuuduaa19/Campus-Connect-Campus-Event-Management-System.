package main.java.model;

public class Student {
    private int studentId;
    private String studentName;
    private String email;

    public Student(int studentId, String studentName, String email) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEmail() {
        return email;
    }

    public void displayDetails() {
        System.out.println("Student ID: " + studentId);
        System.out.println("Name: " + studentName);
        System.out.println("Email: " + email);
    }
}
