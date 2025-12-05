package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup.AdminSignupView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupView;

import java.io.IOException;

public class CustomerNavigationManager {
    private StackPane stackPane;
    private Pane loginPane;
    private Pane signupPane;

    private CustomerLoginController loginController;
    private CustomerSignupController signupController;

    public CustomerNavigationManager(StackPane stackPane) {
        this.stackPane = stackPane;
        initializeViews();
    }

    private void initializeViews() {
        try {
            // Load Login View
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerLogin.fxml"));
            loginPane = loginLoader.load();
            CustomerLoginView loginView = loginLoader.getController();
            CustomerLoginModel loginModel = new CustomerLoginModel();
            loginController = new CustomerLoginController(loginView, loginModel, this);

            // Load Signup View
            FXMLLoader signupLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerSignup.fxml"));
            signupPane = signupLoader.load();
            CustomerSignupView signupView = signupLoader.getController();
            CustomerSignupModel signupModel = new CustomerSignupModel();
            signupController = new CustomerSignupController(signupView, signupModel, this);

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