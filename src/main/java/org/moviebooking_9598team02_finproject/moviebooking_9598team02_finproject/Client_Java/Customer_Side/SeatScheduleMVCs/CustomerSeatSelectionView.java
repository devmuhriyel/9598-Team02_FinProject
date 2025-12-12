package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

// REMOVED: Payment imports (Logic moved to MovieNavigationManager)

public class CustomerSeatSelectionView implements Initializable {

    // ===== FXML INJECTED FIELDS =====
    @FXML private GridPane seatGridPane;
    @FXML private Label statusMessageLabel;
    @FXML private Label currentSeatDetailsLabel;
    @FXML private Button cancelButton;
    @FXML private Button homeButton;
    @FXML private Button bookButton;

    // All seat buttons
    @FXML private Button btnA1, btnA2, btnA3, btnA4, btnA5, btnA6, btnA7, btnA8, btnA9, btnA10, btnA11;
    @FXML private Button btnB1, btnB2, btnB3, btnB4, btnB5, btnB6, btnB7, btnB8, btnB9, btnB10, btnB11;
    @FXML private Button btnC1, btnC2, btnC3, btnC4, btnC5, btnC6, btnC7, btnC8, btnC9, btnC10, btnC11;

    // ===== STYLE CONSTANTS =====
    private static final String SELECTED_STYLE = "-fx-background-color: #007ACC; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60; -fx-border-color: #0066AA; -fx-border-width: 2;";
    private static final String REGULAR_STYLE = "-fx-background-color: WHITE; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;";
    private static final String VIP_STYLE = "-fx-background-color: #FFD700; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;";
    private static final String TAKEN_STYLE = "-fx-background-color: #00CC66; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;";
    private static final String MAINTENANCE_STYLE = "-fx-background-color: #808080; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;";

    // ===== INTERNAL STATE =====
    private Map<String, Button> seatButtonMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Build seat button map
        Button[] allButtons = {
                btnA1, btnA2, btnA3, btnA4, btnA5, btnA6, btnA7, btnA8, btnA9, btnA10, btnA11,
                btnB1, btnB2, btnB3, btnB4, btnB5, btnB6, btnB7, btnB8, btnB9, btnB10, btnB11,
                btnC1, btnC2, btnC3, btnC4, btnC5, btnC6, btnC7, btnC8, btnC9, btnC10, btnC11
        };

        for (Button button : allButtons) {
            if (button != null) {
                seatButtonMap.put(button.getText(), button);
            }
        }
    }

    // ===== PUBLIC API FOR CONTROLLER =====

    public List<String> getAllSeatButtonKeys() {
        return new ArrayList<>(seatButtonMap.keySet());
    }

    public void applySeatStyle(String seatKey, String styleType) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn == null) return;

        switch (styleType.toLowerCase()) {
            case "selected": btn.setStyle(SELECTED_STYLE); break;
            case "vip": btn.setStyle(VIP_STYLE); break;
            case "taken": btn.setStyle(TAKEN_STYLE); break;
            case "maintenance": btn.setStyle(MAINTENANCE_STYLE); break;
            case "available": default: btn.setStyle(REGULAR_STYLE); break;
        }
    }

    public void enableSeatButton(String seatKey, boolean enabled) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn != null) btn.setDisable(!enabled);
    }

    public void setSeatClickHandler(String seatKey, Runnable handler) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn != null) btn.setOnAction(e -> handler.run());
    }

    public void updateSelectedSeatsDisplay(List<String> selectedSeats) {
        if (currentSeatDetailsLabel == null) return;

        if (selectedSeats == null || selectedSeats.isEmpty()) {
            currentSeatDetailsLabel.setText("No seats selected");
            currentSeatDetailsLabel.setStyle("-fx-text-fill: #AAAAAA;");
        } else {
            currentSeatDetailsLabel.setText("Selected: " + String.join(", ", selectedSeats));
            currentSeatDetailsLabel.setStyle("-fx-text-fill: #00CC66; -fx-font-weight: bold;");
        }
    }

    public void showStatus(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setVisible(true);
        }
    }

    public void setCancelButtonAction(Runnable action) {
        if (cancelButton != null) cancelButton.setOnAction(e -> action.run());
    }

    public void setHomeButtonAction(Runnable action) {
        if (homeButton != null) homeButton.setOnAction(e -> action.run());
    }

    public void setBookButtonAction(Runnable action) {
        if (bookButton != null) bookButton.setOnAction(e -> action.run());
    }

    public void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // ===== REMOVED openPaymentWindow() method entirely =====
    // Logic is now handled by MovieNavigationManager called by the Controller.
}