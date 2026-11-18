package test.java;

import main.java.dao.EventDAO;
import main.java.model.Event;

public class EventDatabaseTest {

    public static void main(String[] args) {
        EventDAO eventDAO = new EventDAO();

        Event event = new Event(
                1001,
                "Database Test Event",
                "Auditorium",
                "2026-11-25",
                100);

        if (eventDAO.addEvent(event)) {
            System.out.println("\nEvents stored in MySQL:");
            eventDAO.displayAllEvents();
        }
    }
}
