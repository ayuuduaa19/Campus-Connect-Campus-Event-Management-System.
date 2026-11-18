package main.java.manager;

import java.util.List;

import main.java.dao.RegistrationDAO;
import main.java.model.Event;
import main.java.model.Student;
import main.java.model.Registration;

public class RegistrationManager {

    private final RegistrationDAO registrationDAO;

    public RegistrationManager() {
        registrationDAO = new RegistrationDAO();
    }

    // Admin methods
    public List<Registration> getAllRegistrations() {
        return registrationDAO.getAllRegistrations();
    }

    public boolean registerStudent(
            int registrationId, Student student, Event event) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Please select a student.");
        }

        if (event == null) {
            throw new IllegalArgumentException(
                    "Please select an event.");
        }

        return registerStudent(
                registrationId,
                student.getStudentId(),
                event.getEventId());
    }

    public boolean registerStudent(
            int registrationId, int studentId, int eventId) {

        validateIds(registrationId, studentId, eventId);

        return registrationDAO.registerStudent(
                registrationId, studentId, eventId);
    }

    public void displayAllRegistrations() {
        registrationDAO.displayAllRegistrations();
    }

    public void displayRegistrationsByStudent(int studentId) {
        validatePositiveId(studentId, "Student ID");
        registrationDAO.displayRegistrationsByStudent(studentId);
    }

    public boolean cancelRegistration(int registrationId) {
        validatePositiveId(registrationId, "Registration ID");
        return registrationDAO.cancelRegistration(registrationId);
    }

    public boolean markAttendance(int registrationId, boolean present) {
        validatePositiveId(registrationId, "Registration ID");
        return registrationDAO.markAttendance(registrationId, present);
    }

    // Bulk attendance for one event
    public int markAttendanceForEvent(int eventId, boolean present) {
        validatePositiveId(eventId, "Event ID");
        return registrationDAO.markAttendanceForEvent(eventId, present);
    }

    // Student-specific methods
    public boolean registerStudentForEvent(int studentId, int eventId) {

        validatePositiveId(studentId, "Student ID");
        validatePositiveId(eventId, "Event ID");

        return registrationDAO.registerStudentForStudent(
                studentId, eventId);
    }

    public List<Registration> getRegistrationsByStudent(int studentId) {
        validatePositiveId(studentId, "Student ID");
        return registrationDAO.getRegistrationsByStudent(studentId);
    }

    public boolean cancelStudentRegistration(
            int registrationId, int studentId) {

        validatePositiveId(registrationId, "Registration ID");
        validatePositiveId(studentId, "Student ID");

        return registrationDAO.cancelStudentRegistration(
                registrationId, studentId);
    }

    // Certificate eligibility check
    public boolean isCertificateEligible(int registrationId) {

        validatePositiveId(registrationId, "Registration ID");

        return registrationDAO.isCertificateEligible(registrationId);
    }

    private void validateIds(
            int registrationId, int studentId, int eventId) {

        validatePositiveId(registrationId, "Registration ID");
        validatePositiveId(studentId, "Student ID");
        validatePositiveId(eventId, "Event ID");
    }

    private void validatePositiveId(int id, String fieldName) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be a positive number.");
        }
    }
}