package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

public class AdminHomeView {

    // ===== VARIABLES =====
    @FXML
    private Label adminName;
    @FXML
    private Label logOutButton;
    @FXML
    private Label navDashboard;
    @FXML
    private Label navMovie;
    @FXML
    private Label navShowtime;
    @FXML
    private Label navBooking;
    @FXML
    private Label navCustomer;
    @FXML
    private Label navAdmin;
    @FXML
    private Label navGenre;
    @FXML
    private StackPane panesContainer;

    private Map<String, Label> labelMap = new HashMap<>();

    @FXML
    public void initialize() {
        labelMap.put("navDashboard", navDashboard);
        labelMap.put("navMovie", navMovie);
        labelMap.put("navShowtime", navShowtime);
        labelMap.put("navBooking", navBooking);
        labelMap.put("navCustomer", navCustomer);
        labelMap.put("navAdmin", navAdmin);
        labelMap.put("navGenre", navGenre);
    }

    // ===== METHODS =====
    public void setAdminName(String adminName){
        this.adminName.setText(adminName);
    }

    public void changePanes(String labelName){
        for (Label label : labelMap.values()) {
            label.setStyle("-fx-text-fill: #FFFFFF; -fx-padding: 8 10; -fx-cursor: hand;");
        }

        Label activeLabel = labelMap.get(labelName);
        if (activeLabel != null) {
            activeLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #ff2525; -fx-padding: 8 10; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        }
    }

    // ===== GETTERS =====
    public Label getAdminName() {
        return adminName;
    }
    public Label getLogOutButton() {
        return logOutButton;
    }
    public Label getNavDashboard() {
        return navDashboard;
    }
    public Label getNavMovie() {
        return navMovie;
    }
    public Label getNavShowtime() {
        return navShowtime;
    }
    public Label getNavBooking() {
        return navBooking;
    }
    public Label getNavCustomer() {
        return navCustomer;
    }
    public Label getNavAdmin() {
        return navAdmin;
    }
    public Label getNavGenre(){
        return navGenre;
    }
    public StackPane getPanesContainer() {
        return panesContainer;
    }
}