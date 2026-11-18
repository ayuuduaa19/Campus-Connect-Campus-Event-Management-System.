package main.java.gui;

import javafx.scene.Node;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import main.java.dao.EventDAO;
import main.java.dao.StudentDAO;
import main.java.manager.RegistrationManager;
import main.java.model.Event;
import main.java.model.Registration;
import main.java.model.Student;
import main.java.model.User;
import main.java.utils.CertificateGenerator;

public class StudentDashboard extends BorderPane {

        private final User loggedInUser;
        private final int studentId;

        private final EventDAO eventDAO;
        private final StudentDAO studentDAO;
        private final RegistrationManager registrationManager;

        private Student currentStudent;

        private TableView<Registration> registrationTable;

        private TextField searchField;
        private ComboBox<String> categoryFilter;
        private ComboBox<String> statusFilter;

        private GridPane eventCardsGrid;
        private Label eventCountLabel;

        private List<Event> allEvents;

        private final String NAVY = "#142447";
        private final String ROYAL_BLUE = "#315FEA";
        private final String PURPLE = "#7551B8";
        private final String BACKGROUND = "#F3F5FB";

        private final String[] categories = {
                        "Technical", "Cultural", "Sports",
                        "Workshop", "Academic", "Other"
        };

        public StudentDashboard(User user) {

                if (user == null) {
                        throw new IllegalArgumentException("User information is missing.");
                }

                if (user.getStudentId() == null) {
                        throw new IllegalArgumentException(
                                        "This account is not linked to a student record.");
                }

                loggedInUser = user;
                studentId = user.getStudentId();

                eventDAO = new EventDAO();
                studentDAO = new StudentDAO();
                registrationManager = new RegistrationManager();

                setStyle("-fx-background-color: " + BACKGROUND + ";");

                setTop(createHeader());
                setCenter(createTabs());

                loadStudentProfile();
                refreshEvents();
                refreshRegistrations();
        }

        // ---------------- HEADER ----------------

        private HBox createHeader() {

                Label heading = new Label("CAMPUS CONNECT  |  STUDENT PORTAL");

                heading.setStyle(
                                "-fx-font-size: 17px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: white;");

                Label welcome = new Label(
                                "Welcome, " + loggedInUser.getUsername());

                welcome.setStyle("-fx-text-fill: #C9D8F2;");

                Button changePasswordButton = new Button("Change Password");
                changePasswordButton.setOnAction(
                                e -> new ChangePasswordDialog().show(loggedInUser));

                changePasswordButton.setStyle(
                                "-fx-background-color: " + ROYAL_BLUE + ";" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-weight: bold;");

                Button logoutButton = new Button("Logout");

                logoutButton.setStyle(
                                "-fx-background-color: #D9534F;" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-weight: bold;");

                logoutButton.setOnAction(e -> {

                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Logout");
                        confirmation.setHeaderText("Are you sure you want to logout?");

                        confirmation.showAndWait().ifPresent(response -> {

                                if (response == ButtonType.OK) {

                                        Stage stage = (Stage) getScene().getWindow();

                                        stage.setScene(
                                                        new Scene(new LoginView(stage), 1100, 700));

                                        stage.setTitle("Campus Connect | Login");
                                        stage.centerOnScreen();
                                }
                        });
                });

                VBox welcomeBox = new VBox(4, heading, welcome);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox header = new HBox(
                                15, welcomeBox, spacer,
                                changePasswordButton, logoutButton);

                header.setAlignment(Pos.CENTER_LEFT);
                header.setPadding(new Insets(15, 25, 15, 25));
                header.setStyle("-fx-background-color: " + NAVY + ";");

                return header;
        }

        // ---------------- TABS ----------------

        private TabPane createTabs() {

                TabPane tabPane = new TabPane();

                Tab eventsTab = new Tab("Browse Events", createEventsView());

                Tab registrationsTab = new Tab(
                                "My Registrations", createRegistrationsView());

                Tab profileTab = new Tab("My Profile", createProfileView());

                eventsTab.setClosable(false);
                registrationsTab.setClosable(false);
                profileTab.setClosable(false);

                tabPane.getTabs().addAll(
                                eventsTab, registrationsTab, profileTab);

                return tabPane;
        }

        // ---------------- BROWSE EVENTS ----------------

        private VBox createEventsView() {

                VBox content = new VBox(16);
                content.setPadding(new Insets(25));

                Label heading = new Label("Explore Campus Events");

                heading.setStyle(
                                "-fx-font-size: 25px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: " + NAVY + ";");

                Label subtitle = new Label(
                                "Discover events, check their schedules and register.");

                subtitle.setStyle("-fx-text-fill: #64748B;");

                searchField = new TextField();
                searchField.setPromptText("Search by event name or venue...");
                searchField.setPrefWidth(300);

                searchField.textProperty().addListener(
                                (obs, oldValue, newValue) -> applyEventFilters());

                categoryFilter = new ComboBox<>();
                categoryFilter.getItems().add("All Categories");
                categoryFilter.getItems().addAll(categories);
                categoryFilter.setValue("All Categories");
                categoryFilter.setOnAction(e -> applyEventFilters());

                statusFilter = new ComboBox<>();
                statusFilter.getItems().addAll(
                                "All Statuses", "Upcoming",
                                "Ongoing", "Completed", "Cancelled");
                statusFilter.setValue("All Statuses");
                statusFilter.setOnAction(e -> applyEventFilters());

                HBox filters = new HBox(
                                12, searchField, categoryFilter, statusFilter);

                filters.setAlignment(Pos.CENTER_LEFT);

                eventCountLabel = new Label("Loading events...");
                eventCountLabel.setStyle(
                                "-fx-text-fill: #64748B;" +
                                                "-fx-font-weight: bold;");

                // Three-column event grid

                eventCardsGrid = new GridPane();
                eventCardsGrid.setHgap(14);
                eventCardsGrid.setVgap(14);
                eventCardsGrid.setPadding(new Insets(5));

                ColumnConstraints column1 = new ColumnConstraints();
                column1.setPercentWidth(33.333);
                column1.setHgrow(Priority.ALWAYS);

                ColumnConstraints column2 = new ColumnConstraints();
                column2.setPercentWidth(33.333);
                column2.setHgrow(Priority.ALWAYS);

                ColumnConstraints column3 = new ColumnConstraints();
                column3.setPercentWidth(33.334);
                column3.setHgrow(Priority.ALWAYS);

                eventCardsGrid.getColumnConstraints().addAll(
                                column1, column2, column3);

                ScrollPane cardsScrollPane = new ScrollPane(eventCardsGrid);
                cardsScrollPane.setFitToWidth(true);
                cardsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                cardsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                cardsScrollPane.setStyle(
                                "-fx-background: transparent;" +
                                                "-fx-background-color: transparent;");

                VBox.setVgrow(cardsScrollPane, Priority.ALWAYS);

                Button refreshButton = createButton(
                                "Refresh Events", ROYAL_BLUE);

                refreshButton.setOnAction(e -> refreshEvents());

                content.getChildren().addAll(
                                heading, subtitle, filters,
                                eventCountLabel, cardsScrollPane, refreshButton);

                return content;
        }

        // ---------------- EVENT FILTERS ----------------

        private void applyEventFilters() {

                if (allEvents == null || eventCardsGrid == null) {
                        return;
                }

                String search = searchField.getText() == null
                                ? ""
                                : searchField.getText().toLowerCase().trim();

                String category = categoryFilter.getValue();
                String status = statusFilter.getValue();

                List<Event> filtered = allEvents.stream()
                                .filter(event -> event.getEventName().toLowerCase().contains(search)
                                                || event.getVenue().toLowerCase().contains(search))
                                .filter(event -> "All Categories".equals(category)
                                                || category.equals(event.getCategory()))
                                .filter(event -> "All Statuses".equals(status)
                                                || status.equals(event.getStatus()))
                                .toList();

                displayEventCards(filtered);
        }

        // ---------------- DISPLAY EVENT CARDS ----------------

        private void displayEventCards(List<Event> events) {

                eventCardsGrid.getChildren().clear();

                eventCountLabel.setText(events.size() + " event(s) found");

                if (events.isEmpty()) {

                        Label emptyLabel = new Label(
                                        "No events match your search or filters.");

                        emptyLabel.setStyle(
                                        "-fx-font-size: 15px;" +
                                                        "-fx-text-fill: #64748B;" +
                                                        "-fx-padding: 30;");

                        eventCardsGrid.add(emptyLabel, 0, 0);
                        return;
                }

                int column = 0;
                int row = 0;

                for (Event event : events) {

                        VBox card = createEventCard(event);
                        eventCardsGrid.add(card, column, row);

                        column++;

                        if (column == 3) {
                                column = 0;
                                row++;
                        }
                }
        }

        // ---------------- SINGLE EVENT CARD ----------------

        private VBox createEventCard(Event event) {

                VBox card = new VBox(8);
                card.setPadding(new Insets(12));
                card.setMaxWidth(Double.MAX_VALUE);
                card.setMinHeight(0);

                card.setStyle(
                                "-fx-background-color: white;" +
                                                "-fx-background-radius: 14;" +
                                                "-fx-border-color: #E2E8F0;" +
                                                "-fx-border-radius: 14;");

                Node poster = createPoster(event);

                Label name = new Label(event.getEventName());
                name.setWrapText(true);

                name.setStyle(
                                "-fx-font-size: 17px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: " + NAVY + ";");

                Label category = new Label(
                                event.getCategory() == null
                                                ? "Other"
                                                : event.getCategory());

                category.setStyle(
                                "-fx-background-color: #EDE9FE;" +
                                                "-fx-text-fill: " + PURPLE + ";" +
                                                "-fx-padding: 3 8;" +
                                                "-fx-background-radius: 12;" +
                                                "-fx-font-size: 11px;" +
                                                "-fx-font-weight: bold;");

                Label status = new Label(event.getStatus());

                status.setStyle(
                                "-fx-background-color: #EAF2FF;" +
                                                "-fx-text-fill: " + ROYAL_BLUE + ";" +
                                                "-fx-padding: 3 8;" +
                                                "-fx-background-radius: 12;" +
                                                "-fx-font-size: 11px;" +
                                                "-fx-font-weight: bold;");

                HBox tags = new HBox(7, category, status);
                tags.setAlignment(Pos.CENTER_LEFT);

                String descriptionText = event.getDescription() == null
                                || event.getDescription().isBlank()
                                                ? "No event description available."
                                                : event.getDescription();

                Label description = new Label(descriptionText);
                description.setWrapText(true);
                description.setMaxHeight(32);

                description.setStyle(
                                "-fx-font-size: 11px;" +
                                                "-fx-text-fill: #64748B;");

                GridPane details = new GridPane();
                details.setHgap(10);
                details.setVgap(5);

                ColumnConstraints detailColumn1 = new ColumnConstraints();
                detailColumn1.setPercentWidth(50);

                ColumnConstraints detailColumn2 = new ColumnConstraints();
                detailColumn2.setPercentWidth(50);

                details.getColumnConstraints().addAll(
                                detailColumn1, detailColumn2);

                details.add(createDetailLabel("Date: ", event.getDate()), 0, 0);

                details.add(createDetailLabel(
                                "Time: ",
                                formatTime(event.getStartTime())
                                                + " - " + formatTime(event.getEndTime())),
                                1, 0);

                details.add(createDetailLabel("Venue: ", event.getVenue()), 0, 1);

                details.add(createDetailLabel(
                                "Seats: ",
                                String.valueOf(
                                                Math.max(0, event.getAvailableSeats()))),
                                1, 1);

                details.add(createDetailLabel(
                                "Deadline: ", event.getRegistrationDeadline()), 0, 2, 2, 1);

                Button registerButton = createButton("Register Now", "#16805D");
                registerButton.setMaxWidth(Double.MAX_VALUE);

                if (!event.isRegistrationOpen()) {

                        registerButton.setText("Registration Closed");
                        registerButton.setDisable(true);

                        registerButton.setStyle(
                                        "-fx-background-color: #94A3B8;" +
                                                        "-fx-text-fill: white;" +
                                                        "-fx-font-weight: bold;" +
                                                        "-fx-padding: 7 12;" +
                                                        "-fx-background-radius: 8;");
                }

                registerButton.setOnAction(e -> registerForEvent(event));

                card.getChildren().addAll(
                                poster, tags, name, description, details, registerButton);

                return card;
        }

        // ---------------- EVENT POSTER ----------------

        private Node createPoster(Event event) {

                String posterPath = event.getPosterPath();

                if (posterPath != null && !posterPath.isBlank()) {

                        try {

                                File posterFile = new File(posterPath);

                                String imageSource = posterFile.exists()
                                                ? posterFile.toURI().toString()
                                                : posterPath;

                                Image image = new Image(
                                                imageSource, 300, 80, true, true);

                                if (!image.isError()) {

                                        ImageView imageView = new ImageView(image);
                                        imageView.setFitWidth(300);
                                        imageView.setFitHeight(80);
                                        imageView.setPreserveRatio(false);

                                        return imageView;
                                }

                        } catch (Exception ignored) {
                                // Show placeholder if poster cannot be loaded.
                        }
                }

                Label placeholder = new Label("CAMPUS EVENT");
                placeholder.setAlignment(Pos.CENTER);
                placeholder.setMaxWidth(Double.MAX_VALUE);
                placeholder.setPrefHeight(80);

                placeholder.setStyle(
                                "-fx-background-color: linear-gradient(to right, "
                                                + NAVY + ", " + PURPLE + ");" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-size: 16px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-background-radius: 9;");

                return placeholder;
        }

        private Label createDetailLabel(String heading, String value) {

                Label label = new Label(
                                heading + (value == null ? "N/A" : value));

                label.setWrapText(true);

                label.setStyle(
                                "-fx-font-size: 11px;" +
                                                "-fx-text-fill: #475569;");

                return label;
        }

        // ---------------- REGISTER FOR EVENT ----------------

        private void registerForEvent(Event selectedEvent) {

                if (!selectedEvent.isRegistrationOpen()) {

                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "Registration Closed",
                                        "Registration for this event is currently closed.");

                        refreshEvents();
                        return;
                }

                try {

                        List<Registration> existingRegistrations = registrationManager
                                        .getRegistrationsByStudent(studentId);

                        for (Registration registration : existingRegistrations) {

                                if (registration.getEvent().getEventId() == selectedEvent.getEventId()) {

                                        showAlert(
                                                        Alert.AlertType.WARNING,
                                                        "Already Registered",
                                                        "You have already registered for this event.");

                                        return;
                                }
                        }

                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirm Registration");

                        confirmation.setHeaderText(
                                        "Register for " + selectedEvent.getEventName() + "?");

                        confirmation.setContentText(
                                        "Date: " + selectedEvent.getDate()
                                                        + "\nTime: "
                                                        + formatTime(selectedEvent.getStartTime())
                                                        + " - " + formatTime(selectedEvent.getEndTime())
                                                        + "\nVenue: " + selectedEvent.getVenue()
                                                        + "\nRegistration deadline: "
                                                        + selectedEvent.getRegistrationDeadline());

                        confirmation.showAndWait().ifPresent(response -> {

                                if (response == ButtonType.OK) {
                                        completeRegistration(selectedEvent);
                                }
                        });

                } catch (IllegalArgumentException | IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Registration Error",
                                        ex.getMessage());
                }
        }

        private void completeRegistration(Event selectedEvent) {

                try {

                        boolean registered = registrationManager.registerStudentForEvent(
                                        studentId, selectedEvent.getEventId());

                        if (registered) {

                                showAlert(
                                                Alert.AlertType.INFORMATION,
                                                "Registration Successful",
                                                "You have successfully registered for "
                                                                + selectedEvent.getEventName() + ".");

                        } else {

                                showAlert(
                                                Alert.AlertType.ERROR,
                                                "Registration Failed",
                                                "Registration could not be completed.");
                        }

                        refreshEvents();
                        refreshRegistrations();

                } catch (IllegalArgumentException | IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Registration Failed",
                                        ex.getMessage());

                        refreshEvents();
                        refreshRegistrations();
                }
        }

        // ---------------- MY REGISTRATIONS ----------------

        private VBox createRegistrationsView() {

                VBox content = new VBox(16);
                content.setPadding(new Insets(25));

                Label heading = new Label("My Registrations");

                heading.setStyle(
                                "-fx-font-size: 25px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: " + NAVY + ";");

                Label subtitle = new Label(
                                "View your registered events and their schedules.");

                subtitle.setStyle("-fx-text-fill: #64748B;");

                registrationTable = new TableView<>();

                TableColumn<Registration, Integer> idColumn = new TableColumn<>("Registration ID");

                idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                                data.getValue().getRegistrationId()).asObject());

                TableColumn<Registration, String> eventColumn = new TableColumn<>("Event");

                eventColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getEvent().getEventName()));

                TableColumn<Registration, String> venueColumn = new TableColumn<>("Venue");

                venueColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getEvent().getVenue()));

                TableColumn<Registration, String> dateColumn = new TableColumn<>("Date");

                dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getEvent().getDate()));

                dateColumn.setComparator((first, second) -> {
                        try {
                                return LocalDate.parse(first).compareTo(LocalDate.parse(second));
                        } catch (DateTimeParseException ex) {
                                return first.compareTo(second);
                        }
                });

                TableColumn<Registration, String> timeColumn = new TableColumn<>("Time Slot");

                timeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                formatTime(data.getValue().getEvent().getStartTime())
                                                + " - "
                                                + formatTime(data.getValue().getEvent().getEndTime())));

                TableColumn<Registration, String> statusColumn = new TableColumn<>("Attendance");

                statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getAttendanceStatus()));

                registrationTable.getColumns().addAll(
                                idColumn, eventColumn, venueColumn,
                                dateColumn, timeColumn, statusColumn);

                // Sort registrations by event date, earliest first.
                registrationTable.getSortOrder().add(dateColumn);
                dateColumn.setSortType(TableColumn.SortType.ASCENDING);

                registrationTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                registrationTable.setPlaceholder(
                                new Label("You have not registered for any events yet."));

                VBox.setVgrow(registrationTable, Priority.ALWAYS);

                Button cancelButton = createButton(
                                "Cancel Selected Registration", "#C0392B");

                cancelButton.setOnAction(e -> cancelSelectedRegistration());

                Button downloadCertificateButton = createButton(
                                "Download Certificate", PURPLE);

                downloadCertificateButton.setOnAction(
                                e -> downloadSelectedCertificate());

                // Refresh button beside the My Registrations heading
                Button refreshButton = createButton(
                                "Refresh Registrations", ROYAL_BLUE);

                refreshButton.setOnAction(e -> refreshRegistrations());

                HBox headingRow = new HBox(12, heading, refreshButton);
                headingRow.setAlignment(Pos.CENTER_LEFT);

                HBox actionRow = new HBox(
                                12, cancelButton, downloadCertificateButton);

                actionRow.setAlignment(Pos.CENTER_LEFT);

                content.getChildren().addAll(
                                headingRow, subtitle, registrationTable,
                                actionRow);

                return content;
        }

        // ---------------- CANCEL REGISTRATION ----------------

        private void cancelSelectedRegistration() {

                Registration selected = registrationTable.getSelectionModel().getSelectedItem();

                if (selected == null) {

                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Registration Selected",
                                        "Please select a registration first.");

                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Cancel Registration");

                confirmation.setHeaderText(
                                "Cancel " + selected.getEvent().getEventName() + "?");

                confirmation.setContentText(
                                "Are you sure you want to cancel this registration?");

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {
                                completeCancellation(selected);
                        }
                });
        }

        private void completeCancellation(Registration selected) {

                try {

                        boolean cancelled = registrationManager.cancelStudentRegistration(
                                        selected.getRegistrationId(), studentId);

                        if (cancelled) {

                                showAlert(
                                                Alert.AlertType.INFORMATION,
                                                "Registration Cancelled",
                                                "Your registration has been cancelled.");

                        } else {

                                showAlert(
                                                Alert.AlertType.ERROR,
                                                "Cancellation Failed",
                                                "Could not cancel this registration.");
                        }

                        refreshRegistrations();
                        refreshEvents();

                } catch (IllegalArgumentException | IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Cancellation Failed",
                                        ex.getMessage());

                        refreshRegistrations();
                }
        }

        // ---------------- DOWNLOAD CERTIFICATE ----------------

        private void downloadSelectedCertificate() {

                Registration selected = registrationTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(
                                        Alert.AlertType.WARNING,
                                        "No Registration Selected",
                                        "Please select a registration first.");
                        return;
                }

                try {

                        boolean eligible = registrationManager.isCertificateEligible(
                                        selected.getRegistrationId());

                        if (!eligible) {
                                showAlert(
                                                Alert.AlertType.INFORMATION,
                                                "Certificate Not Available",
                                                "Your certificate is not available yet. It requires "
                                                                + "certificates to be enabled for the event, "
                                                                + "the event to be completed, and your attendance "
                                                                + "to be marked Present.");
                                return;
                        }

                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save Participation Certificate");

                        String studentName = selected.getStudent().getStudentName();

                        String eventName = selected.getEvent().getEventName();

                        String fileName = (studentName + "_" + eventName
                                        + "_Certificate")
                                        .replaceAll("[^a-zA-Z0-9._-]", "_");

                        fileChooser.setInitialFileName(fileName + ".pdf");

                        fileChooser.getExtensionFilters().add(
                                        new FileChooser.ExtensionFilter(
                                                        "PDF Files", "*.pdf"));

                        Stage stage = (Stage) getScene().getWindow();

                        File outputFile = fileChooser.showSaveDialog(stage);

                        if (outputFile == null) {
                                return;
                        }

                        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
                                outputFile = new File(
                                                outputFile.getAbsolutePath() + ".pdf");
                        }

                        CertificateGenerator.generateCertificate(
                                        studentName,
                                        eventName,
                                        selected.getEvent().getDate(),
                                        selected.getRegistrationId(),
                                        outputFile);

                        showAlert(
                                        Alert.AlertType.INFORMATION,
                                        "Certificate Downloaded",
                                        "Your participation certificate has been saved to:\n"
                                                        + outputFile.getAbsolutePath());

                } catch (IOException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Certificate Generation Failed",
                                        "Could not generate your certificate. "
                                                        + ex.getMessage());

                } catch (IllegalArgumentException | IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Certificate Error",
                                        ex.getMessage());
                }
        }

        // ---------------- MY PROFILE ----------------

        private VBox createProfileView() {

                VBox content = new VBox(18);
                content.setPadding(new Insets(35));

                Label heading = new Label("My Profile");

                heading.setStyle(
                                "-fx-font-size: 25px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: " + NAVY + ";");

                Label name = new Label("Full Name: Loading...");
                Label email = new Label("Email: Loading...");
                Label id = new Label("Student ID: " + studentId);

                Label username = new Label(
                                "Username: " + loggedInUser.getUsername());

                Label role = new Label("Account Type: Student");

                for (Label label : List.of(name, email, id, username, role)) {

                        label.setStyle(
                                        "-fx-font-size: 16px;" +
                                                        "-fx-text-fill: #52677F;");
                }

                Button refresh = createButton("Refresh Profile", ROYAL_BLUE);

                refresh.setOnAction(e -> {

                        loadStudentProfile();

                        name.setText("Full Name: "
                                        + (currentStudent == null
                                                        ? "Not Available"
                                                        : currentStudent.getStudentName()));

                        email.setText("Email: "
                                        + (currentStudent == null
                                                        ? "Not Available"
                                                        : currentStudent.getEmail()));
                });

                content.getChildren().addAll(
                                heading, name, email, id, username, role, refresh);

                name.setText("Full Name: "
                                + (currentStudent == null
                                                ? "Not Available"
                                                : currentStudent.getStudentName()));

                email.setText("Email: "
                                + (currentStudent == null
                                                ? "Not Available"
                                                : currentStudent.getEmail()));

                return content;
        }

        private void loadStudentProfile() {

                try {

                        currentStudent = studentDAO.findStudentById(studentId);

                } catch (IllegalArgumentException | IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Profile Loading Error",
                                        ex.getMessage());
                }
        }

        // ---------------- REFRESH DATA ----------------

        private void refreshEvents() {

                try {

                        allEvents = eventDAO.getAllEvents();
                        applyEventFilters();

                } catch (IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Events Loading Error",
                                        ex.getMessage());
                }
        }

        private void refreshRegistrations() {

                try {

                        List<Registration> registrations = registrationManager.getRegistrationsByStudent(studentId);

                        registrationTable.setItems(
                                        FXCollections.observableArrayList(registrations));
                        registrationTable.sort();

                } catch (IllegalStateException ex) {

                        showAlert(
                                        Alert.AlertType.ERROR,
                                        "Registrations Loading Error",
                                        ex.getMessage());
                }
        }

        // ---------------- HELPERS ----------------

        private String formatTime(String time) {

                try {

                        return LocalTime.parse(time)
                                        .format(DateTimeFormatter.ofPattern("hh:mm a"));

                } catch (Exception ex) {
                        return time == null ? "N/A" : time;
                }
        }

        private Button createButton(String text, String color) {

                Button button = new Button(text);

                button.setStyle(
                                "-fx-background-color: " + color + ";" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-padding: 7 12;" +
                                                "-fx-background-radius: 8;" +
                                                "-fx-cursor: hand;");

                return button;
        }

        private void showAlert(
                        Alert.AlertType type, String title, String message) {

                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);

                Label messageLabel = new Label(
                                message == null || message.isBlank()
                                                ? "An unexpected error occurred."
                                                : message);

                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(600);

                messageLabel.setStyle(
                                "-fx-font-size: 14px;" +
                                                "-fx-text-fill: #1B365D;" +
                                                "-fx-padding: 10px;");

                alert.getDialogPane().setContent(messageLabel);

                // Increase alert dimensions
                alert.getDialogPane().setMinWidth(650);
                alert.getDialogPane().setPrefWidth(680);
                alert.getDialogPane().setMinHeight(150);

                alert.showAndWait();
        }
}