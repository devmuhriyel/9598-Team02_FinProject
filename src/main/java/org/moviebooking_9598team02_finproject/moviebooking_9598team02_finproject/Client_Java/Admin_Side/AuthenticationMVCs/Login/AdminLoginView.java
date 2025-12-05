package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AdminLoginView {

    // ===== VARIABLES =====
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink createAccountLink;
    @FXML
    private Text errorText;

    // ===== METHODS =====
    public void clearFields(){
        userNameField.clear();
        passwordField.clear();
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
    public String getUserName() {
        return userNameField.getText();
    }
    public String getPassword() {
        return passwordField.getText();
    }
    public Button getLoginButton() {
        return loginButton;
    }
    public Hyperlink getCreateAccountLink() {
        return createAccountLink;
    }



}
