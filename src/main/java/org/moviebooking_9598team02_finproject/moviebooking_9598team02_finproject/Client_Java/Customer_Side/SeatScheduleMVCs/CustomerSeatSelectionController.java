package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
// ✅ FIX: Import the correct manager
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs.MovieNavigationManager;

import java.util.*;

public class CustomerSeatSelectionController {

    private CustomerSeatSelectionView view;
    private CustomerSeatSelectionModel model;
    // ✅ FIX: Use MovieNavigationManager
    private MovieNavigationManager navManager;

    private ScheduleListSeat[] scheduleListSeatsData = new ScheduleListSeat[0];
    private Seat[] allSeatsData = new Seat[0];
    private Map<Integer, Seat> seatMap = new HashMap<>();

    private String[] selectedSeatKeys = new String[0];
    private int currentScheduleId;

    // ✅ FIX: Constructor accepts MovieNavigationManager
    public CustomerSeatSelectionController(CustomerSeatSelectionView view, CustomerSeatSelectionModel model, MovieNavigationManager navManager) {
        this.view = view;
        this.model = model;
        this.navManager = navManager;
    }

    public void init(int scheduleId) {
        this.currentScheduleId = scheduleId;
        wireButtons();
        initializeData(scheduleId);
    }

    private void initializeData(int scheduleID) {
        try {
            System.out.println("🔄 Loading seats for schedule ID: " + scheduleID);

            // Load ALL seats from database
            allSeatsData = model.getAvailableSeats();

            // Create map for quick lookup
            seatMap.clear();
            for (Seat seat : allSeatsData) {
                seatMap.put(seat.seatId, seat);
            }

            // Load schedule-specific availability
            scheduleListSeatsData = model.getAvailableSeats(scheduleID);

            loadSeats();
            showStatus("Seats loaded successfully. Select your seats and click Book.");
        } catch (Exception e) {
            showStatus("Error loading schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void wireButtons() {
        if (view == null) return;
        view.setCancelButtonAction(this::handleCancel);
        view.setHomeButtonAction(this::handleHome);
        view.setBookButtonAction(this::handleBook);
    }

    private void loadSeats() {
        if (model == null) return;
        try {
            updateSeatButtons();
            showStatus("Seats loaded successfully. Select your seats.");
        } catch (Exception e) {
            showStatus("Error loading seats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSeatButtons() {
        List<String> allSeatKeys = view.getAllSeatButtonKeys();

        for (String seatKey : allSeatKeys) {
            String row = seatKey.substring(0, 1);
            int seatNum;
            try {
                seatNum = Integer.parseInt(seatKey.substring(1));
            } catch (NumberFormatException e) {
                continue;
            }

            Seat seat = findSeat(allSeatsData, row, seatNum);
            if (seat != null) {
                ScheduleListSeat sls = findScheduleListSeat(scheduleListSeatsData, seat.seatId);

                if (sls != null) {
                    if ("available".equalsIgnoreCase(sls.status)) {
                        // Available seat
                        String style = "available";
                        if ("vip".equalsIgnoreCase(seat.seatType)) style = "vip";
                        else if ("maintenance".equalsIgnoreCase(seat.seatType)) style = "maintenance";

                        view.applySeatStyle(seatKey, style);
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

    private ScheduleListSeat findScheduleListSeatByKey(String seatKey) {
        if (seatKey == null || seatKey.length() < 2) return null;
        String row = seatKey.substring(0, 1);
        int seatNum;
        try {
            seatNum = Integer.parseInt(seatKey.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        Seat seat = findSeat(allSeatsData, row, seatNum);
        if (seat != null) {
            return findScheduleListSeat(scheduleListSeatsData, seat.seatId);
        }
        return null;
    }

    private void handleSeatClick(String key, Seat seat, ScheduleListSeat sls) {
        List<String> list = new ArrayList<>();
        if (selectedSeatKeys != null) {
            list.addAll(Arrays.asList(selectedSeatKeys));
        }

        if (list.contains(key)) {
            // Deselect
            list.remove(key);
            String style = "available";
            if ("vip".equalsIgnoreCase(seat.seatType)) style = "vip";
            view.applySeatStyle(key, style);
        } else {
            // Select
            list.add(key);
            view.applySeatStyle(key, "selected");
        }

        selectedSeatKeys = list.toArray(new String[0]);
        Collections.sort(list);
        view.updateSelectedSeatsDisplay(list);
    }

    public void handleCancel() { view.closeWindow(); }
    public void handleHome() { view.closeWindow(); }

    // ==========================================
    // ✅ FIX: UPDATED HANDLE BOOK METHOD
    // ==========================================
    public void handleBook() {
        if (selectedSeatKeys == null || selectedSeatKeys.length == 0) {
            showStatus("Please select at least one seat");
            return;
        }

        try {
            // 1. Map Keys to IDs
            int[] scheduleListSeatIds = new int[selectedSeatKeys.length];
            int i = 0;
            for(String key : selectedSeatKeys) {
                ScheduleListSeat sls = findScheduleListSeatByKey(key);
                if(sls != null) scheduleListSeatIds[i++] = sls.scheduleListSeatId;
            }

            showStatus("Creating booking...");

            // 2. Create Booking (CORBA)
            int bookingId = model.createBooking(scheduleListSeatIds);

            // 3. Calculate Total Price
            double totalPrice = calculateTotalPrice(scheduleListSeatIds);

            System.out.println("✅ Booking created: #" + bookingId + " | Total: ₱" + totalPrice);

            // 4. CALL NAV MANAGER TO SHOW PAYMENT
            if (navManager != null) {
                navManager.showPayment(bookingId, totalPrice);
            } else {
                System.err.println("❌ CRITICAL: NavManager is null! Cannot open payment.");
            }

            // 5. Close this window
            view.closeWindow();

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
                        if (seat != null) total += seat.seatPrice;
                        break;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    private void showStatus(String message) {
        if (view != null) view.showStatus(message);
        System.out.println("📢 Status: " + message);
    }
}