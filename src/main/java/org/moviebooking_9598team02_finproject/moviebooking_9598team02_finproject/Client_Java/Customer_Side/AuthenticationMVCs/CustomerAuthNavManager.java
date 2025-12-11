package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerNavigationManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup.CustomerSignupView;

import java.io.IOException;

public class CustomerAuthNavManager {
    private Stage authStage;
    private StackPane authStackPane;
    private Scene authScene;
    private Pane loginPane;
    private Pane signupPane;

    private CustomerLoginController loginController;
    private CustomerSignupController signupController;

    public CustomerAuthNavManager(Stage authStage) {
        this.authStage = authStage;
        this.authStackPane = new StackPane();
        this.authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);
        initializeAuthViews();
    }

    private void initializeAuthViews() {
        try {
            // Load Login View
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerLogin.fxml"));
            loginPane = loginLoader.load();
            CustomerLoginView loginView = loginLoader.getController();
            CustomerLoginModel loginModel = new CustomerLoginModel();
            loginController = new CustomerLoginController(loginView, loginModel, this);

            // Load Signup View
            FXMLLoader signupLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerSignup.fxml"));
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
        authStackPane.getChildren().clear();
        authStackPane.getChildren().add(loginPane);
        authStage.setTitle("Customer Login");
        authStage.show();
    }

    public void showSignupView() {
        authStackPane.getChildren().clear();
        authStackPane.getChildren().add(signupPane);
        authStage.setTitle("Customer Signup");
    }

    public void openCustomerHome(Customer customer) {
        // Close auth stage and open home in new stage
        Stage homeStage = new Stage();
        new CustomerNavigationManager(homeStage, this, customer);
        authStage.close();
    }

    public void reopenAuthStage() {
        // Create fresh instances for returning to auth
        authStage = new Stage();
        authStackPane = new StackPane();
        authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);
        showLoginView();
    }
}