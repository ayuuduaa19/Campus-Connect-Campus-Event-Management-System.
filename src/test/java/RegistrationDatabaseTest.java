package test.java;

import main.java.dao.RegistrationDAO;

public class RegistrationDatabaseTest {

    public static void main(String[] args) {

        RegistrationDAO registrationDAO = new RegistrationDAO();

        // Register Aarav for event 1001
        registrationDAO.registerStudent(301, 201, 1001);

        // Try duplicate registration
        registrationDAO.registerStudent(302, 201, 1001);

        // Register Priya for the same event
        registrationDAO.registerStudent(303, 202, 1001);

        // Display registrations
        System.out.println("\nAll Registrations:");
        registrationDAO.displayAllRegistrations();

        // Mark attendance
        System.out.println("\nUpdating Attendance:");
        registrationDAO.markAttendance(301, true);

        // Display updated attendance
        registrationDAO.displayAllRegistrations();
    }
}
