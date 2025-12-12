package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;

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
        view.getCreateButton().setOnAction(event -> view.showCreatePanel());
        view.getSearchButton().setOnAction(event -> handleSearchButtonClick());
        view.getRefreshButton().setOnAction(event -> handleRefreshButtonClick());
        view.getSaveButton().setOnAction(event -> handleSaveProfileButtonClick());
        view.getBackToListButton().setOnAction(event -> {
            view.showListPanel();
            view.clearDetailMessage();
        });
        view.getSubmitEmailButton().setOnAction(event -> handleSubmitEmailButtonClick());
        view.getSubmitPasswordButton().setOnAction(event -> handleSubmitPasswordButtonClick());
        view.getDeleteButton().setOnAction(event -> handleDeleteButtonClick());
        view.getCreateSaveButton().setOnAction(event -> handleCreateSaveButtonClick());
        view.getCreateCancelButton().setOnAction(event -> view.showListPanel());
    }

    // ===== LIST PANEL HANDLERS =====

    private void handleSearchButtonClick() {
        String searchText = view.getSearchField().getText();

        // Handle empty search - just refresh the list
        if (searchText == null || searchText.trim().isEmpty()) {
            try {
                loadAllAdmins();
                view.showListMessage("✅ Showing all admins");
            } catch (AuthenticationException e) {
                view.showListMessage("❌ " + e.getMessage());
            }
            return;
        }

        // ✅ VALIDATE using model
        String error = model.validateSearchQuery(searchText);
        if (error != null) {
            view.showListMessage("❌ " + error);
            return;
        }

        try {
            view.clearListMessage();
            String trimmed = searchText.trim();

            if (trimmed.matches("\\d+")) {
                Admin admin = model.getAdminById(trimmed);
                if (admin != null) {
                    Admin[] result = new Admin[1];
                    result[0] = admin;
                    view.displayAdminList(result);
                    view.showListMessage("✅ Found 1 admin");
                } else {
                    view.displayAdminList(new Admin[0]);
                    view.showListMessage("❌ No admin found with ID: " + trimmed);
                }
            } else {
                Admin[] results = model.searchAdmins(trimmed);
                view.displayAdminList(results);
                view.showListMessage("✅ Found " + results.length + " admin(s)");
            }
        } catch (AuthenticationException e) {
            view.showListMessage("❌ " + e.getMessage());
        }
    }

    private void handleRefreshButtonClick() {
        try {
            view.clearListMessage();
            loadAllAdmins();
            view.showListMessage("✅ Admin list refreshed");
        } catch (AuthenticationException e) {
            view.showListMessage("❌ " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - SAVE PROFILE =====

    private void handleSaveProfileButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);
            String fullName = view.getDetailFullNameField().getText();
            String phoneNumber = view.getDetailPhoneField().getText();

            // ✅ VALIDATE using model
            String error = model.validateProfileUpdate(fullName, phoneNumber);
            if (error != null) {
                view.showDetailMessage("❌ " + error);
                return;
            }

            view.clearDetailMessage();
            model.updateAdminProfile(targetAdminId, fullName, phoneNumber);
            view.showDetailMessage("✅ Profile updated successfully!");
            loadAllAdmins();

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Invalid admin ID");
        } catch (AuthenticationException e) {
            view.showDetailMessage("❌ " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - CHANGE EMAIL =====

    private void handleSubmitEmailButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);
            String newEmail = view.getNewEmailField().getText();
            String password = view.getEmailPasswordField().getText();

            // ✅ VALIDATE using model
            String error = model.validateEmailChange(newEmail, password);
            if (error != null) {
                view.showDetailMessage("❌ " + error);
                return;
            }

            view.clearDetailMessage();
            model.updateAdminEmail(targetAdminId, newEmail, password);
            view.getDetailEmailLabel().setText(newEmail.trim());
            view.getNewEmailField().clear();
            view.getEmailPasswordField().clear();
            view.showDetailMessage("✅ Email updated successfully!");
            loadAllAdmins();

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Invalid admin ID");
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Incorrect password")) {
                view.showDetailMessage("❌ Incorrect password");
            } else {
                view.showDetailMessage("❌ " + e.getMessage());
            }
        } catch (InvalidInputException e) {
            if (e.getMessage().contains("Email already exists")) {
                view.showDetailMessage("❌ This email is already in use");
            } else {
                view.showDetailMessage("❌ " + e.getMessage());
            }
        }
    }

    // ===== DETAIL PANEL - CHANGE PASSWORD =====

    private void handleSubmitPasswordButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);
            String oldPassword = view.getOldPasswordField().getText();
            String newPassword = view.getNewPasswordField().getText();
            String confirmPassword = view.getConfirmPasswordField().getText();

            // ✅ VALIDATE password match first
            if (!newPassword.equals(confirmPassword)) {
                view.showDetailMessage("❌ Passwords do not match");
                return;
            }

            // ✅ VALIDATE using model
            String error = model.validatePasswordChange(oldPassword, newPassword);
            if (error != null) {
                view.showDetailMessage("❌ " + error);
                return;
            }

            view.clearDetailMessage();
            model.changeAdminPassword(targetAdminId, oldPassword, newPassword);
            view.getOldPasswordField().clear();
            view.getNewPasswordField().clear();
            view.getConfirmPasswordField().clear();
            view.showDetailMessage("✅ Password changed successfully!");

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Invalid admin ID");
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Incorrect")) {
                view.showDetailMessage("❌ Incorrect current password");
            } else {
                view.showDetailMessage("❌ " + e.getMessage());
            }
        }
    }

    // ===== DETAIL PANEL - DELETE =====

    private void handleDeleteButtonClick() {
        try {
            String adminIdText = view.getDetailAdminIdLabel().getText();
            int targetAdminId = Integer.parseInt(adminIdText);
            String username = view.getDetailUsernameLabel().getText();

            boolean confirmed = view.showDeleteConfirmation(username);
            if (confirmed) {
                view.clearDetailMessage();
                model.deleteAdmin(targetAdminId);
                view.showListPanel();
                loadAllAdmins();
                view.showListMessage("✅ Admin '" + username + "' deleted successfully");
            }

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Invalid admin ID");
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Cannot delete")) {
                view.showDetailMessage("❌ You cannot delete your own account");
            } else {
                view.showDetailMessage("❌ " + e.getMessage());
            }
        }
    }

    // ===== CREATE PANEL =====

    private void handleCreateSaveButtonClick() {
        String username = view.getCreateUsernameField().getText();
        String email = view.getCreateEmailField().getText();
        String password = view.getCreatePasswordField().getText();
        String confirmPassword = view.getCreateConfirmPasswordField().getText();
        String fullName = view.getCreateFullNameField().getText();
        String phoneNumber = view.getCreatePhoneField().getText();

        // ✅ VALIDATE password match first
        if (!password.equals(confirmPassword)) {
            view.showCreateMessage("❌ Passwords do not match");
            return;
        }

        // ✅ VALIDATE all fields using model
        String error = model.validateAdminCreation(username, email, password, fullName, phoneNumber);
        if (error != null) {
            view.showCreateMessage("❌ " + error);
            return;
        }

        try {
            view.clearCreateMessage();
            int newAdminId = model.createAdmin(username, email, password, fullName, phoneNumber);
            view.showSuccessAlert("Admin Created", "✅ Admin account created successfully!\nAdmin ID: " + newAdminId);
            view.showListPanel();
            loadAllAdmins();

        } catch (AuthenticationException e) {
            view.showCreateMessage("❌ " + e.getMessage());
        } catch (InvalidInputException e) {
            if (e.getMessage().contains("already exists")) {
                view.showCreateMessage("❌ Username or email already exists");
            } else {
                view.showCreateMessage("❌ " + e.getMessage());
            }
        }
    }

    // ===== DATA LOADING =====

    public void loadAllAdmins() throws AuthenticationException {
        adminArrays = model.loadAllAdmins();
        view.displayAdminList(adminArrays);
    }
}
