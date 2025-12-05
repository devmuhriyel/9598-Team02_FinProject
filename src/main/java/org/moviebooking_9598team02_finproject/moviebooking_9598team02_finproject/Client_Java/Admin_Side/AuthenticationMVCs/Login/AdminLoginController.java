package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminNavigationManager;

public class AdminLoginController {
    private AdminLoginView view;
    private AdminLoginModel model;
    private AdminNavigationManager authManager;
    private Admin admin;

    public AdminLoginController(AdminLoginView loginView, AdminLoginModel loginModel, AdminNavigationManager authManager) {
        this.view = loginView;
        this.model = loginModel;
        this.authManager = authManager;
        initializeButtons();
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
        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        }
    }

    public void switchToSignUp() {
        authManager.showSignupView();
    }
}