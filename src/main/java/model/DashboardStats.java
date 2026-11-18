
package main.java.model;

public class DashboardStats {

    private final int totalEvents;
    private final int totalStudents;
    private final int totalRegistrations;
    private final int availableSeats;
    private final int presentCount;
    private final int absentCount;
    private final int notMarkedCount;

    public DashboardStats(
            int totalEvents,
            int totalStudents,
            int totalRegistrations,
            int availableSeats) {

        this(totalEvents, totalStudents, totalRegistrations,
                availableSeats, 0, 0, 0);
    }

    public DashboardStats(
            int totalEvents,
            int totalStudents,
            int totalRegistrations,
            int availableSeats,
            int presentCount,
            int absentCount,
            int notMarkedCount) {

        this.totalEvents = totalEvents;
        this.totalStudents = totalStudents;
        this.totalRegistrations = totalRegistrations;
        this.availableSeats = availableSeats;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.notMarkedCount = notMarkedCount;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getTotalRegistrations() {
        return totalRegistrations;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public int getNotMarkedCount() {
        return notMarkedCount;
    }
}
