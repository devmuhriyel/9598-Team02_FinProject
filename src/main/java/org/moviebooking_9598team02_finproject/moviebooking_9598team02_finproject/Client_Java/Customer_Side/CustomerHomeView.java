package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

/**
 * CustomerHomeView - View component for customer home screen
 * FIXED VERSION - Matches CustomerHome.fxml fx:id names
 */
public class CustomerHomeView {

    // ===== FXML VARIABLES =====
    @FXML
    private Label customerName;

    @FXML
    private Label logOutButton;

    // Navigation labels - FIXED to match FXML
    @FXML
    private Label navBrowse;  // Was: navDashboard/navMovie

    @FXML
    private Label navBookings;  // Was: navBooking

    @FXML
    private Label navProfile;  // Was: navCustomer

    @FXML
    private Label navSettings;  // NEW

    @FXML
    private Label currentDateTime;  // NEW - for date/time display

    @FXML
    private StackPane panesContainer;

    private Map<String, Label> labelMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Map navigation labels for easy access
        labelMap.put("navBrowse", navBrowse);
        labelMap.put("navBookings", navBookings);
        labelMap.put("navProfile", navProfile);
        labelMap.put("navSettings", navSettings);
    }

    // ===== METHODS =====

    /**
     * Set the customer name displayed in the sidebar
     */
    public void setCustomerName(String customerName) {
        this.customerName.setText(customerName);
    }

    /**
     * Update the date/time display
     */
    public void updateDateTime(String dateTime) {
        if (currentDateTime != null) {
            currentDateTime.setText(dateTime);
        }
    }

    /**
     * Highlight the active navigation item
     * @param labelName The name of the label to highlight (e.g., "navBrowse")
     */
    public void changePanes(String labelName) {
        // Reset all navigation labels to default style
        for (Label label : labelMap.values()) {
            label.setStyle("-fx-text-fill: #FFFFFF; -fx-padding: 8 10; -fx-cursor: hand;");
        }

        // Highlight the active label
        Label activeLabel = labelMap.get(labelName);
        if (activeLabel != null) {
            activeLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #FF6B6B; -fx-padding: 8 10; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        }
    }

    // ===== GETTERS =====

    public Label getCustomerName() {
        return customerName;
    }

    public Label getLogOutButton() {
        return logOutButton;
    }

    public Label getNavBrowse() {
        return navBrowse;
    }

    public Label getNavBookings() {
        return navBookings;
    }

    public Label getNavHistory() {
        return navHistory;
    }

    public Label getNavProfile() {
        return navProfile;
    }

    public Label getNavSettings() {
        return navSettings;
    }

    public Label getCurrentDateTime() {
        return currentDateTime;
    }

    public StackPane getPanesContainer() {
        return panesContainer;
    }
}