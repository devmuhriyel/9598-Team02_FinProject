package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

public class CustomerLoginController {
    private CustomerLoginView view;
    private CustomerLoginModel model;
    private Customer customer;

    public CustomerLoginController(CustomerLoginView loginView, CustomerLoginModel loginModel) {
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
            customer = model.loginCustomer(username, password);
            view.clearFields();
            view.clearError();
        } catch (AuthenticationException e) {
            view.showError(e.getMessage());
        }

    }
    public void switchToSignUp(){

    }
}
