package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupView;

import java.io.IOException;

public class AdminNavigationManager {
    private StackPane stackPane;
    private Pane loginPane;
    private Pane signupPane;

    private AdminLoginController loginController;
    private AdminSignupController signupController;

    public AdminNavigationManager(StackPane stackPane) {
        this.stackPane = stackPane;
        initializeViews();
    }

    private void initializeViews() {
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
        stackPane.getChildren().clear();
        stackPane.getChildren().add(loginPane);
    }

    public void showSignupView() {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(signupPane);
    }
}