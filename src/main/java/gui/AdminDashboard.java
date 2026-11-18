
package main.java.gui;

import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import main.java.dao.DashboardOverviewDAO;
import main.java.dao.DashboardStatsDAO;
import main.java.model.DashboardStats;
import main.java.model.Event;
import main.java.model.Registration;
import main.java.model.User;

public class AdminDashboard extends BorderPane {

        private final User loggedInUser;
        private final DashboardOverviewDAO overviewDAO;
        private final DashboardStatsDAO statsDAO;

        private Button dashboardBtn;
        private Button eventsBtn;
        private Button studentsBtn;
        private Button registrationsBtn;
        private Button attendanceBtn;
        private Button activeNavButton;

        private VBox dashboardContent;

        private Label eventsValue;
        private Label studentsValue;
        private Label registrationsValue;
        private Label seatsValue;

        private PieChart attendanceChart;
        private BarChart<String, Number> categoryChart;

        private VBox upcomingBox;
        private VBox recentBox;

        private final String NAVY = "#142447";
        private final String ROYAL_BLUE = "#315FEA";
        private final String PURPLE = "#7551B8";
        private final String BACKGROUND = "#F3F5FB";

        public AdminDashboard(User user) {

                if (user == null) {
                        throw new IllegalArgumentException("User information is missing.");
                }

                loggedInUser = user;
                overviewDAO = new DashboardOverviewDAO();
                statsDAO = new DashboardStatsDAO();

                setStyle("-fx-background-color: " + BACKGROUND + ";");

                setTop(createTopBar());
                setLeft(createSidebar());
                showDashboard();
        }

        private HBox createTopBar() {

                Label title = new Label("CAMPUS CONNECT  |  ADMIN PANEL");
                title.setStyle(
                                "-fx-font-size: 17px; -fx-font-weight: bold;"
                                                + "-fx-text-fill: white;");

                Button passwordButton = new Button("Change Password");
                passwordButton.setOnAction(
                                e -> new ChangePasswordDialog().show(loggedInUser));

                Button logoutButton = new Button("Logout");
                logoutButton.setStyle(
                                "-fx-background-color: #D9534F;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;");

                logoutButton.setOnAction(e -> {

                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Logout");
                        confirmation.setHeaderText("Are you sure you want to logout?");
                        confirmation.setContentText(
                                        "You will return to the login screen.");

                        confirmation.showAndWait().ifPresent(response -> {
                                if (response == javafx.scene.control.ButtonType.OK) {
                                        Stage stage = (Stage) getScene().getWindow();
                                        stage.setScene(new Scene(new LoginView(stage), 1100, 700));
                                        stage.setTitle("Campus Connect | Login");
                                        stage.centerOnScreen();
                                }
                        });
                });

                passwordButton.setStyle(
                                "-fx-background-color: " + ROYAL_BLUE + ";"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox header = new HBox(
                                15, title, spacer, passwordButton, logoutButton);

                header.setAlignment(Pos.CENTER_LEFT);
                header.setPadding(new Insets(15, 25, 15, 25));
                header.setStyle("-fx-background-color: " + NAVY + ";");

                return header;
        }

        private VBox createSidebar() {

                VBox sidebar = new VBox(15);
                sidebar.setPadding(new Insets(30, 18, 25, 18));
                sidebar.setPrefWidth(225);
                sidebar.setStyle("-fx-background-color: " + NAVY + ";");

                Label logo = new Label("CAMPUS\nCONNECT");
                logo.setStyle(
                                "-fx-font-size: 24px;"
                                                + "-fx-font-weight: bold;"
                                                + "-fx-text-fill: white;");

                Label subtitle = new Label("EVENT MANAGEMENT");
                subtitle.setStyle(
                                "-fx-font-size: 10px;"
                                                + "-fx-text-fill: #A0DFEA;"
                                                + "-fx-letter-spacing: 2px;");

                dashboardBtn = createNavButton("Dashboard");
                eventsBtn = createNavButton("Events");
                studentsBtn = createNavButton("Students");
                registrationsBtn = createNavButton("Registrations");
                attendanceBtn = createNavButton("Attendance");

                dashboardBtn.setOnAction(e -> showDashboard());
                eventsBtn.setOnAction(e -> navigateTo(eventsBtn, new EventsView()));
                studentsBtn.setOnAction(e -> navigateTo(studentsBtn, new StudentsView()));
                registrationsBtn.setOnAction(e -> navigateTo(registrationsBtn, new RegistrationsView()));
                attendanceBtn.setOnAction(e -> navigateTo(attendanceBtn, new AttendanceView()));

                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);

                Label footer = new Label("Campus Event Management System");
                footer.setWrapText(true);
                footer.setStyle("-fx-text-fill: #A0DFEA; -fx-font-size: 11px;");

                sidebar.getChildren().addAll(
                                logo, subtitle,
                                dashboardBtn, eventsBtn, studentsBtn,
                                registrationsBtn, attendanceBtn,
                                spacer, footer);

                setActiveNavButton(dashboardBtn);

                return sidebar;
        }

        private Button createNavButton(String text) {

                Button button = new Button(text);
                button.setMaxWidth(Double.MAX_VALUE);
                button.setAlignment(Pos.CENTER_LEFT);
                button.setPadding(new Insets(12, 15, 12, 15));
                button.setStyle(normalNavStyle());

                button.setOnMouseEntered(e -> {
                        if (button != activeNavButton) {
                                button.setStyle(hoverNavStyle());
                        }
                });

                button.setOnMouseExited(e -> {
                        if (button != activeNavButton) {
                                button.setStyle(normalNavStyle());
                        }
                });

                return button;
        }

        private String normalNavStyle() {
                return "-fx-background-color: transparent;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-size: 14px;"
                                + "-fx-background-radius: 8;"
                                + "-fx-cursor: hand;";
        }

        private String hoverNavStyle() {
                return "-fx-background-color: #075B9B;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-size: 14px;"
                                + "-fx-background-radius: 8;"
                                + "-fx-cursor: hand;";
        }

        private String activeNavStyle() {
                return "-fx-background-color: " + ROYAL_BLUE + ";"
                                + "-fx-text-fill: white;"
                                + "-fx-font-size: 14px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-background-radius: 8;"
                                + "-fx-cursor: hand;";
        }

        private void setActiveNavButton(Button selected) {

                if (activeNavButton != null) {
                        activeNavButton.setStyle(normalNavStyle());
                }

                activeNavButton = selected;
                activeNavButton.setStyle(activeNavStyle());
        }

        private void navigateTo(Button button, Node page) {
                setActiveNavButton(button);
                setCenter(page);
        }

        private void showDashboard() {

                setActiveNavButton(dashboardBtn);

                dashboardContent = new VBox(22);
                dashboardContent.setPadding(new Insets(30));
                dashboardContent.setStyle(
                                "-fx-background-color: " + BACKGROUND + ";");

                Label heading = new Label("Dashboard Overview");
                heading.setStyle(
                                "-fx-font-size: 27px;"
                                                + "-fx-font-weight: bold;"
                                                + "-fx-text-fill: " + NAVY + ";");

                Label subtitle = new Label(
                                "Monitor your campus events and activity at a glance.");
                subtitle.setStyle("-fx-text-fill: #64748B;");

                GridPane statsGrid = new GridPane();
                statsGrid.setHgap(15);
                statsGrid.setVgap(15);

                eventsValue = new Label("...");
                studentsValue = new Label("...");
                registrationsValue = new Label("...");
                seatsValue = new Label("...");

                statsGrid.add(createStatCard(
                                "Total Events", eventsValue, ROYAL_BLUE), 0, 0);
                statsGrid.add(createStatCard(
                                "Total Students", studentsValue, PURPLE), 1, 0);
                statsGrid.add(createStatCard(
                                "Registrations", registrationsValue, "#A34C9B"), 2, 0);
                statsGrid.add(createStatCard(
                                "Available Seats", seatsValue, "#287C87"), 3, 0);

                Button refresh = new Button("Refresh Dashboard");
                refresh.setStyle(
                                "-fx-background-color: " + ROYAL_BLUE + ";"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                                + "-fx-padding: 10 18;"
                                                + "-fx-background-radius: 8;");

                refresh.setOnAction(e -> refreshDashboard());

                HBox refreshRow = new HBox(refresh);
                refreshRow.setAlignment(Pos.CENTER_RIGHT);

                attendanceChart = new PieChart();
                attendanceChart.setTitle("Attendance Overview");
                attendanceChart.setLegendVisible(true);
                attendanceChart.setLabelsVisible(true);
                attendanceChart.setPrefHeight(280);

                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Event Category");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Number of Events");
                yAxis.setMinorTickVisible(false);

                categoryChart = new BarChart<>(xAxis, yAxis);
                categoryChart.setTitle("Events by Category");
                categoryChart.setLegendVisible(false);
                categoryChart.setAnimated(false);
                categoryChart.setPrefHeight(280);

                HBox charts = new HBox(18,
                                createPanel(attendanceChart),
                                createPanel(categoryChart));

                HBox.setHgrow(charts.getChildren().get(0), Priority.ALWAYS);
                HBox.setHgrow(charts.getChildren().get(1), Priority.ALWAYS);

                upcomingBox = new VBox(9);
                recentBox = new VBox(9);

                HBox lower = new HBox(18,
                                createPanel(upcomingBox),
                                createPanel(recentBox));

                HBox.setHgrow(lower.getChildren().get(0), Priority.ALWAYS);
                HBox.setHgrow(lower.getChildren().get(1), Priority.ALWAYS);

                dashboardContent.getChildren().addAll(
                                heading, subtitle, statsGrid, refreshRow, charts, lower);

                javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(dashboardContent);

                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(
                                javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setStyle(
                                "-fx-background: " + BACKGROUND + ";"
                                                + "-fx-background-color: " + BACKGROUND + ";");

                setCenter(scrollPane);

                refreshDashboard();
        }

        private VBox createStatCard(
                        String title, Label value, String color) {

                value.setStyle(
                                "-fx-font-size: 30px;"
                                                + "-fx-font-weight: bold;"
                                                + "-fx-text-fill: white;");

                Label heading = new Label(title);
                heading.setStyle(
                                "-fx-font-size: 14px;"
                                                + "-fx-text-fill: white;");

                VBox card = new VBox(10, value, heading);
                card.setPadding(new Insets(20));
                card.setMinWidth(145);
                card.setMaxWidth(Double.MAX_VALUE);
                card.setStyle(
                                "-fx-background-color: " + color + ";"
                                                + "-fx-background-radius: 13;");

                GridPane.setHgrow(card, Priority.ALWAYS);

                return card;
        }

        private VBox createPanel(Node content) {

                VBox panel = new VBox(12, content);
                panel.setPadding(new Insets(18));
                panel.setStyle(
                                "-fx-background-color: white;"
                                                + "-fx-background-radius: 12;"
                                                + "-fx-border-color: #E2E8F0;"
                                                + "-fx-border-radius: 12;");

                HBox.setHgrow(panel, Priority.ALWAYS);
                panel.setMinWidth(0);

                return panel;
        }

        private void refreshDashboard() {

                try {
                        loadStats();
                        loadAttendanceChart();
                        loadCategoryChart();
                        loadUpcomingEvents();
                        loadRecentRegistrations();

                } catch (IllegalStateException ex) {
                        showAlert("Dashboard Error", ex.getMessage());
                }
        }

        private void loadStats() {

                DashboardStats stats = statsDAO.getDashboardStats();

                eventsValue.setText(String.valueOf(stats.getTotalEvents()));
                studentsValue.setText(String.valueOf(stats.getTotalStudents()));
                registrationsValue.setText(
                                String.valueOf(stats.getTotalRegistrations()));
                seatsValue.setText(String.valueOf(stats.getAvailableSeats()));
        }

        private void loadAttendanceChart() {

                DashboardStats stats = statsDAO.getDashboardStats();

                attendanceChart.setData(FXCollections.observableArrayList(
                                new PieChart.Data(
                                                "Present", stats.getPresentCount()),
                                new PieChart.Data(
                                                "Absent", stats.getAbsentCount()),
                                new PieChart.Data(
                                                "Not Marked", stats.getNotMarkedCount())));
        }

        private void loadCategoryChart() {

                Map<String, Integer> counts = overviewDAO.getCategoryCounts();

                javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();

                series.setName("Events");

                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                        series.getData().add(
                                        new javafx.scene.chart.XYChart.Data<>(
                                                        entry.getKey(), entry.getValue()));
                }

                categoryChart.getData().clear();

                if (!series.getData().isEmpty()) {
                        categoryChart.getData().add(series);
                }
        }

        private void loadUpcomingEvents() {

                upcomingBox.getChildren().clear();

                Label heading = new Label("Upcoming Events");
                heading.setStyle(
                                "-fx-font-size: 18px; -fx-font-weight: bold;"
                                                + "-fx-text-fill: " + NAVY + ";");

                upcomingBox.getChildren().add(heading);

                List<Event> events = overviewDAO.getUpcomingEvents();

                if (events.isEmpty()) {
                        upcomingBox.getChildren().add(
                                        new Label("No upcoming events."));
                        return;
                }

                for (Event event : events) {

                        Label details = new Label(
                                        event.getEventName()
                                                        + "  •  " + event.getDate()
                                                        + "\n" + event.getStartTime()
                                                        + " – " + event.getEndTime()
                                                        + "  •  " + event.getVenue()
                                                        + "\n" + event.getCategory()
                                                        + "  •  " + event.getRegisteredCount()
                                                        + "/" + event.getCapacity() + " registered");

                        details.setWrapText(true);
                        details.setPadding(new Insets(10));
                        details.setMaxWidth(Double.MAX_VALUE);
                        details.setStyle(
                                        "-fx-background-color: #F0F4FF;"
                                                        + "-fx-background-radius: 8;"
                                                        + "-fx-text-fill: " + NAVY + ";");

                        upcomingBox.getChildren().add(details);
                }

                Button viewAll = new Button("View All Events");
                viewAll.setOnAction(e -> navigateTo(eventsBtn, new EventsView()));

                upcomingBox.getChildren().add(viewAll);
        }

        private void loadRecentRegistrations() {

                recentBox.getChildren().clear();

                Label heading = new Label("Recent Registrations");
                heading.setStyle(
                                "-fx-font-size: 18px; -fx-font-weight: bold;"
                                                + "-fx-text-fill: " + NAVY + ";");

                recentBox.getChildren().add(heading);

                List<Registration> registrations = overviewDAO.getRecentRegistrations();

                if (registrations.isEmpty()) {
                        recentBox.getChildren().add(
                                        new Label("No registrations found."));
                        return;
                }

                for (Registration registration : registrations) {

                        Label details = new Label(
                                        registration.getStudent().getStudentName()
                                                        + "\n" + registration.getEvent().getEventName()
                                                        + "  •  Registration #"
                                                        + registration.getRegistrationId()
                                                        + "\nAttendance: "
                                                        + registration.getAttendanceStatus());

                        details.setWrapText(true);
                        details.setPadding(new Insets(10));
                        details.setMaxWidth(Double.MAX_VALUE);
                        details.setStyle(
                                        "-fx-background-color: #F6F1FF;"
                                                        + "-fx-background-radius: 8;"
                                                        + "-fx-text-fill: " + NAVY + ";");

                        recentBox.getChildren().add(details);
                }

                Button viewAll = new Button("View All Registrations");
                viewAll.setOnAction(e -> navigateTo(registrationsBtn, new RegistrationsView()));

                recentBox.getChildren().add(viewAll);
        }

        private void showAlert(String title, String message) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }
}
