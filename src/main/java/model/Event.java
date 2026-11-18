
package main.java.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Event {

    private int eventId;
    private String eventName;
    private String venue;
    private String date;
    private int capacity;
    private int registeredCount;

    private String category;
    private String description;
    private String startTime;
    private String endTime;
    private String registrationDeadline;
    private String posterPath;
    private boolean cancelled;
    private boolean certificateEnabled;

    public Event(int eventId, String eventName, String venue,
            String date, int capacity) {

        this(eventId, eventName, venue, date, capacity, 0);
    }

    public Event(int eventId, String eventName, String venue,
            String date, int capacity, int registeredCount) {

        this(eventId, eventName, venue, date, capacity,
                registeredCount, "Other", "",
                "09:00", "17:00", date, null, false, false);
    }

    // Original complete constructor retained for compatibility
    public Event(int eventId, String eventName, String venue,
            String date, int capacity, int registeredCount,
            String category, String description,
            String startTime, String endTime,
            String registrationDeadline, String posterPath,
            boolean cancelled) {

        this(eventId, eventName, venue, date, capacity,
                registeredCount, category, description,
                startTime, endTime, registrationDeadline,
                posterPath, cancelled, false);
    }

    // New complete constructor with certificate eligibility
    public Event(int eventId, String eventName, String venue,
            String date, int capacity, int registeredCount,
            String category, String description,
            String startTime, String endTime,
            String registrationDeadline, String posterPath,
            boolean cancelled, boolean certificateEnabled) {

        this.eventId = eventId;
        this.eventName = eventName;
        this.venue = venue;
        this.date = date;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
        this.category = category;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.registrationDeadline = registrationDeadline;
        this.posterPath = posterPath;
        this.cancelled = cancelled;
        this.certificateEnabled = certificateEnabled;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getVenue() {
        return venue;
    }

    public String getDate() {
        return date;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isCertificateEnabled() {
        return certificateEnabled;
    }

    public void setCertificateEnabled(boolean certificateEnabled) {
        this.certificateEnabled = certificateEnabled;
    }

    public int getAvailableSeats() {
        return capacity - registeredCount;
    }

    public String getStatus() {

        if (cancelled) {
            return "Cancelled";
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = LocalDate.parse(date)
                .atTime(LocalTime.parse(startTime));

        LocalDateTime end = LocalDate.parse(date)
                .atTime(LocalTime.parse(endTime));

        if (now.isBefore(start)) {
            return "Upcoming";
        }

        if (!now.isAfter(end)) {
            return "Ongoing";
        }

        return "Completed";
    }

    public boolean isRegistrationOpen() {

        if (cancelled || getStatus().equals("Completed")) {
            return false;
        }

        LocalDate deadline = LocalDate.parse(registrationDeadline);

        return !LocalDate.now().isAfter(deadline)
                && registeredCount < capacity;
    }

    public boolean registerParticipant() {
        if (registeredCount < capacity) {
            registeredCount++;
            return true;
        }
        return false;
    }

    public void displayDetails() {
        System.out.println("Event: " + eventName);
        System.out.println("Venue: " + venue);
        System.out.println("Date: " + date);
        System.out.println("Time: " + startTime + " - " + endTime);
        System.out.println("Category: " + category);
        System.out.println("Status: " + getStatus());
        System.out.println("Capacity: " + capacity);
        System.out.println("Registered: " + registeredCount);
        System.out.println("Certificates Enabled: " + certificateEnabled);
    }
}
