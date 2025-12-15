package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

public class ProfileController {
    private final ProfileModel model;
    private final ProfileView view;

    public ProfileController(ProfileModel model, ProfileView view) {
        this.model = model;
        this.view = view;

        attachEventHandlers();
        loadInitialData();
    }

    private void attachEventHandlers() {
        view.getSaveButton().setOnAction(e -> handleSaveAction());
        view.getChangePasswordButton().setOnAction(e -> handleChangePasswordAction());
    }

    private void loadInitialData() {
        view.setUsername(model.getUsername());
        view.setFullName(model.getFullName());
        view.setEmail(model.getEmail());
        view.setPhoneNumber(model.getPhoneNumber());
    }

    private void handleSaveAction() {
        String newFullName = view.getFullName();
        String newPhoneNumber = view.getPhoneNumber();
        if (newFullName.trim().isEmpty() || newPhoneNumber.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Full Name and Phone Number cannot be empty.");
            return;
        }
        try {
            model.updateCustomerProfile(newPhoneNumber, newFullName);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile details have been successfully updated!");
        } catch (AuthenticationException e) {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Authentication Error: Please log in again.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "Failed to communicate with the server.");
            e.printStackTrace();
        }
    }

    private void handleChangePasswordAction() {
        String oldPassword = view.getOldPassword();
        if (oldPassword == null) return;

        String newPassword = view.getNewPassword();
        if (newPassword == null) return;

        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Invalid Password", "New password must be at least 6 characters long.");
            return;
        }
        try {
            model.changePassword(oldPassword, newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been changed successfully!");
        } catch (AuthenticationException e) {
            showAlert(Alert.AlertType.ERROR, "Change Failed", "Error: Please check your old password.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "Failed to change password on the server.");
            e.printStackTrace();
        }
    }

    // --- Utility Methods for UI Feedback ---
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}