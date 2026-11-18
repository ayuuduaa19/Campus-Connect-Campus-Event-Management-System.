
package main.java.model;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String role;
    private Integer studentId;
    private boolean mustChangePassword;

    public User(
            int userId,
            String username,
            String passwordHash,
            String passwordSalt,
            String role,
            Integer studentId) {

        this(userId, username, passwordHash, passwordSalt,
                role, studentId, false);
    }

    public User(
            int userId,
            String username,
            String passwordHash,
            String passwordSalt,
            String role,
            Integer studentId,
            boolean mustChangePassword) {

        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.studentId = studentId;
        this.mustChangePassword = mustChangePassword;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public String getRole() {
        return role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }
}
