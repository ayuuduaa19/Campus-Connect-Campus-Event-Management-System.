
package main.java.manager;

import main.java.dao.EventDAO;
import main.java.model.Event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventManager {

    private final EventDAO eventDAO;

    public EventManager() {
        eventDAO = new EventDAO();
    }

    public boolean addEvent(Event event) {
        validateEvent(event, true);
        return eventDAO.addEvent(event);
    }

    public List<Event> getAllEvents() {
        return eventDAO.getAllEvents();
    }

    public boolean updateEvent(Event event) {
        validateEvent(event, false);
        return eventDAO.updateEvent(event);
    }

    public boolean deleteEvent(int eventId) {

        if (eventId <= 0) {
            throw new IllegalArgumentException(
                    "Event ID must be positive.");
        }

        return eventDAO.deleteEvent(eventId);
    }

    public void displayAllEvents() {
        eventDAO.displayAllEvents();
    }

    public Event findEventById(int eventId) {

        if (eventId <= 0) {
            throw new IllegalArgumentException(
                    "Event ID must be positive.");
        }

        return eventDAO.findEventById(eventId);
    }

    private void validateEvent(Event event, boolean isNew) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Event details cannot be empty.");
        }

        if (isNew && event.getEventId() <= 0) {
            throw new IllegalArgumentException(
                    "Event ID must be positive.");
        }

        if (event.getEventName() == null
                || event.getEventName().isBlank()
                || event.getEventName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Enter a valid event name (maximum 100 characters).");
        }

        if (event.getVenue() == null
                || event.getVenue().isBlank()
                || event.getVenue().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Enter a valid venue (maximum 100 characters).");
        }

        if (event.getCapacity() <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero.");
        }

        if (event.getCategory() == null
                || event.getCategory().isBlank()
                || event.getCategory().length() > 50) {
            throw new IllegalArgumentException(
                    "Please select a valid event category.");
        }

        try {
            LocalDate eventDate = LocalDate.parse(event.getDate());
            LocalDate deadline = LocalDate.parse(event.getRegistrationDeadline());

            LocalTime start = LocalTime.parse(event.getStartTime());
            LocalTime end = LocalTime.parse(event.getEndTime());

            // Past dates are rejected only when creating a new event.
            if (isNew && eventDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "New event date cannot be in the past.");
            }

            if (deadline.isAfter(eventDate)) {
                throw new IllegalArgumentException(
                        "Registration deadline cannot be after the event date.");
            }

            if (!end.isAfter(start)) {
                throw new IllegalArgumentException(
                        "End time must be after start time.");
            }

        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException(
                    "Please enter valid event dates and times.");
        }
    }
}
