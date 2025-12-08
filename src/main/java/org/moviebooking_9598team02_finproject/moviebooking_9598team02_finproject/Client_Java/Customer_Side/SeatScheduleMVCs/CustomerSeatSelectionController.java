package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.util.*;

public class CustomerSeatSelectionController {

    private CustomerSeatSelectionView view;
    private CustomerSeatSelectionModel model;
    private SSNavigationManager ssNavigationManager;
    private ScheduleListSeat[] scheduleListSeatsData = new ScheduleListSeat[0];

    private String[] selectedSeatKeys = new String[0];

    public CustomerSeatSelectionController(CustomerSeatSelectionView view, CustomerSeatSelectionModel model, SSNavigationManager ssNavigationManager) {
        this.view = view;
        this.model = model;
        this.ssNavigationManager = ssNavigationManager;
    }

    /**
     * Manual initializer invoked by SSNavigationManager after construction.
     * Wires buttons and loads data for the provided schedule.
     */
    public void init(int scheduleId) {
        Buttons();
        initializeData(scheduleId);
    }

    private void initializeData(int scheduleID) {
        try {
            scheduleListSeatsData = model.getAvailableSeats(scheduleID);
            loadSeats();
        } catch (Exception e) {
            showStatus("Error loading schedule: " + e.getMessage());
        }
    }

    private void Buttons() {
        if (view == null) return;
        if (view.getCancelButton() != null) {
            view.getCancelButton().setOnAction(this::handleCancel);
        }
        if (view.getHomeButton() != null) {
            view.getHomeButton().setOnAction(this::handleHome);
        }
        if (view.getBookButton() != null) {
            view.getBookButton().setOnAction(this::handleBook);
        }
    }

    private void loadSeats() {
        if (model == null || scheduleListSeatsData == null) {
            return;
        }

        try {
            // Get all seats
            Seat[] allSeats = model.getAvailableSeats();
            
            // Update seat buttons in the grid
            if (view != null && view.getSeatGridPane() != null) {
                updateSeatButtons(allSeats, scheduleListSeatsData);
            }

            showStatus("Seats loaded successfully");
        } catch (Exception e) {
            showStatus("Error loading seats: " + e.getMessage());
        }
    }

    private void updateSeatButtons(Seat[] allSeats, ScheduleListSeat[] scheduleListSeats) {
        GridPane gridPane = view.getSeatGridPane();
        if (gridPane == null) return;

        // Iterate through all buttons in the grid and update their state
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String buttonText = btn.getText();
                
                // Parse seat identifier (e.g., "A1", "B3")
                if (buttonText != null && buttonText.length() >= 2) {
                    String row = buttonText.substring(0, 1);
                    int seatNum = Integer.parseInt(buttonText.substring(1));
                    
                    // Find matching seat
                    Seat seat = findSeat(allSeats, row, seatNum);
                    if (seat != null) {
                        // Find matching ScheduleListSeat
                        ScheduleListSeat sls = findScheduleListSeat(scheduleListSeats, seat.seatId);
                        
                        String key = row + seatNum;
                        
                        if (sls != null) {
                            // Update button style based on availability
                            if ("available".equalsIgnoreCase(sls.status)) {
                                view.styleSeatAvailable(btn);
                                btn.setDisable(false);
                                btn.setOnAction(e -> handleSeatClick(key, seat, sls));
                            } else {
                                view.styleSeatTaken(btn);
                                btn.setDisable(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private Seat findSeat(Seat[] seats, String row, int seatNum) {
        for (Seat seat : seats) {
            if (seat != null && seat.seatRow.equals(row) && seat.seatNumber == seatNum) {
                return seat;
            }
        }
        return null;
    }

    private ScheduleListSeat findScheduleListSeat(ScheduleListSeat[] scheduleListSeats, int seatId) {
        for (ScheduleListSeat sls : scheduleListSeats) {
            if (sls != null && sls.seatId == seatId) {
                return sls;
            }
        }
        return null;
    }

    private ScheduleListSeat findScheduleListSeatByKey(ScheduleListSeat[] scheduleListSeats, String seatKey) {
        if (seatKey == null || seatKey.length() < 2) return null;
        String row = seatKey.substring(0, 1);
        int seatNum = Integer.parseInt(seatKey.substring(1));
        
        // First find the seat
        try {
            Seat[] allSeats = model.getAvailableSeats();
            Seat seat = findSeat(allSeats, row, seatNum);
            if (seat != null) {
                return findScheduleListSeat(scheduleListSeats, seat.seatId);
            }
        } catch (Exception e) {
            // Handle exception
        }
        return null;
    }

    private void handleSeatClick(String key, Seat seat, ScheduleListSeat sls) {
        // Check if seat is already selected
        boolean isSelected = containsString(selectedSeatKeys, key);
        
        if (isSelected) {
            // Deselect seat
            selectedSeatKeys = removeString(selectedSeatKeys, key);
            Button button = ButtonByKey(key);
            if (button != null) {
                view.styleSeatAvailable(button);
            }
        } else {
            // Select seat (highlight in yellow)
            selectedSeatKeys = addString(selectedSeatKeys, key);
            Button button = ButtonByKey(key);
            if (button != null) {
                view.styleSeatSelected(button);
            }
        }
        updateSelectionDisplay();
    }

    private Button ButtonByKey(String key) {
        if (view == null || view.getSeatGridPane() == null) return null;
        
        GridPane gridPane = view.getSeatGridPane();
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (key.equals(btn.getText())) {
                    return btn;
                }
            }
        }
        return null;
    }

    private boolean containsString(String[] array, String value) {
        if (array == null) return false;
        for (String s : array) {
            if (value.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private String[] addString(String[] array, String value) {
        if (containsString(array, value)) {
            return array;
        }
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = value;
        return newArray;
    }

    private String[] removeString(String[] array, String value) {
        if (!containsString(array, value)) {
            return array;
        }
        String[] newArray = new String[array.length - 1];
        int index = 0;
        for (String s : array) {
            if (!value.equals(s)) {
                newArray[index++] = s;
            }
        }
        return newArray;
    }

    private void updateSelectionDisplay() {
        List<String> selectedList = new ArrayList<>();
        if (selectedSeatKeys != null) {
            for (String key : selectedSeatKeys) {
                selectedList.add(key);
            }
        }
        Collections.sort(selectedList);
        
        if (view != null) {
            view.updateSelectedSeats(selectedList);
            if (view.getCurrentSeatDetailsLabel() != null) {
                if (selectedList.isEmpty()) {
                    view.getCurrentSeatDetailsLabel().setText("No seats selected");
                } else {
                    view.getCurrentSeatDetailsLabel().setText("Selected: " + String.join(", ", selectedList));
                }
            }
        }
    }

    public void handleCancel(ActionEvent event) {
        selectedSeatKeys = new String[0];
        loadSeats(); // Reload to reset all buttons
        updateSelectionDisplay();
        showStatus("Selection cancelled");
        
        if (ssNavigationManager != null) {
            ssNavigationManager.goBack();
        }
    }

    public void handleHome(ActionEvent event) {
        if (ssNavigationManager != null) {
            ssNavigationManager.goHome();
        }
    }

    public void handleBook(ActionEvent event) {
        if (selectedSeatKeys == null || selectedSeatKeys.length == 0) {
            showStatus("Please select at least one seat");
            return;
        }

        try {
            // Convert selected seats to scheduleListSeatIds using strings
            int[] scheduleListSeatIds = new int[selectedSeatKeys.length];
            int index = 0;
            for (String seatKey : selectedSeatKeys) {
                ScheduleListSeat sls = findScheduleListSeatByKey(scheduleListSeatsData, seatKey);
                if (sls != null) {
                    scheduleListSeatIds[index++] = sls.scheduleListSeatId;
                }
            }

            if (index == 0) {
                showStatus("Error: Could not map seats to schedule list seat IDs");
                return;
            }

            // Resize array if needed
            if (index < scheduleListSeatIds.length) {
                int[] resized = new int[index];
                System.arraycopy(scheduleListSeatIds, 0, resized, 0, index);
                scheduleListSeatIds = resized;
            }

            int bookingId = model.createBooking(scheduleListSeatIds);
            showStatus("Booking created successfully! Booking ID: " + bookingId);
            
            // Clear selection after booking
            selectedSeatKeys = new String[0];
            updateSelectionDisplay();
            loadSeats();

            if (ssNavigationManager != null) {
                ssNavigationManager.onBookingSuccess(bookingId);
            }
        } catch (Exception e) {
            showStatus("Error creating booking: " + e.getMessage());
        }
    }

    private void showStatus(String message) {
        if (view != null && view.getStatusMessageLabel() != null) {
            view.getStatusMessageLabel().setText(message);
            view.getStatusMessageLabel().setVisible(true);
        }
    }

    // Public method to initialize with schedule ID
    public void setScheduleId(int scheduleId) {
        initializeData(scheduleId);
    }

    // Getter for view (used by SSNavigationManager)
    public CustomerSeatSelectionView getView() {
        return view;
    }

    // Setter for view (used by SSNavigationManager)
    public void setView(CustomerSeatSelectionView view) {
        this.view = view;
    }
}
