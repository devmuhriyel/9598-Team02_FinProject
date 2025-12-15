package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ProfileView {
    @FXML
    private VBox rootPane;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField oldPasswordField;
    @FXML
    private TextField newPasswordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button changePasswordButton;

    // ==== SETTERS ====
    public void setUsername(String username) {this.usernameField.setText(username);    }
    public void setFullName(String fullName) {this.fullNameField.setText(fullName);    }
    public void setEmail(String email) {this.emailField.setText(email);    }
    public void setPhoneNumber(String phoneNumber) {this.phoneNumberField.setText(phoneNumber);    }

    // ==== GETTERS ====
    public String getUsername() {return this.usernameField.getText();    }
    public String getFullName() {return this.fullNameField.getText();    }
    public String getEmail() {return this.emailField.getText();    }
    public String getPhoneNumber() {return this.phoneNumberField.getText();    }
    public String getOldPassword() {return this.oldPasswordField.getText(); }
    public String getNewPassword() {return this.newPasswordField.getText(); }
    public Button getSaveButton() {
        return this.saveButton;
    }
    public Button getChangePasswordButton() {
        return this.changePasswordButton;
    }
}