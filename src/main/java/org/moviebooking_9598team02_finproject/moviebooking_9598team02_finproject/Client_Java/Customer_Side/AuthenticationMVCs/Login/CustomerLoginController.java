package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;

public class CustomerLoginController {
    private CustomerLoginView view;
    private CustomerLoginModel model;
    private CustomerAuthNavManager authManager;
    private Customer customer;

    public CustomerLoginController(CustomerLoginView loginView, CustomerLoginModel loginModel, CustomerAuthNavManager authManager) {
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
            customer = model.logInCustomer(username, password);
            view.clearFields();
            view.clearError();
            authManager.openCustomerHome(customer);
        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        }
    }

    public void switchToSignUp() {
        authManager.showSignupView();
    }
}