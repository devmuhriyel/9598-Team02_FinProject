package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthenticationManager;

public class MainAdminApp extends Application {

    public static void main(String[] args) {
        // Initialize CORBA connection BEFORE launching JavaFX
        AdminParentModel.initializeCORBAConnection(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        // Initialize your authentication manager
        new AdminAuthenticationManager(root);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Admin Login");
        primaryStage.show();
    }
}