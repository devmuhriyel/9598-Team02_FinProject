package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AdminLoginResponse;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;

/**
 * ✅ UPDATED: Now passes sessionToken to navigation manager
 */
public class AdminLoginController {
    private AdminLoginView view;
    private AdminLoginModel model;
    private AdminAuthNavManager authManager;
    private Admin admin;

    public AdminLoginController(AdminLoginView loginView, AdminLoginModel loginModel, AdminAuthNavManager authManager) {
        this.view = loginView;
        this.model = loginModel;
        this.authManager = authManager;
        initializeButtons();
        view.clearError();
    }

    private void initializeButtons() {
        view.getLoginButton().setOnAction(event -> login());
        view.getCreateAccountLink().setOnAction(event -> switchToSignUp());
    }

    public void login() {
        String username = view.getUserName();
        String password = view.getPassword();

        if (username.isEmpty()) {
            view.showError("Input your username");
            return;
        } else if (password.isEmpty()) {
            view.showError("Input your password");
            return;
        }

        try {
            // Get full response with sessionToken
            AdminLoginResponse response = model.logInAdmin(username, password);
            admin = response.admin;

            view.clearFields();
            view.clearError();

            // ✅ FIX: Pass BOTH admin and sessionToken to home
            authManager.openAdminHome(admin, response.sessionToken);

        } catch (AuthenticationException e) {
            view.showError(e.message != null && !e.message.isEmpty() ? e.message : "Invalid username or password");
        }
    }

    public void switchToSignUp() {
        authManager.showSignupView();
    }
}