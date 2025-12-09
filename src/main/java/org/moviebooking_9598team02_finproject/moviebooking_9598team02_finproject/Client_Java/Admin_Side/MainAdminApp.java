package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;

public class MainAdminApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminConnection.fxml"));
            Pane connectionPane = loader.load();
            AdminParentView connectionView = loader.getController();

            AdminHomeModel connectionModel = new AdminHomeModel();

            // Just use AdminParentController directly with a callback
            new AdminParentController(connectionModel, connectionView, () -> {
                primaryStage.close();
                Stage authStage = new Stage();
                new AdminAuthNavManager(authStage);
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