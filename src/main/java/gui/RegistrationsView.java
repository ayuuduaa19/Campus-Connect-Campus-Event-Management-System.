package main.java.gui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import main.java.manager.RegistrationManager;
import main.java.manager.StudentManager;
import main.java.manager.EventManager;
import main.java.model.Registration;
import main.java.model.Student;
import main.java.model.Event;

public class RegistrationsView extends VBox {

        private final RegistrationManager registrationManager;
        private final StudentManager studentManager;
        private final EventManager eventManager;

        private TableView<Registration> registrationTable;
        private final ObservableList<Registration> registrationData;

        public RegistrationsView() {

                registrationManager = new RegistrationManager();
                studentManager = new StudentManager();
                eventManager = new EventManager();

                registrationData = FXCollections.observableArrayList();

                setSpacing(20);
                setPadding(new Insets(30));
                setStyle("-fx-background-color: #EAF2F8;");

                Label heading = new Label("Registrations Management");
                heading.setStyle(
                                "-fx-font-size: 28px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: #1B365D;");

                Label subtitle = new Label(
                                "Manage event registrations and student attendance");

                subtitle.setStyle(
                                "-fx-font-size: 14px;" +
                                                "-fx-text-fill: #526477;");

                TextField searchField = new TextField();
                searchField.setPromptText(
                                "Search by registration ID, student or event...");
                searchField.setPrefWidth(350);

                Button registerButton = new Button("+ New Registration");
                Button refreshButton = new Button("Refresh");
                Button cancelButton = new Button("Cancel Registration");
                Button presentButton = new Button("Mark Present");
                Button absentButton = new Button("Mark Absent");

                registerButton.setStyle(
                                "-fx-background-color: #462C7D;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 18 10 18;" +
                                                "-fx-background-radius: 8px;");

                refreshButton.setStyle(
                                "-fx-background-color: #075B9B;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 18 10 18;" +
                                                "-fx-background-radius: 8px;");

                cancelButton.setStyle(
                                "-fx-background-color: #B83250;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 18 10 18;" +
                                                "-fx-background-radius: 8px;");

                presentButton.setStyle(
                                "-fx-background-color: #218838;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 18 10 18;" +
                                                "-fx-background-radius: 8px;");

                absentButton.setStyle(
                                "-fx-background-color: #D97706;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-padding: 10 18 10 18;" +
                                                "-fx-background-radius: 8px;");

                registerButton.setOnAction(e -> showRegisterDialog());
                refreshButton.setOnAction(e -> loadRegistrations());
                cancelButton.setOnAction(e -> cancelSelectedRegistration());
                presentButton.setOnAction(e -> updateAttendance(true));
                absentButton.setOnAction(e -> updateAttendance(false));

                HBox topBar = new HBox(
                                12, searchField, registerButton, refreshButton);

                HBox bottomBar = new HBox(
                                12, cancelButton, presentButton, absentButton);

                registrationTable = new TableView<>();

                TableColumn<Registration, Integer> idColumn = new TableColumn<>("Registration ID");

                idColumn.setCellValueFactory(
                                new PropertyValueFactory<>("registrationId"));

                TableColumn<Registration, String> studentColumn = new TableColumn<>("Student");

                studentColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getStudent().getStudentName()));

                TableColumn<Registration, String> eventColumn = new TableColumn<>("Event");

                eventColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getEvent().getEventName()));

                TableColumn<Registration, String> dateColumn = new TableColumn<>("Event Date");

                dateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                                cell.getValue().getEvent().getDate()));

                dateColumn.setComparator((first, second) -> {
                        try {
                                return java.time.LocalDate.parse(first)
                                                .compareTo(java.time.LocalDate.parse(second));
                        } catch (java.time.format.DateTimeParseException ex) {
                                return first.compareTo(second);
                        }
                });

                TableColumn<Registration, String> attendanceColumn = new TableColumn<>("Attendance");

                attendanceColumn.setCellValueFactory(
                                new PropertyValueFactory<>("attendanceStatus"));

                idColumn.setPrefWidth(150);
                studentColumn.setPrefWidth(250);
                eventColumn.setPrefWidth(250);
                dateColumn.setPrefWidth(150);
                attendanceColumn.setPrefWidth(180);

                registrationTable.getColumns().addAll(
                                idColumn,
                                studentColumn,
                                eventColumn,
                                dateColumn,
                                attendanceColumn);

                registrationTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                registrationTable.setPlaceholder(
                                new Label("No registrations found."));

                FilteredList<Registration> filteredRegistrations = new FilteredList<>(registrationData, r -> true);

                searchField.textProperty().addListener(
                                (observable, oldValue, newValue) -> {

                                        String search = newValue.toLowerCase().trim();

                                        filteredRegistrations.setPredicate(registration -> {

                                                if (search.isEmpty()) {
                                                        return true;
                                                }

                                                return String.valueOf(
                                                                registration.getRegistrationId())
                                                                .contains(search)
                                                                || registration.getStudent()
                                                                                .getStudentName()
                                                                                .toLowerCase().contains(search)
                                                                || registration.getEvent()
                                                                                .getEventName()
                                                                                .toLowerCase().contains(search)
                                                                || registration.getAttendanceStatus()
                                                                                .toLowerCase().contains(search);
                                        });
                                });

                registrationTable.setItems(filteredRegistrations);

                // Show registrations in event-date order by default.
                registrationTable.getSortOrder().add(dateColumn);
                dateColumn.setSortType(TableColumn.SortType.ASCENDING);
                registrationTable.sort();

                VBox.setVgrow(registrationTable, Priority.ALWAYS);

                getChildren().addAll(
                                heading,
                                subtitle,
                                topBar,
                                registrationTable,
                                bottomBar);

                loadRegistrations();
        }

        // ---------------- LOAD REGISTRATIONS ----------------

        private void loadRegistrations() {

                try {
                        List<Registration> registrations = registrationManager.getAllRegistrations();

                        registrationData.setAll(registrations);

                } catch (IllegalStateException e) {
                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Database Error",
                                        e.getMessage());
                }
        }

        // ---------------- NEW REGISTRATION ----------------

        private void showRegisterDialog() {

                List<Student> students;
                List<Event> events;

                try {
                        students = studentManager.getAllStudents();
                        events = eventManager.getAllEvents();

                } catch (IllegalStateException e) {
                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Database Error",
                                        e.getMessage());
                        return;
                }

                if (students.isEmpty()) {
                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Students Available",
                                        "Add a student before creating a registration.");
                        return;
                }

                if (events.isEmpty()) {
                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Events Available",
                                        "Add an event before creating a registration.");
                        return;
                }

                Dialog<ButtonType> dialog = new Dialog<>();

                dialog.setTitle("New Registration");
                dialog.setHeaderText("Select a student and an event");

                ButtonType saveButton = new ButtonType(
                                "Register", ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                saveButton, ButtonType.CANCEL);

                TextField registrationIdField = new TextField();
                registrationIdField.setPromptText("Registration ID");

                ComboBox<Student> studentBox = new ComboBox<>();
                studentBox.getItems().setAll(students);
                studentBox.setPromptText("Select student");
                studentBox.setMaxWidth(Double.MAX_VALUE);

                studentBox.setConverter(new StringConverter<Student>() {
                        @Override
                        public String toString(Student student) {
                                if (student == null)
                                        return "";

                                return student.getStudentName()
                                                + " (ID: " + student.getStudentId() + ")";
                        }

                        @Override
                        public Student fromString(String string) {
                                return null;
                        }
                });

                ComboBox<Event> eventBox = new ComboBox<>();
                eventBox.getItems().setAll(events);
                eventBox.setPromptText("Select event");
                eventBox.setMaxWidth(Double.MAX_VALUE);

                eventBox.setConverter(new StringConverter<Event>() {
                        @Override
                        public String toString(Event event) {
                                if (event == null)
                                        return "";

                                return event.getEventName()
                                                + " (ID: " + event.getEventId() + ")";
                        }

                        @Override
                        public Event fromString(String string) {
                                return null;
                        }
                });

                VBox form = new VBox(
                                15,
                                new Label("Registration ID:"),
                                registrationIdField,
                                new Label("Student:"),
                                studentBox,
                                new Label("Event:"),
                                eventBox);

                form.setPadding(new Insets(20));
                dialog.getDialogPane().setContent(form);

                Button saveButtonNode = (Button) dialog.getDialogPane()
                                .lookupButton(saveButton);

                saveButtonNode.addEventFilter(
                                ActionEvent.ACTION,
                                event -> {

                                        String idText = registrationIdField.getText().trim();

                                        if (!idText.matches("\\d+")) {
                                                showAlert(
                                                                Alert.AlertType.WARNING,
                                                                "Invalid Registration ID",
                                                                "Registration ID must contain numbers only.");
                                                event.consume();
                                                return;
                                        }

                                        try {
                                                if (Integer.parseInt(idText) <= 0) {
                                                        showAlert(
                                                                        Alert.AlertType.WARNING,
                                                                        "Invalid Registration ID",
                                                                        "Registration ID must be positive.");
                                                        event.consume();
                                                        return;
                                                }
                                        } catch (NumberFormatException ex) {
                                                showAlert(
                                                                Alert.AlertType.WARNING,
                                                                "Invalid Registration ID",
                                                                "Registration ID is too large.");
                                                event.consume();
                                                return;
                                        }

                                        if (studentBox.getValue() == null) {
                                                showAlert(
                                                                Alert.AlertType.WARNING,
                                                                "Student Required",
                                                                "Please select a student.");
                                                event.consume();
                                                return;
                                        }

                                        if (eventBox.getValue() == null) {
                                                showAlert(
                                                                Alert.AlertType.WARNING,
                                                                "Event Required",
                                                                "Please select an event.");
                                                event.consume();
                                        }
                                });

                dialog.setResultConverter(button -> button);

                dialog.showAndWait().ifPresent(result -> {

                        if (result == saveButton) {

                                try {
                                        int registrationId = Integer.parseInt(
                                                        registrationIdField.getText().trim());

                                        registrationManager.registerStudent(
                                                        registrationId,
                                                        studentBox.getValue(),
                                                        eventBox.getValue());

                                        showAlert(
                                                        Alert.AlertType.INFORMATION,
                                                        "Success",
                                                        "Student registered successfully!");

                                        loadRegistrations();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(
                                                        Alert.AlertType.ERROR,
                                                        "Registration Failed",
                                                        e.getMessage());
                                }
                        }
                });
        }

        // ---------------- CANCEL REGISTRATION ----------------

        private void cancelSelectedRegistration() {

                Registration selected = registrationTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Selection",
                                        "Please select a registration first.");
                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Cancel Registration");
                confirmation.setHeaderText("Cancel this registration?");
                confirmation.setContentText(
                                "Registration ID: " + selected.getRegistrationId());

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {

                                try {
                                        registrationManager.cancelRegistration(
                                                        selected.getRegistrationId());

                                        showAlert(
                                                        Alert.AlertType.INFORMATION,
                                                        "Success",
                                                        "Registration cancelled successfully!");

                                        loadRegistrations();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(
                                                        Alert.AlertType.ERROR,
                                                        "Cancellation Failed",
                                                        e.getMessage());
                                }
                        }
                });
        }

        // ---------------- UPDATE ATTENDANCE ----------------

        private void updateAttendance(boolean present) {

                Registration selected = registrationTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Selection",
                                        "Please select a registration first.");
                        return;
                }

                try {
                        registrationManager.markAttendance(
                                        selected.getRegistrationId(), present);

                        showAlert(
                                        Alert.AlertType.INFORMATION,
                                        "Attendance Updated",
                                        "Attendance marked as "
                                                        + (present ? "Present" : "Absent") + ".");

                        loadRegistrations();

                } catch (IllegalArgumentException | IllegalStateException e) {
                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Attendance Update Failed",
                                        e.getMessage());
                }
        }

        // ---------------- ALERTS ----------------

        private void showAlert(
                        Alert.AlertType type,
                        String title,
                        String message) {

                Alert alert = new Alert(type);

                alert.setTitle(title);
                alert.setHeaderText(null);

                Label messageLabel = new Label(message);
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(480);
                messageLabel.setStyle(
                                "-fx-font-size: 14px;" +
                                                "-fx-text-fill: #1B365D;");

                alert.getDialogPane().setContent(messageLabel);
                alert.getDialogPane().setMinWidth(550);
                alert.getDialogPane().setPrefWidth(570);

                alert.showAndWait();
        }
}