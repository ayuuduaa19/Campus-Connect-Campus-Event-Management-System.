
package main.java.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSetup {

    public static void main(String[] args) {

        String[] queries = {

                """
                        CREATE TABLE IF NOT EXISTS events (
                            event_id INT PRIMARY KEY,
                            event_name VARCHAR(100) NOT NULL,
                            venue VARCHAR(100) NOT NULL,
                            event_date DATE NOT NULL,
                            capacity INT NOT NULL CHECK (capacity > 0),
                            category VARCHAR(50) NOT NULL DEFAULT 'Other',
                            description TEXT,
                            start_time TIME NOT NULL DEFAULT '09:00:00',
                            end_time TIME NOT NULL DEFAULT '17:00:00',
                            registration_deadline DATE NOT NULL,
                            poster_path VARCHAR(500),
                            is_cancelled BOOLEAN NOT NULL DEFAULT FALSE
                        )
                        """,

                """
                        CREATE TABLE IF NOT EXISTS students (
                            student_id INT PRIMARY KEY,
                            student_name VARCHAR(100) NOT NULL,
                            email VARCHAR(150) NOT NULL UNIQUE
                        )
                        """,

                """
                        CREATE TABLE IF NOT EXISTS registrations (
                            registration_id INT AUTO_INCREMENT PRIMARY KEY,
                            student_id INT NOT NULL,
                            event_id INT NOT NULL,
                            attendance_status VARCHAR(20)
                                NOT NULL DEFAULT 'Not Marked',
                            FOREIGN KEY (student_id)
                                REFERENCES students(student_id),
                            FOREIGN KEY (event_id)
                                REFERENCES events(event_id),
                            UNIQUE (student_id, event_id),
                            CHECK (attendance_status IN
                                ('Not Marked', 'Present', 'Absent'))
                        )
                        """,

                """
                        CREATE TABLE IF NOT EXISTS users (
                            user_id INT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) NOT NULL UNIQUE,
                            password_hash VARCHAR(255) NOT NULL,
                            password_salt VARCHAR(255) NOT NULL,
                            role ENUM('ADMIN', 'STUDENT') NOT NULL,
                            student_id INT UNIQUE,
                            FOREIGN KEY (student_id)
                                REFERENCES students(student_id)
                        )
                        """
        };

        try (Connection connection = DBConnection.getConnection();
                Statement statement = connection.createStatement()) {

            for (String query : queries) {
                statement.executeUpdate(query);
            }

            System.out.println(
                    "All database tables checked successfully!");

        } catch (SQLException e) {
            System.out.println("Database setup failed.");
            e.printStackTrace();
        }
    }
}
