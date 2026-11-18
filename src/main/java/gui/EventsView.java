
package main.java.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import main.java.manager.EventManager;
import main.java.model.Event;

public class EventsView extends VBox {

        private final EventManager eventManager;
        private TableView<Event> eventTable;
        private ObservableList<Event> allEvents;

        private TextField searchField;
        private ComboBox<String> categoryFilter;
        private ComboBox<String> statusFilter;

        private final String[] categories = {
                        "Technical", "Cultural", "Sports",
                        "Workshop", "Academic", "Other"
        };

        public EventsView() {

                eventManager = new EventManager();
                allEvents = FXCollections.observableArrayList();

                setSpacing(18);
                setPadding(new Insets(25));
                setStyle("-fx-background-color: #F4F6FC;");

                Label heading = new Label("Events Management");
                heading.setStyle(
                                "-fx-font-size: 27px; -fx-font-weight: bold;"
                                                + "-fx-text-fill: #142447;");

                Label subtitle = new Label(
                                "Create, manage and monitor campus events");
                subtitle.setStyle("-fx-text-fill: #64748B;");

                Button addButton = createButton("+ Add Event", "#315FEA");
                Button editButton = createButton("Edit Event", "#8B5CF6");
                Button cancelButton = createButton("Cancel Event", "#D97777");
                Button deleteButton = createButton("Delete Event", "#DC4545");
                Button refreshButton = createButton("Refresh", "#334155");

                addButton.setOnAction(e -> showEventDialog(null));
                editButton.setOnAction(e -> editSelectedEvent());
                cancelButton.setOnAction(e -> cancelSelectedEvent());
                deleteButton.setOnAction(e -> deleteSelectedEvent());
                refreshButton.setOnAction(e -> loadEvents());

                HBox actions = new HBox(
                                10, addButton, editButton, cancelButton,
                                deleteButton, refreshButton);

                searchField = new TextField();
                searchField.setPromptText("Search event name or venue...");
                searchField.textProperty().addListener(
                                (obs, oldValue, newValue) -> applyFilters());

                categoryFilter = new ComboBox<>();
                categoryFilter.getItems().add("All Categories");
                categoryFilter.getItems().addAll(categories);
                categoryFilter.setValue("All Categories");
                categoryFilter.setOnAction(e -> applyFilters());

                statusFilter = new ComboBox<>();
                statusFilter.getItems().addAll(
                                "All Statuses", "Upcoming",
                                "Ongoing", "Completed", "Cancelled");
                statusFilter.setValue("All Statuses");
                statusFilter.setOnAction(e -> applyFilters());

                HBox filters = new HBox(
                                12, searchField, categoryFilter, statusFilter);

                eventTable = new TableView<>();

                TableColumn<Event, Integer> id = new TableColumn<>("ID");
                id.setCellValueFactory(new PropertyValueFactory<>("eventId"));

                TableColumn<Event, String> name = new TableColumn<>("Event");
                name.setCellValueFactory(new PropertyValueFactory<>("eventName"));

                TableColumn<Event, String> category = new TableColumn<>("Category");
                category.setCellValueFactory(new PropertyValueFactory<>("category"));

                TableColumn<Event, String> venue = new TableColumn<>("Venue");
                venue.setCellValueFactory(new PropertyValueFactory<>("venue"));

                TableColumn<Event, String> date = new TableColumn<>("Date");
                date.setCellValueFactory(new PropertyValueFactory<>("date"));

                TableColumn<Event, String> time = new TableColumn<>("Time Slot");
                time.setCellValueFactory(data -> new SimpleStringProperty(
                                data.getValue().getStartTime() + " - "
                                                + data.getValue().getEndTime()));

                TableColumn<Event, Integer> capacity = new TableColumn<>("Capacity");
                capacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));

                TableColumn<Event, Integer> registered = new TableColumn<>("Registered");
                registered.setCellValueFactory(
                                new PropertyValueFactory<>("registeredCount"));

                TableColumn<Event, Integer> available = new TableColumn<>("Available");
                available.setCellValueFactory(data -> new SimpleIntegerProperty(
                                data.getValue().getAvailableSeats()).asObject());

                TableColumn<Event, String> deadline = new TableColumn<>("Deadline");
                deadline.setCellValueFactory(
                                new PropertyValueFactory<>("registrationDeadline"));

                TableColumn<Event, String> status = new TableColumn<>("Status");
                status.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

                eventTable.getColumns().addAll(
                                id, name, category, venue, date, time,
                                capacity, registered, available, deadline, status);

                eventTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                VBox.setVgrow(eventTable, javafx.scene.layout.Priority.ALWAYS);

                getChildren().addAll(
                                heading, subtitle, actions, filters, eventTable);

                loadEvents();
        }

        private Button createButton(String text, String color) {

                Button button = new Button(text);

                button.setStyle(
                                "-fx-background-color: " + color + ";"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                                + "-fx-padding: 10 15;"
                                                + "-fx-background-radius: 7;"
                                                + "-fx-cursor: hand;");

                return button;
        }

        private String safeText(String value) {
                return value == null ? "" : value;
        }

        private void showEventDialog(Event selected) {

                boolean editing = selected != null;

                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle(editing ? "Edit Event" : "Add New Event");
                dialog.setHeaderText(
                                editing ? "Update event information"
                                                : "Enter event information");

                ButtonType saveType = new ButtonType(
                                editing ? "Update Event" : "Save Event",
                                ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                saveType, ButtonType.CANCEL);

                TextField idField = new TextField();
                idField.setPromptText("Event ID");
                idField.setDisable(editing);

                TextField nameField = new TextField();
                nameField.setPromptText("Event name");

                TextField venueField = new TextField();
                venueField.setPromptText("Venue");

                ComboBox<String> categoryField = new ComboBox<>();
                categoryField.getItems().addAll(categories);
                categoryField.setValue("Other");

                TextArea descriptionField = new TextArea();
                descriptionField.setPromptText("Event description");
                descriptionField.setPrefRowCount(3);

                DatePicker datePicker = new DatePicker();

                datePicker.setDayCellFactory(picker -> new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(!editing
                                                && (empty || date.isBefore(LocalDate.now())));
                        }
                });

                TextField startField = new TextField("09:00:00");
                TextField endField = new TextField("17:00:00");

                DatePicker deadlinePicker = new DatePicker();

                TextField capacityField = new TextField();
                capacityField.setPromptText("Maximum participants");

                TextField posterField = new TextField();
                posterField.setPromptText("Optional poster image path");

                // New certificate option
                CheckBox certificateEnabledCheckBox = new CheckBox("Enable Participation Certificates");

                certificateEnabledCheckBox.setSelected(false);

                if (editing) {
                        idField.setText(String.valueOf(selected.getEventId()));
                        nameField.setText(safeText(selected.getEventName()));
                        venueField.setText(safeText(selected.getVenue()));

                        categoryField.setValue(
                                        selected.getCategory() == null
                                                        ? "Other"
                                                        : selected.getCategory());

                        descriptionField.setText(
                                        safeText(selected.getDescription()));

                        datePicker.setValue(LocalDate.parse(selected.getDate()));
                        startField.setText(selected.getStartTime());
                        endField.setText(selected.getEndTime());

                        deadlinePicker.setValue(
                                        LocalDate.parse(selected.getRegistrationDeadline()));

                        capacityField.setText(String.valueOf(selected.getCapacity()));
                        posterField.setText(safeText(selected.getPosterPath()));

                        // Preserve the existing certificate setting while editing
                        certificateEnabledCheckBox.setSelected(
                                        selected.isCertificateEnabled());
                }

                GridPane form = new GridPane();
                form.setHgap(12);
                form.setVgap(12);
                form.setPadding(new Insets(15));

                int row = 0;

                form.addRow(row++, new Label("Event ID:"), idField);
                form.addRow(row++, new Label("Event Name:"), nameField);
                form.addRow(row++, new Label("Category:"), categoryField);
                form.addRow(row++, new Label("Venue:"), venueField);
                form.addRow(row++, new Label("Description:"), descriptionField);
                form.addRow(row++, new Label("Event Date:"), datePicker);
                form.addRow(row++, new Label("Start Time:"), startField);
                form.addRow(row++, new Label("End Time:"), endField);
                form.addRow(row++, new Label("Registration Deadline:"), deadlinePicker);
                form.addRow(row++, new Label("Capacity:"), capacityField);
                form.addRow(row++, new Label("Poster Path:"), posterField);

                // Certificate checkbox in the event form
                form.addRow(row, new Label("Certificates:"), certificateEnabledCheckBox);

                dialog.getDialogPane().setContent(form);
                dialog.getDialogPane().setPrefWidth(650);

                Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);

                saveButton.addEventFilter(
                                javafx.event.ActionEvent.ACTION, action -> {

                                        action.consume();

                                        try {
                                                validateFields(
                                                                idField.getText(),
                                                                nameField.getText(),
                                                                venueField.getText(),
                                                                categoryField.getValue(),
                                                                datePicker.getValue(),
                                                                startField.getText(),
                                                                endField.getText(),
                                                                deadlinePicker.getValue(),
                                                                capacityField.getText(),
                                                                editing);

                                                Event event = new Event(
                                                                Integer.parseInt(idField.getText().trim()),
                                                                nameField.getText().trim(),
                                                                venueField.getText().trim(),
                                                                datePicker.getValue().toString(),
                                                                Integer.parseInt(capacityField.getText().trim()),
                                                                editing ? selected.getRegisteredCount() : 0,
                                                                categoryField.getValue(),
                                                                safeText(descriptionField.getText()).trim(),
                                                                startField.getText().trim(),
                                                                endField.getText().trim(),
                                                                deadlinePicker.getValue().toString(),
                                                                safeText(posterField.getText()).trim().isEmpty()
                                                                                ? null
                                                                                : posterField.getText().trim(),
                                                                editing && selected.isCancelled(),
                                                                certificateEnabledCheckBox.isSelected());

                                                boolean result;

                                                if (editing) {
                                                        result = eventManager.updateEvent(event);
                                                } else {
                                                        result = eventManager.addEvent(event);
                                                }

                                                if (result) {
                                                        loadEvents();
                                                        dialog.close();

                                                        showAlert(
                                                                        Alert.AlertType.INFORMATION,
                                                                        "Success",
                                                                        editing
                                                                                        ? "Event updated successfully!"
                                                                                        : "Event added successfully!");
                                                }

                                        } catch (IllegalArgumentException
                                                        | IllegalStateException ex) {

                                                showAlert(
                                                                Alert.AlertType.ERROR,
                                                                "Unable to Save Event",
                                                                ex.getMessage());

                                        } catch (Exception ex) {

                                                ex.printStackTrace();

                                                showAlert(
                                                                Alert.AlertType.ERROR,
                                                                "Unexpected Error",
                                                                "Could not save the event: " + ex.getMessage());
                                        }
                                });

                dialog.showAndWait();
        }

        private void validateFields(
                        String id, String name, String venue, String category,
                        LocalDate date, String start, String end,
                        LocalDate deadline, String capacity, boolean editing) {

                try {
                        if (id == null || Integer.parseInt(id.trim()) <= 0) {
                                throw new IllegalArgumentException("Enter a valid Event ID.");
                        }
                } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                        "Event ID must be a positive number.");
                }

                if (name == null || name.isBlank() || name.trim().length() > 100) {
                        throw new IllegalArgumentException(
                                        "Enter an event name (maximum 100 characters).");
                }

                if (venue == null || venue.isBlank() || venue.trim().length() > 100) {
                        throw new IllegalArgumentException(
                                        "Enter a valid venue (maximum 100 characters).");
                }

                if (category == null || category.isBlank()) {
                        throw new IllegalArgumentException("Select a category.");
                }

                if (date == null) {
                        throw new IllegalArgumentException("Select an event date.");
                }

                if (!editing && date.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException(
                                        "New event date cannot be in the past.");
                }

                if (deadline == null || deadline.isAfter(date)) {
                        throw new IllegalArgumentException(
                                        "Registration deadline cannot be after event date.");
                }

                try {
                        LocalTime startTime = LocalTime.parse(start.trim());
                        LocalTime endTime = LocalTime.parse(end.trim());

                        if (!endTime.isAfter(startTime)) {
                                throw new IllegalArgumentException(
                                                "End time must be after start time.");
                        }

                } catch (DateTimeParseException ex) {
                        throw new IllegalArgumentException(
                                        "Enter time in HH:mm:ss format, e.g. 09:00:00.");
                }

                try {
                        if (capacity == null
                                        || Integer.parseInt(capacity.trim()) <= 0) {
                                throw new IllegalArgumentException(
                                                "Capacity must be greater than zero.");
                        }
                } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                        "Enter a valid numeric capacity.");
                }
        }

        private void editSelectedEvent() {

                Event selected = eventTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Selection", "Please select an event.");
                        return;
                }

                showEventDialog(selected);
        }

        private void cancelSelectedEvent() {

                Event selected = eventTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Selection", "Please select an event.");
                        return;
                }

                if (selected.isCancelled()) {
                        showAlert(Alert.AlertType.INFORMATION,
                                        "Already Cancelled", "This event is already cancelled.");
                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Cancel Event");
                confirmation.setHeaderText(
                                "Cancel " + selected.getEventName() + "?");
                confirmation.setContentText(
                                "Students will no longer be able to register.");

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {
                                try {
                                        Event updated = new Event(
                                                        selected.getEventId(),
                                                        selected.getEventName(),
                                                        selected.getVenue(),
                                                        selected.getDate(),
                                                        selected.getCapacity(),
                                                        selected.getRegisteredCount(),
                                                        selected.getCategory(),
                                                        safeText(selected.getDescription()),
                                                        selected.getStartTime(),
                                                        selected.getEndTime(),
                                                        selected.getRegistrationDeadline(),
                                                        selected.getPosterPath(),
                                                        true,
                                                        selected.isCertificateEnabled());

                                        eventManager.updateEvent(updated);
                                        loadEvents();

                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Success", "Event cancelled successfully.");

                                } catch (Exception ex) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Cancellation Failed", ex.getMessage());
                                }
                        }
                });
        }

        private void deleteSelectedEvent() {

                Event selected = eventTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Selection", "Please select an event.");
                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Delete Event");
                confirmation.setHeaderText(
                                "Delete " + selected.getEventName() + "?");
                confirmation.setContentText("This operation cannot be undone.");

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {
                                try {
                                        eventManager.deleteEvent(selected.getEventId());
                                        loadEvents();

                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Success", "Event deleted successfully.");

                                } catch (Exception ex) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Delete Failed", ex.getMessage());
                                }
                        }
                });
        }

        private void applyFilters() {

                if (allEvents == null) {
                        return;
                }

                String search = searchField.getText() == null
                                ? ""
                                : searchField.getText().toLowerCase().trim();

                String category = categoryFilter.getValue();
                String status = statusFilter.getValue();

                ObservableList<Event> filtered = FXCollections.observableArrayList();

                for (Event event : allEvents) {

                        boolean matchesSearch = safeText(event.getEventName()).toLowerCase().contains(search)
                                        || safeText(event.getVenue()).toLowerCase().contains(search);

                        boolean matchesCategory = "All Categories".equals(category)
                                        || safeText(event.getCategory()).equals(category);

                        boolean matchesStatus = "All Statuses".equals(status)
                                        || event.getStatus().equals(status);

                        if (matchesSearch && matchesCategory && matchesStatus) {
                                filtered.add(event);
                        }
                }

                eventTable.setItems(filtered);
        }

        private void loadEvents() {

                try {
                        allEvents.setAll(eventManager.getAllEvents());
                        applyFilters();

                } catch (IllegalStateException ex) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Database Error", ex.getMessage());
                }
        }

        private void showAlert(
                        Alert.AlertType type, String title, String message) {

                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }
}
