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

/**
 * ✅ UPDATED: AdminAuthNavManager with concurrent login support
 */
public class AdminAuthNavManager {
    private Stage authStage;
    private StackPane authStackPane;
    private Scene authScene;
    private Pane loginPane;
    private Pane signupPane;

    private AdminLoginController loginController;
    private AdminSignupController signupController;
    private AdminLoginView loginView;

    public AdminAuthNavManager(Stage authStage) {
        this.authStage = authStage;
        this.authStackPane = new StackPane();
        this.authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);
        initializeAuthViews();
    }

    private void initializeAuthViews() {
        try {
            // Load Login View
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminLogin.fxml"));
            loginPane = loginLoader.load();
            loginView = loginLoader.getController();
            AdminLoginModel loginModel = new AdminLoginModel();
            loginController = new AdminLoginController(loginView, loginModel, this);

            // Load Signup View
            FXMLLoader signupLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminSignup.fxml"));
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
        authStage.setTitle("Admin Login");
        authStage.show();
    }

    public void showSignupView() {
        authStackPane.getChildren().clear();
        authStackPane.getChildren().add(signupPane);
        authStage.setTitle("Admin Signup");
    }

    /**
     * ✅ UPDATED: Now accepts sessionToken parameter
     */
    public void openAdminHome(Admin admin, String sessionToken) {
        // Close auth stage and open home in new stage
        Stage homeStage = new Stage();
        new AdminNavigationManager(homeStage, this, admin, sessionToken); // ✅ Pass token
        authStage.close();
    }

    /**
     * ✅ UPDATED: Can now show concurrent login message
     */
    public void reopenAuthStage(String message) {
        // Create fresh instances for returning to auth
        authStage = new Stage();
        authStackPane = new StackPane();
        authScene = new Scene(authStackPane, 600, 400);
        authStage.setScene(authScene);

        // Reinitialize views
        initializeAuthViews();

        // Show message if provided
        if (message != null && !message.isEmpty() && loginView != null) {
            loginView.showError(message);
        }

        showLoginView();
    }

    /**
     * ✅ NEW: Overload for backward compatibility
     */
    public void reopenAuthStage() {
        reopenAuthStage(null);
    }
}