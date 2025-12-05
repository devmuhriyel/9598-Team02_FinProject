package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Signup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;


public class CustomerSignupView {

    // ===== VARIABLES =====
    @FXML
    private TextField userNameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Text errorText;
    @FXML
    private Button signupButton;
    @FXML
    private Hyperlink logInLink;

    // ===== METHODS =====
    public void clearFields(){
        userNameField.clear();
        fullNameField.clear();
        phoneNumberField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
    public void showError(String message){
        if(errorText != null){
            errorText.setText(message);
            errorText.setVisible(true);
        }
    }
    public void clearError(){
        if(errorText != null){
            errorText.setText("");
            errorText.setVisible(false);
        }
    }

    // ===== GETTERS =====
    public String getUserName(){
        return userNameField.getText();
    }
    public String getFullName(){
        return fullNameField.getText();
    }
    public String getPhoneNumber(){
        return phoneNumberField.getText();
    }
    public String getEmail(){
        return emailField.getText();
    }
    public String getPassword(){
        return passwordField.getText();
    }
    public String getConfirmPassword(){
        return confirmPasswordField.getText();
    }
    public Button getSignUpButton(){
        return signupButton;
    }
    public Hyperlink getLogInLink(){
        return logInLink;
    }
}
