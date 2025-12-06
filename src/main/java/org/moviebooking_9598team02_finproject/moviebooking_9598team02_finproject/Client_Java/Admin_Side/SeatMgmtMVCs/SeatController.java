package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

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
    private int selectedSeatId = -1; // Use int for ID, matching the IDL mapping

    // --- Constructor (Dependency Injection) ---
    public SeatController(SeatModel model, SeatView view) {
        this.model = model;
        this.view = view;
    }

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
            Seat[] allSeats = (Seat[]) model.getAllSeats();
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

    // === EVENT HANDLING === TO BE CHECKED ULIT
    private void handleSeatButtonClick(Button clickedButton) {
        view.clearStatusMessage();

        if (currentlySelectedButton != null && currentlySelectedButton != clickedButton) {
            String prevStyle = seatStyleMap.getOrDefault(currentlySelectedButton.getId(), getStyleForSeatType("Standard"));
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

        view.updateSeatVisual(clickedButton, "-fx-background-color: #6699FF; -fx-text-fill: white;");
        view.showStatusMessage("Selected Seat: " + selectedSeatName + " | Type: " + selectedSeat.seatType + " | Price: P" + selectedSeat.seatPrice, false);
    }

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

    public void updateSelectedSeatType(String newType) {
        if (selectedSeatId == -1 || currentlySelectedButton == null) {
            view.showStatusMessage("Please select a seat to update its type.", true);
            return;
        }

        try {
            model.updateSeatType(selectedSeatId, newType); // Call the Model (CORBA)
            Seat seat = seatDataMap.get(currentlySelectedButton.getId());

            if (seat != null) {
                seat.seatType = newType;
                String newStyle = getStyleForSeatType(newType);
                view.updateSeatVisual(currentlySelectedButton, newStyle);
                seatStyleMap.put(currentlySelectedButton.getId(), newStyle);
            }

            view.showStatusMessage("Type for " + selectedSeatName + " updated to " + newType, false);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Update failed: Admin authentication required.", true);
        } catch (Exception e) {
            view.showStatusMessage("Error updating type: " + e.getMessage(), true);
        }
    }
    private String getStyleForSeatType(String seatType) {
        String style;

        switch (seatType.toUpperCase()) {
            case "VIP":
                style = "-fx-background-color: #FFD700; -fx-text-fill: black;";
                break;
            case "PREMIUM":
                style = "-fx-background-color: #4CAF50; -fx-text-fill: white;";
                break;
            default:
                style = "-fx-background-color: #555555; -fx-text-fill: white;";
                break;
        }
        return style;
    }
}