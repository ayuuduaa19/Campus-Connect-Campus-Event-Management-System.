
package main.java.gui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import main.java.manager.RegistrationManager;
import main.java.manager.EventManager;
import main.java.model.Registration;
import main.java.model.Event;

public class AttendanceView extends VBox {

        private final RegistrationManager registrationManager;
        private final EventManager eventManager;

        private final TableView<Registration> attendanceTable;
        private final ObservableList<Registration> attendanceData;
        private final FilteredList<Registration> filteredAttendance;

        private ComboBox<Event> eventFilter;

        private Label totalLabel;
        private Label presentLabel;
        private Label absentLabel;
        private Label pendingLabel;

        private TextField searchField;

        public AttendanceView() {

                registrationManager = new RegistrationManager();
                eventManager = new EventManager();

                attendanceData = FXCollections.observableArrayList();

                attendanceTable = new TableView<>();
                filteredAttendance = new FilteredList<>(attendanceData, r -> true);

                setSpacing(20);
                setPadding(new Insets(30));
                setStyle("-fx-background-color: #EAF2F8;");

                Label heading = new Label("Event Participants & Attendance");
                heading.setStyle(
                                "-fx-font-size: 28px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: #1B365D;");

                Label subtitle = new Label(
                                "View registered participants and manage event attendance");
                subtitle.setStyle(
                                "-fx-font-size: 14px;" +
                                                "-fx-text-fill: #526477;");

                totalLabel = new Label("Total: 0");
                presentLabel = new Label("Present: 0");
                absentLabel = new Label("Absent: 0");
                pendingLabel = new Label("Not Marked: 0");

                HBox statistics = new HBox(
                                25, totalLabel, presentLabel, absentLabel, pendingLabel);

                statistics.setPadding(new Insets(15));
                statistics.setStyle(
                                "-fx-background-color: white;" +
                                                "-fx-background-radius: 10px;" +
                                                "-fx-font-size: 15px;" +
                                                "-fx-font-weight: bold;");

                eventFilter = new ComboBox<>();
                eventFilter.setPromptText("Select an event");
                eventFilter.setPrefWidth(250);

                eventFilter.setConverter(new StringConverter<Event>() {
                        @Override
                        public String toString(Event event) {
                                return event == null ? "" : event.getEventName();
                        }

                        @Override
                        public Event fromString(String value) {
                                return null;
                        }
                });

                eventFilter.setOnAction(e -> applyFilters());

                searchField = new TextField();
                searchField.setPromptText(
                                "Search name, email or registration ID...");
                searchField.setPrefWidth(300);

                searchField.textProperty().addListener(
                                (obs, oldValue, newValue) -> applyFilters());

                Button refreshButton = new Button("Refresh");
                refreshButton.setOnAction(e -> refreshData());

                HBox filterBar = new HBox(
                                12, eventFilter, searchField, refreshButton);

                // Participant table
                TableColumn<Registration, Integer> registrationIdColumn = new TableColumn<>("Registration ID");
                registrationIdColumn.setCellValueFactory(
                                new PropertyValueFactory<>("registrationId"));

                TableColumn<Registration, Integer> studentIdColumn = new TableColumn<>("Student ID");
                studentIdColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                                cell.getValue().getStudent().getStudentId()));

                TableColumn<Registration, String> studentColumn = new TableColumn<>("Student Name");
                studentColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getStudent().getStudentName()));

                TableColumn<Registration, String> emailColumn = new TableColumn<>("Email");
                emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getStudent().getEmail()));

                TableColumn<Registration, String> eventColumn = new TableColumn<>("Event");
                eventColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getEvent().getEventName()));

                TableColumn<Registration, String> statusColumn = new TableColumn<>("Attendance Status");
                statusColumn.setCellValueFactory(
                                new PropertyValueFactory<>("attendanceStatus"));

                attendanceTable.getColumns().addAll(
                                registrationIdColumn,
                                studentIdColumn,
                                studentColumn,
                                emailColumn,
                                eventColumn,
                                statusColumn);

                attendanceTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                attendanceTable.setPlaceholder(
                                new Label("No registered participants found."));

                attendanceTable.setItems(filteredAttendance);

                filteredAttendance.addListener(
                                (javafx.collections.ListChangeListener<Registration>) change -> updateStatistics());

                // Individual attendance buttons
                Button presentButton = new Button("Mark Present");
                Button absentButton = new Button("Mark Absent");

                presentButton.setStyle(
                                "-fx-background-color: #218838;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 20 10 20;");

                absentButton.setStyle(
                                "-fx-background-color: #D97706;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 20 10 20;");

                presentButton.setOnAction(e -> updateAttendance(true));
                absentButton.setOnAction(e -> updateAttendance(false));

                HBox individualActions = new HBox(
                                12, presentButton, absentButton);

                // Bulk attendance buttons
                Button allPresentButton = new Button("Mark All Present");
                Button allAbsentButton = new Button("Mark All Absent");

                allPresentButton.setStyle(
                                "-fx-background-color: #166534;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 20 10 20;");

                allAbsentButton.setStyle(
                                "-fx-background-color: #B45309;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 20 10 20;");

                allPresentButton.setOnAction(e -> updateAllAttendance(true));
                allAbsentButton.setOnAction(e -> updateAllAttendance(false));

                HBox bulkActions = new HBox(
                                12, allPresentButton, allAbsentButton);

                Label individualLabel = new Label("Individual Attendance");
                individualLabel.setStyle("-fx-font-weight: bold;");

                Label bulkLabel = new Label("Bulk Attendance for Selected Event");
                bulkLabel.setStyle("-fx-font-weight: bold;");

                VBox.setVgrow(attendanceTable, Priority.ALWAYS);

                getChildren().addAll(
                                heading,
                                subtitle,
                                statistics,
                                filterBar,
                                attendanceTable,
                                individualLabel,
                                individualActions,
                                bulkLabel,
                                bulkActions);

                loadEvents();
                loadAttendance();
        }

        private void loadEvents() {

                try {
                        List<Event> events = eventManager.getAllEvents();

                        eventFilter.getItems().setAll(events);
                        eventFilter.getSelectionModel().clearSelection();

                } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Database Error", e.getMessage());
                }
        }

        private void loadAttendance() {

                try {
                        List<Registration> records = registrationManager.getAllRegistrations();

                        attendanceData.setAll(records);
                        applyFilters();

                } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Database Error", e.getMessage());

                } catch (RuntimeException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Error", "Could not load participant records.");
                }
        }

        private void refreshData() {
                loadEvents();
                loadAttendance();
        }

        private void applyFilters() {

                Event selectedEvent = eventFilter.getValue();

                String search = searchField.getText() == null
                                ? ""
                                : searchField.getText().toLowerCase().trim();

                filteredAttendance.setPredicate(registration -> {

                        boolean matchesEvent = selectedEvent == null
                                        || registration.getEvent().getEventId() == selectedEvent.getEventId();

                        boolean matchesSearch = String.valueOf(registration.getRegistrationId())
                                        .contains(search)
                                        || String.valueOf(
                                                        registration.getStudent().getStudentId())
                                                        .contains(search)
                                        || registration.getStudent().getStudentName()
                                                        .toLowerCase().contains(search)
                                        || registration.getStudent().getEmail()
                                                        .toLowerCase().contains(search);

                        return matchesEvent && matchesSearch;
                });

                updateStatistics();
        }

        private void updateStatistics() {

                int total = filteredAttendance.size();
                int present = 0;
                int absent = 0;
                int pending = 0;

                for (Registration registration : filteredAttendance) {

                        String status = registration.getAttendanceStatus();

                        if ("Present".equalsIgnoreCase(status)) {
                                present++;
                        } else if ("Absent".equalsIgnoreCase(status)) {
                                absent++;
                        } else {
                                pending++;
                        }
                }

                totalLabel.setText("Total: " + total);
                presentLabel.setText("Present: " + present);
                absentLabel.setText("Absent: " + absent);
                pendingLabel.setText("Not Marked: " + pending);
        }

        private void updateAttendance(boolean present) {

                Registration selected = attendanceTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Selection",
                                        "Please select a participant first.");
                        return;
                }

                try {
                        boolean updated = registrationManager.markAttendance(
                                        selected.getRegistrationId(), present);

                        if (updated) {
                                showAlert(Alert.AlertType.INFORMATION,
                                                "Attendance Updated",
                                                "Attendance marked as "
                                                                + (present ? "Present" : "Absent") + ".");

                                loadAttendance();

                        } else {
                                showAlert(Alert.AlertType.ERROR,
                                                "Update Failed",
                                                "Could not update attendance.");
                        }

                } catch (IllegalArgumentException | IllegalStateException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Update Failed", e.getMessage());

                } catch (RuntimeException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Error",
                                        "Something went wrong while updating attendance.");
                }
        }

        private void updateAllAttendance(boolean present) {

                Event selectedEvent = eventFilter.getValue();

                if (selectedEvent == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "Select an Event",
                                        "Please select an event before using bulk attendance.");
                        return;
                }

                String status = present ? "Present" : "Absent";

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Bulk Attendance");
                confirmation.setHeaderText(
                                "Mark all participants as " + status + "?");
                confirmation.setContentText(
                                "This will update every registered participant for "
                                                + selectedEvent.getEventName() + ".");

                confirmation.showAndWait().ifPresent(response -> {

                        if (response != ButtonType.OK) {
                                return;
                        }

                        try {
                                int updated = registrationManager.markAttendanceForEvent(
                                                selectedEvent.getEventId(), present);

                                if (updated == 0) {
                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "No Participants",
                                                        "There are no registered participants for this event.");
                                } else {
                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Attendance Updated",
                                                        updated + " participant(s) marked as " + status + ".");
                                }

                                loadAttendance();

                        } catch (IllegalArgumentException | IllegalStateException e) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Update Failed", e.getMessage());

                        } catch (RuntimeException e) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Error",
                                                "Something went wrong while updating attendance.");
                        }
                });
        }

        private void showAlert(
                        Alert.AlertType type,
                        String title,
                        String message) {

                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }
}
