package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AdminParentView {

    // ===== VARIABLES =====
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private Button connectButton;
    @FXML
    private Text errorText;

    // ===== METHODS =====
    public void clearFields(){
        ipField.clear();
        portField.clear();
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
    public String getIPField() {
        return ipField.getText();
    }
    public String getPortField() {
        return portField.getText();
    }
    public Button getConnectButton() {
        return connectButton;
    }
}
