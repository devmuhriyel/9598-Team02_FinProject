package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;

public class MainCustomerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerConnection.fxml"));
            Pane connectionPane = loader.load();
            CustomerParentView connectionView = loader.getController();

            CustomerHomeModel connectionModel = new CustomerHomeModel();

            // Just use CustomerParentController directly with a callback
            new CustomerParentController(connectionModel, connectionView, () -> {
                primaryStage.close();
                Stage authStage = new Stage();
                new CustomerAuthNavManager(authStage);
            });

            Scene scene = new Scene(connectionPane, 600, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Connect to Server");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}