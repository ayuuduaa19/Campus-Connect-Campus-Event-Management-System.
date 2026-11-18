
package main.java.gui;

import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import main.java.manager.StudentManager;
import main.java.manager.AuthManager;
import main.java.model.Student;

public class StudentsView extends VBox {

        private final StudentManager studentManager;
        private final AuthManager authManager;

        private TableView<Student> studentTable;
        private final ObservableList<Student> studentData;

        private Map<Integer, String> studentAccounts = Map.of();

        public StudentsView() {

                studentManager = new StudentManager();
                authManager = new AuthManager();
                studentData = FXCollections.observableArrayList();

                setSpacing(20);
                setPadding(new Insets(30));
                setStyle("-fx-background-color: #EAF2F8;");

                Label heading = new Label("Students Management");
                heading.setStyle(
                                "-fx-font-size: 28px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-text-fill: #1B365D;");

                Label subtitle = new Label("Manage all registered students");
                subtitle.setStyle(
                                "-fx-font-size: 14px;" +
                                                "-fx-text-fill: #526477;");

                TextField searchField = new TextField();
                searchField.setPromptText("Search by student ID, name or email...");
                searchField.setPrefWidth(300);
                searchField.setStyle(
                                "-fx-background-radius: 8px;" +
                                                "-fx-padding: 10px;");

                Button addButton = new Button("+ Add Student");
                styleButton(addButton, "#462C7D");
                addButton.setOnAction(e -> showAddStudentDialog());

                Button editButton = new Button("Edit Student");
                styleButton(editButton, "#E0A526");
                editButton.setOnAction(e -> showEditStudentDialog());

                Button deleteButton = new Button("Delete Student");
                styleButton(deleteButton, "#C0392B");
                deleteButton.setOnAction(e -> deleteSelectedStudent());

                Button resetButton = new Button("Reset Password");
                styleButton(resetButton, "#7551B8");
                resetButton.setOnAction(e -> resetSelectedStudentPassword());

                Button refreshButton = new Button("Refresh");
                styleButton(refreshButton, "#075B9B");
                refreshButton.setOnAction(e -> loadStudents());

                HBox actionBar = new HBox(
                                10, searchField, addButton, editButton,
                                deleteButton, resetButton, refreshButton);

                actionBar.setAlignment(Pos.CENTER_LEFT);

                Button createAccountButton = new Button("+ Create Student Login Account");

                styleButton(createAccountButton, "#16805D");
                createAccountButton.setOnAction(
                                e -> showCreateAccountDialog());

                HBox accountBar = new HBox(createAccountButton);
                accountBar.setAlignment(Pos.CENTER_RIGHT);

                studentTable = new TableView<>();

                TableColumn<Student, Integer> idColumn = new TableColumn<>("Student ID");

                idColumn.setCellValueFactory(
                                new PropertyValueFactory<>("studentId"));

                TableColumn<Student, String> nameColumn = new TableColumn<>("Student Name");

                nameColumn.setCellValueFactory(
                                new PropertyValueFactory<>("studentName"));

                TableColumn<Student, String> emailColumn = new TableColumn<>("Email");

                emailColumn.setCellValueFactory(
                                new PropertyValueFactory<>("email"));

                TableColumn<Student, String> usernameColumn = new TableColumn<>("Username");

                usernameColumn.setCellValueFactory(data -> {
                        String username = studentAccounts.get(
                                        data.getValue().getStudentId());

                        return new javafx.beans.property.SimpleStringProperty(
                                        username == null ? "—" : username);
                });

                TableColumn<Student, String> accountStatusColumn = new TableColumn<>("Account Status");

                accountStatusColumn.setCellValueFactory(data -> {
                        boolean exists = studentAccounts.containsKey(
                                        data.getValue().getStudentId());

                        return new javafx.beans.property.SimpleStringProperty(
                                        exists ? "Account Created" : "Not Created");
                });

                studentTable.getColumns().addAll(
                                idColumn, nameColumn, emailColumn,
                                usernameColumn, accountStatusColumn);

                studentTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                studentTable.setPlaceholder(
                                new Label("No student records found."));

                FilteredList<Student> filteredStudents = new FilteredList<>(studentData, student -> true);

                searchField.textProperty().addListener(
                                (observable, oldValue, newValue) -> {

                                        String search = newValue.toLowerCase().trim();

                                        filteredStudents.setPredicate(student -> {

                                                if (search.isEmpty()) {
                                                        return true;
                                                }

                                                return String.valueOf(student.getStudentId())
                                                                .contains(search)
                                                                || student.getStudentName()
                                                                                .toLowerCase().contains(search)
                                                                || student.getEmail()
                                                                                .toLowerCase().contains(search)
                                                                || studentAccounts.getOrDefault(
                                                                                student.getStudentId(), "")
                                                                                .toLowerCase().contains(search);
                                        });
                                });

                studentTable.setItems(filteredStudents);

                VBox.setVgrow(studentTable, Priority.ALWAYS);

                getChildren().addAll(
                                heading, subtitle, actionBar,
                                accountBar, studentTable);

                loadStudents();
        }

        private void styleButton(Button button, String color) {
                button.setStyle(
                                "-fx-background-color: " + color + ";" +
                                                "-fx-text-fill: white;" +
                                                "-fx-font-size: 13px;" +
                                                "-fx-padding: 10 15;" +
                                                "-fx-background-radius: 8px;" +
                                                "-fx-cursor: hand;");
        }

        // ---------------- LOAD STUDENTS ----------------

        private void loadStudents() {

                try {
                        List<Student> students = studentManager.getAllStudents();
                        studentAccounts = authManager.getStudentAccountUsernames();
                        studentData.setAll(students);
                        studentTable.refresh();

                } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Database Error", e.getMessage());
                }
        }

        // ---------------- RESET STUDENT PASSWORD ----------------

        private void resetSelectedStudentPassword() {

                Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

                if (selectedStudent == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Student Selected",
                                        "Please select a student first.");
                        return;
                }

                if (!studentAccounts.containsKey(selectedStudent.getStudentId())) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Login Account",
                                        "This student does not have a login account yet.");
                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Reset Student Password");
                confirmation.setHeaderText(
                                "Reset password for " + selectedStudent.getStudentName() + "?");

                confirmation.setContentText(
                                "A new temporary password will be generated. "
                                                + "The student's next login will require a password change.");

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {

                                try {
                                        String temporaryPassword = authManager.resetStudentPassword(
                                                        selectedStudent.getStudentId());

                                        TextField passwordDisplay = new TextField(temporaryPassword);
                                        passwordDisplay.setEditable(false);
                                        passwordDisplay.setPrefWidth(280);

                                        Button copyButton = new Button("Copy");
                                        copyButton.setOnAction(e -> {
                                                javafx.scene.input.ClipboardContent clipboard = new javafx.scene.input.ClipboardContent();

                                                clipboard.putString(temporaryPassword);
                                                javafx.scene.input.Clipboard.getSystemClipboard()
                                                                .setContent(clipboard);
                                        });

                                        HBox passwordBox = new HBox(8, passwordDisplay, copyButton);
                                        passwordBox.setAlignment(Pos.CENTER_LEFT);

                                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                                        success.setTitle("Temporary Password Generated");
                                        success.setHeaderText("Password reset successfully");

                                        VBox content = new VBox(10,
                                                        new Label("Student: "
                                                                        + selectedStudent.getStudentName()),
                                                        new Label("Username: "
                                                                        + studentAccounts.get(
                                                                                        selectedStudent.getStudentId())),
                                                        new Label("Temporary password:"),
                                                        passwordBox,
                                                        new Label(
                                                                        "Share this password securely. "
                                                                                        + "It will not be displayed again."));

                                        content.setPadding(new Insets(10));
                                        success.getDialogPane().setContent(content);
                                        success.showAndWait();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Password Reset Failed", e.getMessage());
                                }
                        }
                });
        }

        // ---------------- CREATE STUDENT LOGIN ACCOUNT ----------------

        private void showCreateAccountDialog() {

                List<Student> students;

                try {
                        students = studentManager.getAllStudents();
                } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.ERROR,
                                        "Database Error", e.getMessage());
                        return;
                }

                if (students.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Students Available",
                                        "Please add a student record before creating a login account.");
                        return;
                }

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Create Student Login Account");
                dialog.setHeaderText("Enter the Student ID to find the student");

                ButtonType createButtonType = new ButtonType(
                                "Create Account", ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                createButtonType, ButtonType.CANCEL);

                TextField studentIdField = new TextField();
                studentIdField.setPromptText("Enter Student ID");

                Label studentNameLabel = new Label("Student Name: Not found");
                Label studentEmailLabel = new Label("Email: Not found");
                Label statusLabel = new Label("Enter a valid Student ID.");

                Student[] selectedStudent = { null };

                TextField usernameField = new TextField();
                usernameField.setPromptText("Enter unique username");

                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Enter password (minimum 8 characters)");

                PasswordField confirmPasswordField = new PasswordField();
                confirmPasswordField.setPromptText("Confirm password");

                studentIdField.textProperty().addListener(
                                (observable, oldValue, newValue) -> {

                                        selectedStudent[0] = null;
                                        studentNameLabel.setText("Student Name: Not found");
                                        studentEmailLabel.setText("Email: Not found");

                                        String idText = newValue.trim();

                                        if (idText.isEmpty()) {
                                                statusLabel.setText("Enter a Student ID.");
                                                return;
                                        }

                                        if (!idText.matches("\\d+")) {
                                                statusLabel.setText(
                                                                "Student ID must contain numbers only.");
                                                return;
                                        }

                                        try {
                                                int studentId = Integer.parseInt(idText);

                                                for (Student student : students) {
                                                        if (student.getStudentId() == studentId) {

                                                                selectedStudent[0] = student;

                                                                studentNameLabel.setText(
                                                                                "Student Name: " + student
                                                                                                .getStudentName());

                                                                studentEmailLabel.setText(
                                                                                "Email: " + student.getEmail());

                                                                statusLabel.setText("Student found!");
                                                                return;
                                                        }
                                                }

                                                statusLabel.setText(
                                                                "Student not found in database.");

                                        } catch (NumberFormatException ex) {
                                                statusLabel.setText("Invalid Student ID.");
                                        }
                                });

                GridPane form = new GridPane();
                form.setHgap(15);
                form.setVgap(13);
                form.setPadding(new Insets(20));

                form.add(new Label("Student ID:"), 0, 0);
                form.add(studentIdField, 1, 0);
                form.add(statusLabel, 1, 1);
                form.add(studentNameLabel, 1, 2);
                form.add(studentEmailLabel, 1, 3);
                form.add(new Label("Username:"), 0, 4);
                form.add(usernameField, 1, 4);
                form.add(new Label("Password:"), 0, 5);
                form.add(passwordField, 1, 5);
                form.add(new Label("Confirm Password:"), 0, 6);
                form.add(confirmPasswordField, 1, 6);

                Label note = new Label(
                                "Each student can have only one login account.");

                note.setWrapText(true);
                form.add(note, 1, 7);

                dialog.getDialogPane().setContent(form);
                dialog.getDialogPane().setPrefWidth(520);

                Button createButton = (Button) dialog.getDialogPane()
                                .lookupButton(createButtonType);

                createButton.addEventFilter(ActionEvent.ACTION, event -> {

                        if (selectedStudent[0] == null) {
                                showAlert(Alert.AlertType.WARNING,
                                                "Student Not Found",
                                                "Enter a valid Student ID before creating an account.");
                                event.consume();
                                return;
                        }

                        String username = usernameField.getText().trim();
                        String password = passwordField.getText();
                        String confirmPassword = confirmPasswordField.getText();

                        if (username.isEmpty()) {
                                showAlert(Alert.AlertType.WARNING,
                                                "Missing Username", "Please enter a username.");
                                event.consume();
                                return;
                        }

                        if (password.length() < 8) {
                                showAlert(Alert.AlertType.WARNING,
                                                "Weak Password",
                                                "Password must contain at least 8 characters.");
                                event.consume();
                                return;
                        }

                        if (!password.equals(confirmPassword)) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Password Mismatch",
                                                "Both passwords must match.");
                                event.consume();
                        }
                });

                dialog.setResultConverter(button -> button);

                dialog.showAndWait().ifPresent(result -> {

                        if (result == createButtonType) {

                                Student student = selectedStudent[0];

                                try {
                                        boolean created = authManager.createStudentAccount(
                                                        usernameField.getText().trim(),
                                                        passwordField.getText(),
                                                        student.getStudentId());

                                        if (created) {
                                                showAlert(Alert.AlertType.INFORMATION,
                                                                "Account Created",
                                                                "Student login account created successfully!\n\n"
                                                                                + "Student: " + student.getStudentName()
                                                                                + "\nStudent ID: "
                                                                                + student.getStudentId()
                                                                                + "\nUsername: "
                                                                                + usernameField.getText().trim());

                                                loadStudents();

                                        } else {
                                                showAlert(Alert.AlertType.ERROR,
                                                                "Account Creation Failed",
                                                                "The username or student account may already exist.");
                                        }

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Account Creation Failed", e.getMessage());
                                }
                        }
                });
        }

        // ---------------- ADD STUDENT ----------------

        private void showAddStudentDialog() {

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Add Student");
                dialog.setHeaderText("Enter student details");

                ButtonType saveButton = new ButtonType(
                                "Save Student", ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                saveButton, ButtonType.CANCEL);

                TextField idField = new TextField();
                idField.setPromptText("Enter student ID");

                TextField nameField = new TextField();
                nameField.setPromptText("Enter student name");

                TextField emailField = new TextField();
                emailField.setPromptText("Enter email address");

                GridPane form = new GridPane();
                form.setHgap(15);
                form.setVgap(15);
                form.setPadding(new Insets(20));

                form.add(new Label("Student ID:"), 0, 0);
                form.add(idField, 1, 0);
                form.add(new Label("Student Name:"), 0, 1);
                form.add(nameField, 1, 1);
                form.add(new Label("Email:"), 0, 2);
                form.add(emailField, 1, 2);

                dialog.getDialogPane().setContent(form);

                Button saveButtonNode = (Button) dialog.getDialogPane()
                                .lookupButton(saveButton);

                saveButtonNode.addEventFilter(ActionEvent.ACTION, event -> {

                        String idText = idField.getText().trim();
                        String name = nameField.getText().trim();
                        String email = emailField.getText().trim();

                        if (!idText.matches("\\d+")) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Student ID",
                                                "Student ID must contain numbers only.");
                                event.consume();
                                return;
                        }

                        try {
                                if (Integer.parseInt(idText) <= 0) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Invalid Student ID",
                                                        "Student ID must be a positive number.");
                                        event.consume();
                                        return;
                                }
                        } catch (NumberFormatException ex) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Student ID", "Student ID is too large.");
                                event.consume();
                                return;
                        }

                        if (name.isEmpty() || name.length() > 100) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Name",
                                                "Student name must contain 1 to 100 characters.");
                                event.consume();
                                return;
                        }

                        if (!validEmail(email)) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Email",
                                                "Enter a valid email address (maximum 150 characters).");
                                event.consume();
                        }
                });

                dialog.setResultConverter(button -> button);

                dialog.showAndWait().ifPresent(result -> {

                        if (result == saveButton) {
                                try {
                                        Student student = new Student(
                                                        Integer.parseInt(idField.getText().trim()),
                                                        nameField.getText().trim(),
                                                        emailField.getText().trim());

                                        studentManager.addStudent(student);

                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Success", "Student added successfully!");

                                        loadStudents();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Unable to Add Student", e.getMessage());
                                }
                        }
                });
        }

        // ---------------- EDIT STUDENT ----------------

        private void showEditStudentDialog() {

                Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

                if (selectedStudent == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Student Selected",
                                        "Please select a student from the table to edit.");
                        return;
                }

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Student");
                dialog.setHeaderText("Update student details");

                ButtonType updateButton = new ButtonType(
                                "Update Student", ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                updateButton, ButtonType.CANCEL);

                TextField nameField = new TextField(selectedStudent.getStudentName());

                TextField emailField = new TextField(selectedStudent.getEmail());

                GridPane form = new GridPane();
                form.setHgap(15);
                form.setVgap(15);
                form.setPadding(new Insets(20));

                form.add(new Label("Student ID:"), 0, 0);
                form.add(new Label(
                                String.valueOf(selectedStudent.getStudentId())), 1, 0);

                form.add(new Label("Student Name:"), 0, 1);
                form.add(nameField, 1, 1);

                form.add(new Label("Email:"), 0, 2);
                form.add(emailField, 1, 2);

                dialog.getDialogPane().setContent(form);

                Button updateButtonNode = (Button) dialog.getDialogPane()
                                .lookupButton(updateButton);

                updateButtonNode.addEventFilter(ActionEvent.ACTION, event -> {

                        String name = nameField.getText().trim();
                        String email = emailField.getText().trim();

                        if (name.isEmpty() || name.length() > 100) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Name",
                                                "Student name must contain 1 to 100 characters.");
                                event.consume();
                                return;
                        }

                        if (!validEmail(email)) {
                                showAlert(Alert.AlertType.ERROR,
                                                "Invalid Email",
                                                "Enter a valid email address (maximum 150 characters).");
                                event.consume();
                        }
                });

                dialog.setResultConverter(button -> button);

                dialog.showAndWait().ifPresent(result -> {

                        if (result == updateButton) {
                                try {
                                        Student updatedStudent = new Student(
                                                        selectedStudent.getStudentId(),
                                                        nameField.getText().trim(),
                                                        emailField.getText().trim());

                                        studentManager.updateStudent(updatedStudent);

                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Success", "Student updated successfully!");

                                        loadStudents();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Update Failed", e.getMessage());
                                }
                        }
                });
        }

        // ---------------- DELETE STUDENT ----------------

        private void deleteSelectedStudent() {

                Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

                if (selectedStudent == null) {
                        showAlert(Alert.AlertType.WARNING,
                                        "No Student Selected",
                                        "Please select a student from the table to delete.");
                        return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Delete Student");
                confirmation.setHeaderText(
                                "Are you sure you want to delete this student?");

                confirmation.setContentText(
                                "Student: " + selectedStudent.getStudentName()
                                                + "\nStudent ID: " + selectedStudent.getStudentId());

                confirmation.showAndWait().ifPresent(response -> {

                        if (response == ButtonType.OK) {
                                try {
                                        studentManager.deleteStudent(
                                                        selectedStudent.getStudentId());

                                        showAlert(Alert.AlertType.INFORMATION,
                                                        "Success", "Student deleted successfully!");

                                        loadStudents();

                                } catch (IllegalArgumentException | IllegalStateException e) {
                                        showAlert(Alert.AlertType.ERROR,
                                                        "Deletion Failed", e.getMessage());
                                }
                        }
                });
        }

        // ---------------- VALIDATION AND ALERTS ----------------

        private boolean validEmail(String email) {
                return email != null
                                && !email.isEmpty()
                                && email.length() <= 150
                                && email.matches(
                                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
