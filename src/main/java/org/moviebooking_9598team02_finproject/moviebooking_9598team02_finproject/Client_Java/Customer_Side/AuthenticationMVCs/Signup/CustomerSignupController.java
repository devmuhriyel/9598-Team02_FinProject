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
        view.clearError();
    }

    private void initializeButtons() {
        view.getSignUpButton().setOnAction(event -> signUp());
        view.getLogInLink().setOnAction(event -> switchToLogin());
    }

    private void signUp() {
        view.clearError();

        // --- START: Refactored Empty Field Check for Specific Errors ---
        if (view.getUserName().isEmpty()) {
            view.showError("Username cannot be empty.");
            return;
        } else if (view.getFullName().isEmpty()) {
            view.showError("Full Name cannot be empty.");
            return;
        } else if (view.getPhoneNumber().isEmpty()) {
            view.showError("Phone Number cannot be empty.");
            return;
        } else if (view.getEmail().isEmpty()) {
            view.showError("Email Address cannot be empty.");
            return;
        } else if (view.getPassword().isEmpty()) {
            view.showError("Password cannot be empty.");
            return;
        } else if (view.getConfirmPassword().isEmpty()) {
            view.showError("Confirmation Password cannot be empty.");
            return;
        }
        // --- END: Refactored Empty Field Check ---

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
            // Already displays specific server-side errors (e.g., "Username already taken")
            view.showError(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Already displays specific client-side errors (e.g., "Passwords do not match")
            view.showError(e.getMessage());
        }
    }

    private void switchToLogin() {
        view.clearError(); // Clear error before switching
        authManager.showLoginView();
    }
}
