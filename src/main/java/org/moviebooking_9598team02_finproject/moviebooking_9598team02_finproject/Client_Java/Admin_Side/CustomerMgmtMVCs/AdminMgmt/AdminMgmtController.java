package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;

/**
 * AdminMgmtController - Pure business logic, no JavaFX imports
 */
public class AdminMgmtController {
    private AdminMgmtModel model;
    private AdminMgmtView view;
    private Admin[] adminArrays;

    public AdminMgmtController(AdminMgmtModel model, AdminMgmtView view) throws AuthenticationException {
        this.model = model;
        this.view = view;
        initializeButtons();
        loadAllAdmins();
    }

    // ===== BUTTON INITIALIZATION =====
    private void initializeButtons() {
        // List Panel Buttons
        view.getCreateButton().setOnAction(event -> view.showCreatePanel());
        view.getSearchButton().setOnAction(event -> handleSearchButtonClick());
        view.getRefreshButton().setOnAction(event -> handleRefreshButtonClick());

        // Detail Panel - Basic Info
        view.getSaveButton().setOnAction(event -> handleSaveProfileButtonClick());
        view.getBackToListButton().setOnAction(event -> {
            view.showListPanel();
            view.clearDetailMessage();
        });

        // Detail Panel - Change Email Form
        view.getSubmitEmailButton().setOnAction(event -> handleSubmitEmailButtonClick());

        // Detail Panel - Change Password Form
        view.getSubmitPasswordButton().setOnAction(event -> handleSubmitPasswordButtonClick());

        // Detail Panel - Delete
        view.getDeleteButton().setOnAction(event -> handleDeleteButtonClick());

        // Create Panel Buttons
        view.getCreateSaveButton().setOnAction(event -> handleCreateSaveButtonClick());
        view.getCreateCancelButton().setOnAction(event -> view.showListPanel());
    }

    // ===== LIST PANEL HANDLERS =====
    private void handleSearchButtonClick() {
        String searchText = view.getSearchField().getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            view.showListMessage("Please enter a search term");
            return;
        }

        try {
            view.clearListMessage();

            if (searchText.matches("\\d+")) {
                Admin admin = model.getAdminById(searchText);
                if (admin != null) {
                    Admin[] result = new Admin[1];
                    result[0] = admin;
                    view.displayAdminList(result);
                    view.showListMessage("Found 1 admin");
                } else {
                    view.displayAdminList(new Admin[0]);
                    view.showListMessage("No admin found with ID: " + searchText);
                }
            } else {
                Admin[] results = model.searchAdmins(searchText);
                view.displayAdminList(results);
                view.showListMessage("Found " + results.length + " admin(s)");
            }
        } catch (AuthenticationException e) {
            view.showListMessage("Error: " + e.getMessage());
        }
    }

    private void handleRefreshButtonClick() {
        try {
            view.clearListMessage();
            loadAllAdmins();
            view.showListMessage("Admin list refreshed");
        } catch (AuthenticationException e) {
            view.showListMessage("Error: " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - SAVE PROFILE =====
    private void handleSaveProfileButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);

            String fullName = view.getDetailFullNameField().getText();
            String phoneNumber = view.getDetailPhoneField().getText();

            if (fullName == null || fullName.trim().isEmpty()) {
                view.showDetailMessage("Full name cannot be empty");
                return;
            }

            view.clearDetailMessage();
            model.updateAdminProfile(targetAdminId, fullName, phoneNumber);
            view.showDetailMessage("✓ Profile updated successfully!");

            try {
                loadAllAdmins();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            view.showDetailMessage("Error: Invalid admin ID");
        } catch (AuthenticationException e) {
            view.showDetailMessage("Error: " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - CHANGE EMAIL FORM =====
    private void handleSubmitEmailButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);

            String newEmail = view.getNewEmailField().getText();
            String password = view.getEmailPasswordField().getText();

            // Validate inputs
            if (newEmail == null || newEmail.trim().isEmpty()) {
                view.showDetailMessage("Email cannot be empty");
                return;
            }

            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                view.showDetailMessage("Invalid email format");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                view.showDetailMessage("This admin's password is required to change their email");
                return;
            }

            view.clearDetailMessage();
            model.updateAdminEmail(targetAdminId, newEmail, password);

            // Update the email label
            view.getDetailEmailLabel().setText(newEmail);

            // Clear the form
            view.getNewEmailField().clear();
            view.getEmailPasswordField().clear();

            view.showDetailMessage("Email updated successfully!");

            try {
                loadAllAdmins();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            view.showDetailMessage("Error: Invalid admin ID");
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Incorrect password")) {
                view.showDetailMessage("Incorrect password for this admin account");
            } else {
                view.showDetailMessage("Error: " + e.getMessage());
            }
        } catch (InvalidInputException e) {
            if (e.getMessage().contains("Email already exists")) {
                view.showDetailMessage("This email is already in use");
            } else {
                view.showDetailMessage("Error: " + e.getMessage());
            }
        }
    }

    // ===== DETAIL PANEL - CHANGE PASSWORD FORM =====
    private void handleSubmitPasswordButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);

            String oldPassword = view.getOldPasswordField().getText();
            String newPassword = view.getNewPasswordField().getText();
            String confirmPassword = view.getConfirmPasswordField().getText();

            // Validate inputs
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                view.showDetailMessage("Current password is required");
                return;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                view.showDetailMessage("New password is required");
                return;
            }

            if (newPassword.length() < 6) {
                view.showDetailMessage("Password must be at least 6 characters");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                view.showDetailMessage("Passwords do not match");
                return;
            }

            view.clearDetailMessage();
            model.changeAdminPassword(targetAdminId, oldPassword, newPassword);

            // Clear the form
            view.getOldPasswordField().clear();
            view.getNewPasswordField().clear();
            view.getConfirmPasswordField().clear();

            view.showDetailMessage("✓ Password changed successfully!");

        } catch (NumberFormatException e) {
            view.showDetailMessage("Error: Invalid admin ID");
        } catch (AuthenticationException e) {
            view.showDetailMessage("Error: " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - DELETE =====
    private void handleDeleteButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);
            String username = view.getDetailUsernameLabel().getText();

            // Show confirmation dialog via view
            boolean confirmed = view.showDeleteConfirmation(username);

            if (confirmed) {
                view.clearDetailMessage();
                model.deleteAdmin(targetAdminId);
                view.showListPanel();
                loadAllAdmins();
            }

        } catch (NumberFormatException e) {
            view.showDetailMessage("Error: Invalid admin ID");
        } catch (AuthenticationException e) {
            view.showDetailMessage("Error: " + e.getMessage());
        }
    }

    // ===== CREATE PANEL HANDLERS =====
    private void handleCreateSaveButtonClick() {
        try {
            String username = view.getCreateUsernameField().getText();
            String email = view.getCreateEmailField().getText();
            String password = view.getCreatePasswordField().getText();
            String confirmPassword = view.getCreateConfirmPasswordField().getText();
            String fullName = view.getCreateFullNameField().getText();
            String phoneNumber = view.getCreatePhoneField().getText();

            if (username == null || username.trim().isEmpty()) {
                view.showCreateMessage("Username is required");
                return;
            }
            if (email == null || email.trim().isEmpty()) {
                view.showCreateMessage("Email is required");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                view.showCreateMessage("Invalid email format");
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                view.showCreateMessage("Password is required");
                return;
            }
            if (password.length() < 6) {
                view.showCreateMessage("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirmPassword)) {
                view.showCreateMessage("Passwords do not match");
                return;
            }

            view.clearCreateMessage();

            int newAdminId = model.createAdmin(username, email, password, fullName, phoneNumber);

            // Show success message via view
            view.showSuccessAlert("Admin Created", "Admin account created successfully!\nAdmin ID: " + newAdminId);

            view.showListPanel();
            loadAllAdmins();

        } catch (AuthenticationException e) {
            view.showCreateMessage("Error: " + e.getMessage());
        } catch (InvalidInputException e) {
            view.showCreateMessage("Error: " + e.getMessage());
        }
    }

    // ===== DATA LOADING =====
    public void loadAllAdmins() throws AuthenticationException {
        adminArrays = model.loadAllAdmins();
        view.displayAdminList(adminArrays);
    }
}