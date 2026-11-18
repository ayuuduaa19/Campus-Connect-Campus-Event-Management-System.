
import java.util.Scanner;

import main.java.manager.EventManager;
import main.java.manager.StudentManager;
import main.java.manager.RegistrationManager;

import main.java.model.Event;
import main.java.model.Student;

public class Main {

        static Scanner sc = new Scanner(System.in);

        public static void main(String[] args) {

                EventManager eventManager = new EventManager();
                StudentManager studentManager = new StudentManager();
                RegistrationManager registrationManager = new RegistrationManager();

                int choice;

                do {
                        System.out.println("\n========================================");
                        System.out.println("     CAMPUS EVENT MANAGEMENT SYSTEM");
                        System.out.println("========================================");

                        System.out.println("1. Add Event");
                        System.out.println("2. View All Events");
                        System.out.println("3. Search Event");
                        System.out.println("4. Add Student");
                        System.out.println("5. View All Students");
                        System.out.println("6. Register for Event");
                        System.out.println("7. View All Registrations");
                        System.out.println("8. View Registrations by Student");
                        System.out.println("9. Cancel Registration");
                        System.out.println("10. Mark Attendance");
                        System.out.println("11. Exit");

                        System.out.print("\nEnter your choice: ");
                        choice = readInt();

                        switch (choice) {

                                case 1:
                                        System.out.print("Enter Event ID: ");
                                        int eventId = readInt();

                                        System.out.print("Enter Event Name: ");
                                        String eventName = sc.nextLine();

                                        System.out.print("Enter Venue: ");
                                        String venue = sc.nextLine();

                                        System.out.print("Enter Date (YYYY-MM-DD): ");
                                        String date = sc.nextLine();

                                        System.out.print("Enter Capacity: ");
                                        int capacity = readInt();

                                        Event event = new Event(
                                                        eventId, eventName, venue, date, capacity);

                                        eventManager.addEvent(event);
                                        break;

                                case 2:
                                        eventManager.displayAllEvents();
                                        break;

                                case 3:
                                        System.out.print("Enter Event ID to search: ");
                                        int searchEventId = readInt();

                                        Event foundEvent = eventManager.findEventById(searchEventId);

                                        if (foundEvent != null) {
                                                foundEvent.displayDetails();
                                        } else {
                                                System.out.println("Event not found.");
                                        }
                                        break;

                                case 4:
                                        System.out.print("Enter Student ID: ");
                                        int studentId = readInt();

                                        System.out.print("Enter Student Name: ");
                                        String studentName = sc.nextLine();

                                        System.out.print("Enter Email: ");
                                        String email = sc.nextLine();

                                        Student student = new Student(
                                                        studentId, studentName, email);

                                        studentManager.addStudent(student);
                                        break;

                                case 5:
                                        studentManager.displayAllStudents();
                                        break;

                                case 6:
                                        System.out.print("Enter Registration ID: ");
                                        int registrationId = readInt();

                                        System.out.print("Enter Student ID: ");
                                        int registerStudentId = readInt();

                                        System.out.print("Enter Event ID: ");
                                        int registerEventId = readInt();

                                        registrationManager.registerStudent(
                                                        registrationId,
                                                        registerStudentId,
                                                        registerEventId);
                                        break;

                                case 7:
                                        registrationManager.displayAllRegistrations();
                                        break;

                                case 8:
                                        System.out.print("Enter Student ID: ");
                                        int studentSearchId = readInt();

                                        registrationManager.displayRegistrationsByStudent(
                                                        studentSearchId);
                                        break;

                                case 9:
                                        System.out.print("Enter Registration ID to cancel: ");
                                        int cancelId = readInt();

                                        registrationManager.cancelRegistration(cancelId);
                                        break;

                                case 10:
                                        System.out.print("Enter Registration ID: ");
                                        int attendanceId = readInt();

                                        System.out.print("Is the student present? (yes/no): ");
                                        String response = sc.nextLine();

                                        if (response.equalsIgnoreCase("yes")) {
                                                registrationManager.markAttendance(attendanceId, true);
                                        } else if (response.equalsIgnoreCase("no")) {
                                                registrationManager.markAttendance(attendanceId, false);
                                        } else {
                                                System.out.println("Invalid attendance choice.");
                                        }
                                        break;

                                case 11:
                                        System.out.println("Thank you for using Campus Event Management System!");
                                        break;

                                default:
                                        System.out.println("Invalid choice! Please try again.");
                        }

                } while (choice != 11);

                sc.close();
        }

        // Helper method to read integer input
        public static int readInt() {
                while (true) {
                        try {
                                int value = Integer.parseInt(sc.nextLine());
                                return value;
                        } catch (NumberFormatException e) {
                                System.out.print("Please enter a valid number: ");
                        }
                }
        }
}
