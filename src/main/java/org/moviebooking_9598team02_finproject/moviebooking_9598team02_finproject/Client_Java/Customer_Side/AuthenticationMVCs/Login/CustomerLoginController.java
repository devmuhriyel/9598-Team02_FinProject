package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CustomerLoginResponse;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;

/**
 * ✅ UPDATED: Now passes sessionToken to navigation manager
 */
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
            CustomerLoginResponse response = model.logInCustomer(username, password);
            customer = response.customer;

            view.clearFields();
            view.clearError();

            // ✅ FIX: Pass BOTH customer and sessionToken to home
            authManager.openCustomerHome(customer, response.sessionToken);

        } catch (AuthenticationException e) {
            view.showError(e.message != null && !e.message.isEmpty() ? e.message : "Invalid username or password");
        }
    }

    public void switchToSignUp() {
        authManager.showSignupView();
    }
}