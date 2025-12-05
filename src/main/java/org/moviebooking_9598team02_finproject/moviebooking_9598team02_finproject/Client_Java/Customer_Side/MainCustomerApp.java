package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainCustomerApp extends Application {

    public static void main(String[] args) {
        // Initialize CORBA connection BEFORE launching JavaFX
        CustomerParentModel.initializeCORBAConnection(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        // Initialize your authentication manager
        new CustomerNavigationManager(root);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Customer Login");
        primaryStage.show();
    }
}