package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.ScheduleDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentView;

import java.net.URL;
import java.util.*;

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
        System.out.println("✅ CustomerSeatSelectionView initialized");

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

    /**
     * Get all seat button keys (e.g., "A1", "B2", etc.)
     */
    public List<String> getAllSeatButtonKeys() {
        return new ArrayList<>(seatButtonMap.keySet());
    }

    /**
     * Apply a style to a specific seat button
     * @param seatKey The seat identifier (e.g., "A1")
     * @param styleType One of: "available", "vip", "taken", "maintenance", "selected"
     */
    public void applySeatStyle(String seatKey, String styleType) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn == null) return;

        switch (styleType.toLowerCase()) {
            case "selected":
                btn.setStyle(SELECTED_STYLE);
                break;
            case "vip":
                btn.setStyle(VIP_STYLE);
                break;
            case "taken":
                btn.setStyle(TAKEN_STYLE);
                break;
            case "maintenance":
                btn.setStyle(MAINTENANCE_STYLE);
                break;
            case "available":
            default:
                btn.setStyle(REGULAR_STYLE);
                break;
        }
    }

    /**
     * Enable or disable a seat button
     */
    public void enableSeatButton(String seatKey, boolean enabled) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn != null) {
            btn.setDisable(!enabled);
        }
    }

    /**
     * Set the click handler for a specific seat button
     */
    public void setSeatClickHandler(String seatKey, Runnable handler) {
        Button btn = seatButtonMap.get(seatKey);
        if (btn != null) {
            btn.setOnAction(e -> handler.run());
        }
    }

    /**
     * Update the selected seats display label
     */
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

    /**
     * Show a status message
     */
    public void showStatus(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setVisible(true);
        }
    }

    /**
     * Set the cancel button action
     */
    public void setCancelButtonAction(Runnable action) {
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> action.run());
        }
    }

    /**
     * Set the home button action
     */
    public void setHomeButtonAction(Runnable action) {
        if (homeButton != null) {
            homeButton.setOnAction(e -> action.run());
        }
    }

    /**
     * Set the book button action
     */
    public void setBookButtonAction(Runnable action) {
        if (bookButton != null) {
            bookButton.setOnAction(e -> action.run());
        }
    }

    /**
     * Close the current window
     */
    public void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Open the payment window with all necessary data
     */
    public void openPaymentWindow(
            int customerId,
            int bookingId,
            double totalAmount,
            ScheduleDetails scheduleDetails,
            String[] selectedSeatKeys,
            double ticketPrice,
            double serviceFee
    ) {
        try {
            // Load payment FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerPayment.fxml")
            );

            BorderPane paymentPane = loader.load();

            // Get view and create model/controller
            PaymentView paymentView = loader.getController();
            PaymentModel paymentModel = new PaymentModel();
            PaymentController paymentController = new PaymentController(paymentView, paymentModel);

            // Set payment data
            paymentController.setPaymentData(customerId, bookingId, totalAmount);

            // Populate payment view with booking details
            paymentView.setBookingDetails(
                    "BK-" + String.format("%05d", bookingId),
                    "Customer #" + customerId,
                    scheduleDetails.movieTitle,
                    scheduleDetails.screeningDate,
                    scheduleDetails.screeningTime,
                    "Cinema 1",
                    String.join(", ", selectedSeatKeys)
            );

            // Set price breakdown
            paymentView.setPriceDetails(
                    String.format("₱ %.2f", ticketPrice),
                    "x " + selectedSeatKeys.length,
                    String.format("₱ %.2f", totalAmount),
                    String.format("₱ %.2f", serviceFee),
                    String.format("₱ %.2f", totalAmount)
            );

            // Create and show payment stage
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Payment - Booking #" + bookingId);
            paymentStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(paymentPane, 450, 600);
            paymentStage.setScene(scene);

            // Get current stage
            Stage currentStage = (Stage) bookButton.getScene().getWindow();

            // Close current stage when payment window is shown
            paymentStage.setOnShown(e -> currentStage.close());

            paymentStage.show();

            System.out.println("✅ Payment window opened for booking #" + bookingId);

        } catch (Exception e) {
            System.err.println("❌ Error opening payment window:");
            e.printStackTrace();
            showStatus("Error opening payment: " + e.getMessage());
        }
    }

    // ===== GETTERS (for backwards compatibility) =====

    public GridPane getSeatGridPane() {
        return seatGridPane;
    }

    public Label getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public Label getCurrentSeatDetailsLabel() {
        return currentSeatDetailsLabel;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getHomeButton() {
        return homeButton;
    }

    public Button getBookButton() {
        return bookButton;
    }
}