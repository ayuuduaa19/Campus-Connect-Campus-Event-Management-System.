
package main.java.model;

public class Registration {

    private int registrationId;
    private Student student;
    private Event event;
    private String attendanceStatus = "Not Marked";

    public Registration(int registrationId, Student student, Event event) {
        this.registrationId = registrationId;
        this.student = student;
        this.event = event;
    }

    // Constructor used when retrieving registrations from the database
    public Registration(
            int registrationId,
            Student student,
            Event event,
            String attendanceStatus) {

        this.registrationId = registrationId;
        this.student = student;
        this.event = event;
        this.attendanceStatus = attendanceStatus;
    }

    public void displayRegistration() {
        System.out.println("Registration ID: " + registrationId);
        System.out.println("Student: " + student.getStudentName());
        System.out.println("Event: " + event.getEventName());
        System.out.println("Attendance: " + attendanceStatus);
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public Student getStudent() {
        return student;
    }

    public Event getEvent() {
        return event;
    }

    public void markAttendance(boolean present) {
        attendanceStatus = present ? "Present" : "Absent";
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}
