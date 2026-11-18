
package main.java.manager;

import java.security.SecureRandom;
import java.util.Map;

import main.java.dao.UserDAO;
import main.java.model.User;
import main.java.utils.PasswordUtil;

public class AuthManager {

    private final UserDAO userDAO;
    private final SecureRandom random = new SecureRandom();

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$";

    public AuthManager() {
        userDAO = new UserDAO();
    }

    public User login(String username, String password) {

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return null;
        }

        User user = userDAO.findUserByUsername(username.trim());

        if (user == null) {
            return null;
        }

        boolean validPassword = PasswordUtil.verifyPassword(
                password,
                user.getPasswordHash(),
                user.getPasswordSalt());

        return validPassword ? user : null;
    }

    public boolean createStudentAccount(
            String username, String password, int studentId) {

        if (username == null || username.trim().isEmpty()
                || password == null || password.length() < 8
                || studentId <= 0) {
            return false;
        }

        String cleanUsername = username.trim();

        if (userDAO.usernameExists(cleanUsername)) {
            return false;
        }

        return userDAO.createUser(
                cleanUsername, password, "STUDENT", studentId);
    }

    public boolean createInitialAdmin(String username, String password) {

        if (username == null || username.trim().isEmpty()
                || password == null || password.length() < 8) {
            return false;
        }

        String cleanUsername = username.trim();

        if (userDAO.usernameExists(cleanUsername) || userDAO.adminExists()) {
            return false;
        }

        return userDAO.createUser(
                cleanUsername, password, "ADMIN", null);
    }

    public boolean changePassword(
            int userId,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        if (userId <= 0
                || currentPassword == null || currentPassword.isEmpty()
                || newPassword == null || newPassword.length() < 8
                || confirmPassword == null) {
            return false;
        }

        if (!newPassword.equals(confirmPassword)
                || newPassword.equals(currentPassword)) {
            return false;
        }

        User user = userDAO.findUserById(userId);

        if (user == null) {
            return false;
        }

        boolean validPassword = PasswordUtil.verifyPassword(
                currentPassword,
                user.getPasswordHash(),
                user.getPasswordSalt());

        return validPassword && userDAO.updatePassword(userId, newPassword);
    }

    // Admin-only workflow: generate a temporary password for a student.
    public String resetStudentPassword(int studentId) {

        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID.");
        }

        String temporaryPassword = generateTemporaryPassword();

        boolean updated = userDAO.resetStudentPassword(
                studentId, temporaryPassword);

        if (!updated) {
            throw new IllegalArgumentException(
                    "This student does not have a login account.");
        }

        return temporaryPassword;
    }

    public Map<Integer, String> getStudentAccountUsernames() {
        return userDAO.getStudentAccountUsernames();
    }

    private String generateTemporaryPassword() {

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHARS.length());
            password.append(TEMP_PASSWORD_CHARS.charAt(index));
        }

        return password.toString();
    }
}
