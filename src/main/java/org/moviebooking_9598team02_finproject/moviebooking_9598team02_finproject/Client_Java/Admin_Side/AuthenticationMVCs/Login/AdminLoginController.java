package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

public class AdminLoginController {
    private AdminLoginView view;
    private AdminLoginModel model;
    private Admin admin;

    public AdminLoginController(AdminLoginView loginView, AdminLoginModel loginModel) {
        this.view = loginView;
        this.model = loginModel;
    }

    private void intializeButtons() {
        view.getLoginButton().setOnAction(event -> login());
        view.getCreateAccountLink().setOnAction(event -> switchToSignUp());
    }

    public void login() {
        String username = view.getUsername();
        String password = view.getPassword();

        if (username.isEmpty()) {
            view.showError("Input your usename");
            return;
        } else if (password.isEmpty()) {
            view.showError("Input your password");
            return;
        }

        try{
            admin = model.loginAdmin(username, password);
            view.clearFields();
            view.clearError();
        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        }

    }
    public void switchToSignUp(){

    }
}
