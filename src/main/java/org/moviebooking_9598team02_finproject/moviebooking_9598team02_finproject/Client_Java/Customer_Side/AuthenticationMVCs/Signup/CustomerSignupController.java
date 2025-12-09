package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminNavigationManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerNavigationManager;

public class CustomerSignupController {
    private CustomerSignupView view;
    private CustomerSignupModel model;
    private CustomerAuthNavManager authManager;

    public CustomerSignupController(CustomerSignupView signupView, CustomerSignupModel signupModel, CustomerAuthNavManager authManager) {
        this.view = signupView;
        this.model = signupModel;
        this.authManager = authManager;
        initializeButtons();
    }

    private void initializeButtons() {
        view.getSignUpButton().setOnAction(event -> signUp());
        view.getLogInLink().setOnAction(event -> switchToLogin());
    }

    private void signUp() {
        view.clearError();
        if (view.getUserName().isEmpty() || view.getFullName().isEmpty() ||
                view.getPhoneNumber().isEmpty() || view.getEmail().isEmpty() ||
                view.getPassword().isEmpty() || view.getConfirmPassword().isEmpty()) {
            view.showError("Please fill in all fields");
            return;
        }

        try {
            model.signUpCustomer(
                    view.getUserName(),
                    view.getFullName(),
                    view.getPhoneNumber(),
                    view.getEmail(),
                    view.getPassword(),
                    view.getConfirmPassword()
            );

            view.clearFields();
            view.clearError();
            authManager.showLoginView();

        } catch (InvalidInputException | AuthenticationException e) {
            view.showError(e.getMessage());
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }

    private void switchToLogin() {
        authManager.showLoginView();
    }
}