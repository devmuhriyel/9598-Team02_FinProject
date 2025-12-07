package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;


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
    private StackPane panesContainer;

    List<Label> labels = new ArrayList<>();


    public AdminHomeView(){
        labels.add(navDashboard);
        labels.add(navMovie);
        labels.add(navShowtime);
        labels.add(navBooking);
        labels.add(navCustomer);
    }


    // ===== METHODS =====
    public void setAdminName(String adminName){
        this.adminName.setText(adminName);
    }

    public void changePanes(String labelName){
        for (Label label : labels ){
            if (label.getText().equals(labelName)){
                label.setStyle("-fx-background-color: #ff2525");
            }
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
    public StackPane getPanesContainer() {
        return panesContainer;
    }
}
