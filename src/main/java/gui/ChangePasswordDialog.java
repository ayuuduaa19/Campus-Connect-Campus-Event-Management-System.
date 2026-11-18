
package main.java.gui;

import main.java.manager.AuthManager;
import main.java.model.User;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

public class ChangePasswordDialog {

    private final AuthManager authManager;

    public ChangePasswordDialog() {
        authManager = new AuthManager();
    }

    public void show(User user) {
        showDialog(user, false);
    }

    public boolean showTemporaryPasswordChange(User user) {
        if (user == null) {
            return false;
        }

        return showDialog(user, true);
    }

    private boolean showDialog(User user, boolean forced) {

        if (user == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "User account information is unavailable.");
            return false;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        final boolean[] updated = { false };

        dialog.setTitle(forced
                ? "Set Your New Password"
                : "Change Password");

        dialog.setHeaderText(forced
                ? "Your temporary password must be changed before continuing."
                : "Update Your Account Password");

        ButtonType changeButton = new ButtonType(
                forced ? "Set New Password" : "Change Password",
                ButtonBar.ButtonData.OK_DONE);

        if (forced) {
            dialog.getDialogPane().getButtonTypes().add(changeButton);

            // Prevent closing until the password is successfully changed.
            dialog.setOnCloseRequest(event -> {
                if (!updated[0]) {
                    event.consume();
                }
            });

        } else {
            dialog.getDialogPane().getButtonTypes().addAll(
                    changeButton,
                    ButtonType.CANCEL);
        }

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText(
                forced ? "Enter temporary password" : "Enter current password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label(forced
                ? "Temporary Password:"
                : "Current Password:"), 0, 0);

        grid.add(currentPasswordField, 1, 0);

        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);

        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button changeButtonNode = (Button) dialog.getDialogPane().lookupButton(changeButton);

        // Replace JavaFX's default button handling with our own.
        changeButtonNode.setOnAction(event -> {

            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (currentPassword.isEmpty()
                    || newPassword.isEmpty()
                    || confirmPassword.isEmpty()) {

                showAlert(Alert.AlertType.WARNING,
                        "Missing Fields",
                        "Please fill in all password fields.");
                return;
            }

            if (newPassword.length() < 8) {
                showAlert(Alert.AlertType.WARNING,
                        "Weak Password",
                        "Your new password must contain at least 8 characters.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.WARNING,
                        "Password Mismatch",
                        "The new passwords do not match.");
                return;
            }

            if (currentPassword.equals(newPassword)) {
                showAlert(Alert.AlertType.WARNING,
                        "Same Password",
                        "Your new password must be different from your current password.");
                return;
            }

            try {
                boolean result = authManager.changePassword(
                        user.getUserId(),
                        currentPassword,
                        newPassword,
                        confirmPassword);

                if (result) {

                    updated[0] = true;
                    user.setMustChangePassword(false);

                    showAlert(Alert.AlertType.INFORMATION,
                            "Success",
                            "Your password has been changed successfully.");

                    // Set the result and explicitly close the dialog.
                    dialog.setResult(changeButton);
                    dialog.close();

                } else {
                    showAlert(Alert.AlertType.ERROR,
                            "Password Change Failed",
                            "Current password is incorrect or the update failed.");
                }

            } catch (IllegalStateException ex) {

                showAlert(Alert.AlertType.ERROR,
                        "Database Error",
                        ex.getMessage());

            } catch (RuntimeException ex) {

                showAlert(Alert.AlertType.ERROR,
                        "Error",
                        "Something went wrong while changing your password.");
            }
        });

        dialog.showAndWait();

        return updated[0];
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
