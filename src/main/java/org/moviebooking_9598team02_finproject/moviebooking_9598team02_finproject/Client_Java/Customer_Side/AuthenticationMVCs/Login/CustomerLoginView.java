package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class CustomerLoginView {

    // ===== VARIABLES =====
    @FXML
    private TextField usernameField;
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
        usernameField.clear();
        passwordField.clear();
    }

    //GETTERS
    public String getUsername() {
        return usernameField.getText();
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
}
