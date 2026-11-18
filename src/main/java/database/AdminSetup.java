
package main.java.database;

import java.util.Scanner;

import main.java.manager.AuthManager;

public class AdminSetup {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        AuthManager authManager = new AuthManager();

        System.out.println("===== ADMIN ACCOUNT SETUP =====");

        System.out.print("Enter admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();

        boolean created = authManager.createInitialAdmin(
                username,
                password);

        if (created) {
            System.out.println("\nAdmin account created successfully!");
        } else {
            System.out.println(
                    "\nCould not create admin account. "
                            + "Check the username, password length and database.");
        }

        scanner.close();
    }
}
