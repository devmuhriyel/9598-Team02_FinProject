package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.util.*;

public class CustomerSeatSelectionController {

    private CustomerSeatSelectionView view;
    private CustomerSeatSelectionModel model;
    private SSNavigationManager ssNavigationManager;

    private ScheduleListSeat[] scheduleListSeatsData = new ScheduleListSeat[0];
    private Seat[] allSeatsData = new Seat[0];
    private Map<Integer, Seat> seatMap = new HashMap<>();

    private String[] selectedSeatKeys = new String[0];
    private int currentScheduleId;

    public CustomerSeatSelectionController(CustomerSeatSelectionView view, CustomerSeatSelectionModel model, SSNavigationManager ssNavigationManager) {
        this.view = view;
        this.model = model;
        this.ssNavigationManager = ssNavigationManager;
    }

    public void init(int scheduleId) {
        this.currentScheduleId = scheduleId;
        wireButtons();
        initializeData(scheduleId);
    }

    private void initializeData(int scheduleID) {
        try {
            System.out.println("🔄 Loading seats for schedule ID: " + scheduleID);

            // Load ALL seats from database (with their types)
            allSeatsData = model.getAvailableSeats();
            System.out.println("✅ Loaded " + allSeatsData.length + " total seats from database");

            // Create map for quick lookup
            seatMap.clear();
            for (Seat seat : allSeatsData) {
                seatMap.put(seat.seatId, seat);
                System.out.println("   Seat " + seat.seatRow + seat.seatNumber +
                        " | Type: " + seat.seatType +
                        " | Price: ₱" + seat.seatPrice);
            }

            // Load schedule-specific availability
            scheduleListSeatsData = model.getAvailableSeats(scheduleID);
            System.out.println("✅ Loaded " + scheduleListSeatsData.length + " schedule list seats");

            for (ScheduleListSeat sls : scheduleListSeatsData) {
                Seat seat = seatMap.get(sls.seatId);
                if (seat != null) {
                    System.out.println("   Schedule Seat " + seat.seatRow + seat.seatNumber +
                            " | Status: " + sls.status);
                }
            }

            loadSeats();
            showStatus("Seats loaded successfully. Select your seats and click Book.");
        } catch (Exception e) {
            showStatus("Error loading schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void wireButtons() {
        if (view == null) return;
        view.setCancelButtonAction(() -> handleCancel());
        view.setHomeButtonAction(() -> handleHome());
        view.setBookButtonAction(() -> handleBook());
    }

    private void loadSeats() {
        if (model == null || scheduleListSeatsData == null || allSeatsData == null) {
            return;
        }

        try {
            updateSeatButtons();
            showStatus("Seats loaded successfully. Select your seats.");
        } catch (Exception e) {
            showStatus("Error loading seats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSeatButtons() {
        System.out.println("🎨 Updating seat button styles...");

        List<String> allSeatKeys = view.getAllSeatButtonKeys();

        for (String seatKey : allSeatKeys) {
            String row = seatKey.substring(0, 1);
            int seatNum;
            try {
                seatNum = Integer.parseInt(seatKey.substring(1));
            } catch (NumberFormatException e) {
                continue;
            }

            // Find seat from database
            Seat seat = findSeat(allSeatsData, row, seatNum);
            if (seat != null) {
                // Find schedule-specific availability
                ScheduleListSeat sls = findScheduleListSeat(scheduleListSeatsData, seat.seatId);

                if (sls != null) {
                    System.out.println("   Button " + seatKey +
                            " | Seat Type: " + seat.seatType +
                            " | Status: " + sls.status);

                    if ("available".equalsIgnoreCase(sls.status)) {
                        // Available seat - show its type
                        if ("vip".equalsIgnoreCase(seat.seatType)) {
                            view.applySeatStyle(seatKey, "vip");
                        } else if ("maintenance".equalsIgnoreCase(seat.seatType)) {
                            view.applySeatStyle(seatKey, "maintenance");
                        } else {
                            view.applySeatStyle(seatKey, "available");
                        }
                        view.enableSeatButton(seatKey, true);
                        view.setSeatClickHandler(seatKey, () -> handleSeatClick(seatKey, seat, sls));

                    } else if ("maintenance".equalsIgnoreCase(sls.status)) {
                        view.applySeatStyle(seatKey, "maintenance");
                        view.enableSeatButton(seatKey, false);

                    } else {
                        // Booked/taken
                        view.applySeatStyle(seatKey, "taken");
                        view.enableSeatButton(seatKey, false);
                    }
                } else {
                    System.out.println("   ⚠️ Button " + seatKey + " - No schedule list seat found!");
                    view.enableSeatButton(seatKey, false);
                }
            } else {
                System.out.println("   ⚠️ Button " + seatKey + " - No seat data found!");
            }
        }

        System.out.println("✅ Seat button styles updated");
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

    private ScheduleListSeat findScheduleListSeatByKey(String seatKey) {
        if (seatKey == null || seatKey.length() < 2) return null;
        String row = seatKey.substring(0, 1);
        int seatNum;
        try {
            seatNum = Integer.parseInt(seatKey.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        try {
            Seat seat = findSeat(allSeatsData, row, seatNum);
            if (seat != null) {
                return findScheduleListSeat(scheduleListSeatsData, seat.seatId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleSeatClick(String key, Seat seat, ScheduleListSeat sls) {
        boolean isSelected = containsString(selectedSeatKeys, key);

        if (isSelected) {
            // Deselect seat
            selectedSeatKeys = removeString(selectedSeatKeys, key);

            // Restore original style based on seat type
            if ("vip".equalsIgnoreCase(seat.seatType)) {
                view.applySeatStyle(key, "vip");
            } else {
                view.applySeatStyle(key, "available");
            }
        } else {
            // Select seat
            selectedSeatKeys = addString(selectedSeatKeys, key);
            view.applySeatStyle(key, "selected");
        }

        updateSelectionDisplay();
    }

    private boolean containsString(String[] array, String value) {
        if (array == null) return false;
        for (String s : array) {
            if (value.equals(s)) return true;
        }
        return false;
    }

    private String[] addString(String[] array, String value) {
        if (containsString(array, value)) return array;
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = value;
        return newArray;
    }

    private String[] removeString(String[] array, String value) {
        if (!containsString(array, value)) return array;
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
        view.updateSelectedSeatsDisplay(selectedList);
    }

    public void handleCancel() {
        selectedSeatKeys = new String[0];
        loadSeats();
        updateSelectionDisplay();
        showStatus("Selection cancelled");
        view.closeWindow();
    }

    public void handleHome() {
        view.closeWindow();
    }

    public void handleBook() {
        if (selectedSeatKeys == null || selectedSeatKeys.length == 0) {
            showStatus("Please select at least one seat");
            return;
        }

        try {
            // Map selected seat keys to schedule list seat IDs
            int[] scheduleListSeatIds = new int[selectedSeatKeys.length];
            int index = 0;

            for (String seatKey : selectedSeatKeys) {
                ScheduleListSeat sls = findScheduleListSeatByKey(seatKey);
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

            showStatus("Creating booking...");

            // Create booking through CORBA
            int bookingId = model.createBooking(scheduleListSeatIds);
            double totalPrice = calculateTotalPrice(scheduleListSeatIds);

            showStatus("Booking created! ID: " + bookingId);
            System.out.println("✅ Booking created: #" + bookingId + " | Total: ₱" + totalPrice);

            // Get schedule details for payment window
            ScheduleDetails scheduleDetails = model.getScheduleDetailsView();

            // Calculate price breakdown
            double ticketPrice = selectedSeatKeys.length > 0 ? totalPrice / selectedSeatKeys.length : 0;
            double serviceFee = 0.0;

            // Open payment window via view
            view.openPaymentWindow(
                    model.getCurrentCustomerId(),
                    bookingId,
                    totalPrice,
                    scheduleDetails,
                    selectedSeatKeys,
                    ticketPrice,
                    serviceFee
            );

        } catch (Exception e) {
            showStatus("Error creating booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateTotalPrice(int[] scheduleListSeatIds) {
        double total = 0.0;
        try {
            for (int slsId : scheduleListSeatIds) {
                for (ScheduleListSeat sls : scheduleListSeatsData) {
                    if (sls.scheduleListSeatId == slsId) {
                        Seat seat = seatMap.get(sls.seatId);
                        if (seat != null) {
                            total += seat.seatPrice;
                            System.out.println("   Adding seat price: ₱" + seat.seatPrice + " | Total: ₱" + total);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("💰 Total booking price: ₱" + total);
        return total;
    }

    private void showStatus(String message) {
        if (view != null) {
            view.showStatus(message);
        }
        System.out.println("📢 Status: " + message);
    }

    public void setScheduleId(int scheduleId) {
        initializeData(scheduleId);
    }

    public CustomerSeatSelectionView getView() {
        return view;
    }

    public void setView(CustomerSeatSelectionView view) {
        this.view = view;
    }
}