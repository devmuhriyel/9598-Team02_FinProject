package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminNavigationManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupView;

import java.io.IOException;

public class AdminAuthNavManager {
    private Stage authStage;
    private StackPane authStackPane;
    private Pane loginPane;
    private Pane signupPane;

    private AdminLoginController loginController;
    private AdminSignupController signupController;

    public AdminAuthNavManager(Stage authStage) {
        this.authStage = authStage;
        this.authStackPane = new StackPane();
        initializeAuthViews();
    }

    private void initializeAuthViews() {
        try {
            // Load Login View
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminLogin.fxml"));
            loginPane = loginLoader.load();
            AdminLoginView loginView = loginLoader.getController();
            AdminLoginModel loginModel = new AdminLoginModel();
            loginController = new AdminLoginController(loginView, loginModel, this);

            // Load Signup View
            FXMLLoader signupLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminSignup.fxml"));
            signupPane = signupLoader.load();
            AdminSignupView signupView = signupLoader.getController();
            AdminSignupModel signupModel = new AdminSignupModel();
            signupController = new AdminSignupController(signupView, signupModel, this);

            // Show login view by default
            showLoginView();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginView() {
        authStackPane.getChildren().clear();
        authStackPane.getChildren().add(loginPane);

        Scene authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);
        authStage.setTitle("Admin Login");
        authStage.show();
    }

    public void showSignupView() {
        authStackPane.getChildren().clear();
        authStackPane.getChildren().add(signupPane);

        Scene authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);
        authStage.setTitle("Admin Signup");
    }

    public void openAdminHome(Admin admin) {
        // Close auth stage and open home in new stage
        Stage homeStage = new Stage();
        new AdminNavigationManager(homeStage, this, admin);
        authStage.close();
    }

    public void reopenAuthStage() {
        showLoginView();
    }
}