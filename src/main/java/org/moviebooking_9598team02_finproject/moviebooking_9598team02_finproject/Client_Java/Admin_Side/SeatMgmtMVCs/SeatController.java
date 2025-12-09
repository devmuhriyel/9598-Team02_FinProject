package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.scene.control.Button;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatController {

    private final SeatModel model;
    private final SeatView view;
    private final AdminSMNavManager navManager;

    private final Map<String, Seat> seatDataMap = new HashMap<>();

    private final List<String> selectedSeatNames = new ArrayList<>();
    private final List<Integer> selectedSeatIds = new ArrayList<>();

    public SeatController(SeatModel model, SeatView view, AdminSMNavManager navigationManager) {
        this.model = model;
        this.view = view;
        this.navManager = navigationManager;
        loadAllSeatData();
    }

    public void initializeSeatButtons() {
        for (Button button : view.getAllSeatButtons()) {
            final Button seatButton = button;
            final String seatName = button.getText();
            Seat seat = seatDataMap.get(seatName);
            if (seat != null) {
                view.updateSeatStyle(seatName, seat.seatType);
            }
            button.setOnAction(event -> handleSeatClick(seatButton, seatName));
        }
        view.setStandardType(() -> handleSetSeatType("regular"));
        view.setVipType(() -> handleSetSeatType("vip"));
        view.setMaintenance(() -> handleSetSeatType("maintenance"));
        view.setClearSelection(() -> handleClearSelection());
        view.setPriceUpdateClick(event -> handlePriceUpdate());
    }

    private void loadAllSeatData() {
        try {
            Seat[] allSeats = model.getAllSeats();
            seatDataMap.clear();
            for (Seat seat : allSeats) {
                String seatName = getSeatName(seat);
                seatDataMap.put(seatName, seat);
            }
            view.getStatusMessageLabel().setText("Seat data loaded successfully.");
        } catch (AuthenticationException e) {
            view.getStatusMessageLabel().setText("Error loading seats: Authentication failed.");
        } catch (NullPointerException e) {
            view.getStatusMessageLabel().setText("Service Error: Cannot connect to Admin Service to load seat data. Check CORBA/Service initialization.");
        } catch (Exception e) {
            view.getStatusMessageLabel().setText("Error loading seats from server: " + e.getMessage());
        }
    }

    private String getSeatName(Seat seat) {
        return seat.seatRow + seat.seatNumber;
    }

    private void handleSeatClick(Button clickedButton, String seatName) {
        if (selectedSeatNames.contains(seatName)) {
            selectedSeatNames.remove(seatName);
            Seat seat = seatDataMap.get(seatName);
            if (seat != null) {
                selectedSeatIds.remove(Integer.valueOf(seat.seatId));
            }
            view.deselectSeat(clickedButton);
        } else {
            selectedSeatNames.add(seatName);
            Seat seat = seatDataMap.get(seatName);
            if (seat != null) {
                selectedSeatIds.add(seat.seatId);
            }
            view.setSeatSelected(clickedButton);
        }

        updateSelectionDisplay();
    }

    private void updateSelectionDisplay() {
        if (selectedSeatNames.isEmpty()) {
            view.getStatusMessageLabel().setText("Click seats to select them.");
            view.getCurrentSeatDetailsLabel().setText("");
            view.getPriceInput().setText("");
        } else {
            view.getStatusMessageLabel().setText("Selected: " + String.join(", ", selectedSeatNames));
            String lastSelected = selectedSeatNames.get(selectedSeatNames.size() - 1);
            loadSeatDetails(lastSelected);
        }
    }

    private void loadSeatDetails(String seatName) {
        Seat seat = seatDataMap.get(seatName);
        if (seat != null) {
            view.getCurrentSeatDetailsLabel().setText(
                    String.format("Seat: %s | Type: %s | Price: P%.2f",
                            seatName,
                            seat.seatType.toUpperCase(),
                            seat.seatPrice
                    )
            );
            view.getPriceInput().setText(String.valueOf(seat.seatPrice));
        } else {
            view.getCurrentSeatDetailsLabel().setText("Seat: " + seatName + " | Data load error. Select another seat.");
            view.getPriceInput().setText("");
        }
    }

    public void handleClearSelection(){
        List<String> seatsToDeselect = new ArrayList<>(selectedSeatNames);

        selectedSeatNames.clear();
        selectedSeatIds.clear();

        for (String seatName : seatsToDeselect) {
            Button button = view.getSeatButton(seatName);
            if (button != null) view.deselectSeat(button);
        }

        updateSelectionDisplay();
    }

    public void handlePriceUpdate() {
        if (selectedSeatNames.isEmpty() || selectedSeatIds.isEmpty()) {
            view.getStatusMessageLabel().setText("Error: Please select at least one seat before updating the price.");
            return;
        }
        String priceText = view.getPriceInput().getText();
        double newPrice;
        try {
            newPrice = Double.parseDouble(priceText);
            if (newPrice < 0) {
                view.getStatusMessageLabel().setText("Error: Price must be a non-negative number.");
                return;
            }
        } catch (NumberFormatException e) {
            view.getStatusMessageLabel().setText("Error: Invalid price format. Please enter a number.");
            return;
        }
        try {
            for (int seatId : selectedSeatIds) {
                model.updateSeatPrice(seatId, newPrice);
            }
            for (String seatName : selectedSeatNames) {
                Seat seat = seatDataMap.get(seatName);
                if (seat != null) {
                    seat.seatPrice = newPrice;
                }
            }
            view.getStatusMessageLabel().setText(String.format("Success: Price for %d seat(s) updated to P%.2f.", selectedSeatIds.size(), newPrice));
            if (!selectedSeatNames.isEmpty()) {
                String lastSelected = selectedSeatNames.get(selectedSeatNames.size() - 1);
                loadSeatDetails(lastSelected);
            }
        } catch (AuthenticationException e) {
            view.getStatusMessageLabel().setText("Failed to update price: Authentication failed.");
        } catch (Exception e) {
            view.getStatusMessageLabel().setText("Failed to update price on server: " + e.getMessage());
        }
    }

    public void handleSetSeatType(String type) {
        if (selectedSeatNames.isEmpty() || selectedSeatIds.isEmpty()) {
            view.getStatusMessageLabel().setText("Error: Please select at least one seat before changing the type.");
            return;
        }
        try {
            for (int seatId : selectedSeatIds) {
                model.updateSeatType(seatId, type);
            }
            for (String seatName : selectedSeatNames) {
                Seat seat = seatDataMap.get(seatName);
                if (seat != null) {
                    seat.seatType = type;
                }
            }
            view.getStatusMessageLabel().setText(String.format("Success: %d seat(s) set to %s type.", selectedSeatIds.size(), type.toUpperCase()));
            for (String seatName : selectedSeatNames) {
                view.updateSeatStyle(seatName, type);
            }
            if (!selectedSeatNames.isEmpty()) {
                String lastSelected = selectedSeatNames.get(selectedSeatNames.size() - 1);
                loadSeatDetails(lastSelected);
            }
        } catch (AuthenticationException e) {
            view.getStatusMessageLabel().setText("Failed to update seat type: Authentication failed.");
        } catch (Exception e) {
            view.getStatusMessageLabel().setText("Failed to update seat type on server: " + e.getMessage());
        }
    }
}