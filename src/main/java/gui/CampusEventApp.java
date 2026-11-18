
package main.java.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CampusEventApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        LoginView loginView = new LoginView(primaryStage);

        Scene scene = new Scene(loginView, 1100, 700);

        primaryStage.setTitle("Campus Connect | Login");
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
