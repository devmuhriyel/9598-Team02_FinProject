package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.application.Application;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;

public class MainAdminApp extends Application {

    public static void main(String[] args) {
        // Initialize CORBA connection BEFORE launching JavaFX
        AdminParentModel.initializeCORBAConnection(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize your authentication manager
        new AdminAuthNavManager(primaryStage);
    }
}