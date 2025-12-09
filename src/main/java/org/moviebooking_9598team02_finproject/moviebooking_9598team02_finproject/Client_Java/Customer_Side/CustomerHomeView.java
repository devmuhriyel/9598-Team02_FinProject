package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomerHomeView - JavaFX controller for customer home screen with CSS styling
 */
public class CustomerHomeView {

    // ===== USER INFO =====
    @FXML
    private Label customerName;
    @FXML
    private Label currentDateTime;

    // ===== NAVIGATION LABELS =====
    @FXML
    private Label navBrowse;
    @FXML
    private Label navBookings;
    @FXML
    private Label navHistory;
    @FXML
    private Label navProfile;
    @FXML
    private Label navSettings;
    @FXML
    private Label logOutButton;

    // ===== CONTENT CONTAINER =====
    @FXML
    private StackPane panesContainer;

    // ===== INTERNAL STATE =====
    private Map<String, Label> labelMap = new HashMap<>();

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        // Map navigation labels for easy style management
        labelMap.put("navBrowse", navBrowse);
        labelMap.put("navBookings", navBookings);
        labelMap.put("navHistory", navHistory);
        labelMap.put("navProfile", navProfile);
        labelMap.put("navSettings", navSettings);

        // Update current date/time
        updateDateTime();
    }

    // ===== PUBLIC METHODS =====

    /**
     * Set the customer name displayed in the sidebar
     */
    public void setCustomerName(String name) {
        if (customerName != null) {
            customerName.setText(name);
        }
    }

    /**
     * Update the current date/time display
     */
    public void updateDateTime() {
        if (currentDateTime != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy  |  h:mm a");
            currentDateTime.setText(now.format(formatter));
        }
    }

    /**
     * Change active navigation pane using CSS classes
     * @param labelName The name of the navigation label to highlight
     */
    public void changePanes(String labelName) {
        // Reset all navigation labels to default CSS class
        for (Label label : labelMap.values()) {
            label.getStyleClass().removeAll("nav-label-active");
            if (!label.getStyleClass().contains("nav-label")) {
                label.getStyleClass().add("nav-label");
            }
        }

        // Add active CSS class to selected label
        Label activeLabel = labelMap.get(labelName);
        if (activeLabel != null) {
            activeLabel.getStyleClass().remove("nav-label");
            activeLabel.getStyleClass().add("nav-label-active");
        }
    }

    // ===== GETTERS - User Info =====
    public Label getCustomerName() {
        return customerName;
    }

    public Label getCurrentDateTime() {
        return currentDateTime;
    }

    // ===== GETTERS - Navigation =====
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

    public Label getLogOutButton() {
        return logOutButton;
    }

    // ===== GETTERS - Content Container =====
    public StackPane getPanesContainer() {
        return panesContainer;
    }
}