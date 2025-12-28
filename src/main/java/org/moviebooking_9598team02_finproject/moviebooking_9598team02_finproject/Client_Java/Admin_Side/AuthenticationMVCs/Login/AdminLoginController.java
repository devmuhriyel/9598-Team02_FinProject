package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;

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
            admin = model.logInAdmin(username, password);
            view.clearFields();
            view.clearError();

            // Session validation will be handled by AdminNavigationManager
            authManager.openAdminHome(admin);
        } catch (AuthenticationException e) {
            // ✅ FIX: Show actual error message from server (e.g., "Account already logged in")
            view.showError(e.message != null && !e.message.isEmpty() ? e.message : "Invalid username or password");
        }
    }

    public void switchToSignUp() {
        authManager.showSignupView();
    }
}