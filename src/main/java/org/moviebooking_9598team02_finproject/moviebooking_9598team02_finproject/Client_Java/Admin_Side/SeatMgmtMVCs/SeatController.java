package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SeatController implements Initializable {

    private final SeatModel model;
    private final SeatView view;

    private final Map<String, Seat> seatDataMap = new HashMap<>();
    private final Map<String, String> seatStyleMap = new HashMap<>();

    private Button currentlySelectedButton = null;
    private String selectedSeatName = null;
    private int selectedSeatId = -1;

    // --- Constructor (Dependency Injection) ---
    public SeatController(SeatModel model, SeatView view) {
        this.model = model;
        this.view = view;
    }

    // --- Action Handlers from FXML ---

    public void handlePriceUpdateClick(ActionEvent event) {
        String priceText = view.getPriceInput().getText();

        try {
            double newPrice = Double.parseDouble(priceText);
            updateSelectedSeatPrice(newPrice);
            view.getPriceInput().clear();
        } catch (NumberFormatException e) {
            view.showStatusMessage("Invalid Price: Please enter a valid number.", true);
        }
    }

    public void handleSetStandardType(ActionEvent event) {
        updateSelectedSeatType("STANDARD");
    }
    public void handleSetVipType(ActionEvent event) {
        updateSelectedSeatType("VIP");
    }

    // --- Initialization and Loading ---

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (Button button : view.getAllSeatButtons()) {
            // TO BE CHECKED
            button.setOnAction(event -> handleSeatButtonClick((Button) event.getSource()));
        }
        loadAllSeatData();
    }

    private void loadAllSeatData() {
        try {
            Seat[] allSeats = model.getAllSeats();
            if (allSeats == null || allSeats.length == 0) {
                view.showStatusMessage("No seat data found on the server.", true);
                return;
            }

            // TO BE CHECKED
            Map<String, Button> buttonMap = new HashMap<>();
            for (Button btn : view.getAllSeatButtons()) {
                buttonMap.put(btn.getId(), btn);
            }

            for (Seat seat : allSeats) {
                String buttonId = "btn" + seat.seatRow + seat.seatNumber;

                if (buttonMap.containsKey(buttonId)) {
                    Button button = buttonMap.get(buttonId);
                    seatDataMap.put(button.getId(), seat);
                    String style = getStyleForSeatType(seat.seatType);
                    view.updateSeatVisual(button, style);
                    seatStyleMap.put(button.getId(), style);
                }
            }
            view.showStatusMessage("Seat data loaded successfully.", false);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
        } catch (Exception e) {
            view.showStatusMessage("An unexpected error occurred: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // === EVENT HANDLING ===
    private void handleSeatButtonClick(Button clickedButton) {
        view.clearStatusMessage();

        // Remove highlight from previously selected button
        if (currentlySelectedButton != null && currentlySelectedButton != clickedButton) {
            String prevStyle = seatStyleMap.getOrDefault(currentlySelectedButton.getId(), getStyleForSeatType("STANDARD"));
            view.updateSeatVisual(currentlySelectedButton, prevStyle);
        }

        currentlySelectedButton = clickedButton;
        selectedSeatName = clickedButton.getText();

        Seat selectedSeat = seatDataMap.get(clickedButton.getId());
        if (selectedSeat == null) {
            view.showStatusMessage("Error: Seat data not found for this button.", true);
            selectedSeatId = -1;
            return;
        }

        selectedSeatId = selectedSeat.seatId;

        // Apply temporary highlight style for selection
        view.updateSeatVisual(clickedButton, "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;");
        view.showStatusMessage("Selected Seat: " + selectedSeatName + " | Type: " + selectedSeat.seatType + " | Price: P" + selectedSeat.seatPrice, false);
    }

    // === Update Logic ===

    /**
     * Updates the price of the currently selected seat.
     */
    public void updateSelectedSeatPrice(double newPrice) {
        if (selectedSeatId == -1 || currentlySelectedButton == null) {
            view.showStatusMessage("Please select a seat to update its price.", true);
            return;
        }

        try {
            model.updateSeatPrice(selectedSeatId, newPrice); // Call the Model (CORBA)
            Seat seat = seatDataMap.get(currentlySelectedButton.getId());
            if (seat != null) {
                seat.seatPrice = newPrice;
            }
            view.showStatusMessage("Price for " + selectedSeatName + " updated to P" + newPrice, false);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Update failed: Admin authentication required.", true);
        } catch (Exception e) {
            view.showStatusMessage("Error updating price: " + e.getMessage(), true);
        }
    }

    /**
     * Updates the type (STANDARD/VIP) of the currently selected seat.
     */
    public void updateSelectedSeatType(String newType) {
        if (selectedSeatId == -1 || currentlySelectedButton == null) {
            view.showStatusMessage("Please select a seat to update its type.", true);
            return;
        }

        try {
            String finalType = newType.toUpperCase();
            model.updateSeatType(selectedSeatId, finalType); // Call the Model (CORBA)
            Seat seat = seatDataMap.get(currentlySelectedButton.getId());

            if (seat != null) {
                seat.seatType = finalType;
                String newStyle = getStyleForSeatType(finalType);
                view.updateSeatVisual(currentlySelectedButton, newStyle);
                seatStyleMap.put(currentlySelectedButton.getId(), newStyle);
            }

            view.showStatusMessage("Type for " + selectedSeatName + " updated to " + finalType, false);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Update failed: Admin authentication required.", true);
        } catch (Exception e) {
            view.showStatusMessage("Error updating type: " + e.getMessage(), true);
        }
    }


    private String getStyleForSeatType(String seatType) {
        String baseStyle = "-fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;";
        String colorStyle;

        switch (seatType.toUpperCase()) {
            case "VIP":
                // Gold color for VIP
                colorStyle = "-fx-background-color: #FFD700;";
                break;
            case "STANDARD":
                // Green color for Standard (also used for Reserved in the FXML key)
                colorStyle = "-fx-background-color: #00CC66;";
                break;
            default:
                // Fallback (White for Available/Default)
                colorStyle = "-fx-background-color: #FFFFFF;";
                break;
        }
        return colorStyle + " " + baseStyle;
    }
}