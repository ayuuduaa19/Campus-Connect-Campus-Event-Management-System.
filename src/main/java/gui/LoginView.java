
package main.java.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import main.java.manager.AuthManager;
import main.java.model.User;

public class LoginView extends VBox {

    private final AuthManager authManager;
    private final Stage primaryStage;

    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleBox;

    public LoginView(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.authManager = new AuthManager();

        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #EAF2F8;");

        Label title = new Label("Campus Connect");
        title.setFont(Font.font("Arial", 32));
        title.setTextFill(Color.web("#1B365D"));

        Label subtitle = new Label("Campus Event Management System");
        subtitle.setFont(Font.font("Arial", 15));
        subtitle.setTextFill(Color.web("#555555"));

        Label loginHeading = new Label("Login to Your Account");
        loginHeading.setFont(Font.font("Arial", 20));
        loginHeading.setTextFill(Color.web("#075B9B"));

        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Administrator", "Student");
        roleBox.setPromptText("Select account type");
        roleBox.setMaxWidth(320);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(320);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(320);
        loginButton.setPrefHeight(40);
        loginButton.setStyle(
                "-fx-background-color: #075B9B;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-radius: 6;");

        loginButton.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        getChildren().addAll(
                title, subtitle, loginHeading,
                roleBox, usernameField, passwordField, loginButton);
    }

    private void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();
        String selectedRole = roleBox.getValue();

        if (selectedRole == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Select Account Type",
                    "Please select Administrator or Student.");
            roleBox.requestFocus();
            return;
        }

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {

            showAlert(Alert.AlertType.WARNING,
                    "Missing Details",
                    "Please enter both username and password.");
            return;
        }

        try {

            User user = authManager.login(username, password);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR,
                        "Login Failed",
                        "Invalid username or password.");

                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            if ((selectedRole.equals("Administrator") && !user.isAdmin())
                    || (selectedRole.equals("Student") && !user.isStudent())) {

                showAlert(Alert.AlertType.ERROR,
                        "Role Mismatch",
                        "Your account does not belong to the selected login type.");

                passwordField.clear();
                return;
            }

            // A temporary password must be changed before dashboard access.
            if (user.isStudent() && user.isMustChangePassword()) {

                boolean changed = new ChangePasswordDialog()
                        .showTemporaryPasswordChange(user);

                if (!changed) {
                    return;
                }
            }

            if (user.isAdmin()) {

                primaryStage.setTitle("Campus Connect | Administrator");
                primaryStage.setScene(
                        new Scene(new AdminDashboard(user), 1100, 700));

            } else if (user.isStudent()) {

                primaryStage.setTitle("Campus Connect | Student");
                primaryStage.setScene(
                        new Scene(new StudentDashboard(user), 1100, 700));
            }

        } catch (IllegalStateException e) {

            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unable to connect to the database.");

            passwordField.clear();

        } catch (RuntimeException e) {

            showAlert(Alert.AlertType.ERROR,
                    "Login Error",
                    "Something went wrong while logging in. Please try again.");

            passwordField.clear();
        }
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
