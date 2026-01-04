package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;

public class CustomerSeatSelectionModel extends CustomerParentModel {

    private ClientService clientService;
    private int currentScheduleId;

    // ✅ FIX: Accept session data in constructor
    public CustomerSeatSelectionModel(int scheduleId, int customerId, String sessionToken) {
        this.currentScheduleId = scheduleId;
        this.customerId = customerId;
        this.sessionToken = sessionToken;
        this.clientService = clientServices;
    }

    // Get available seats for a schedule
    public Seat[] getAvailableSeats() throws ScheduleNotFoundException {
        return clientService.getAvailableSeats(currentScheduleId);
    }

    // Get schedule list seats for a schedule (returns ScheduleListSeat[])
    public ScheduleListSeat[] getAvailableSeats(int scheduleID) throws ScheduleNotFoundException {
        return commonServices.getScheduleSeats(scheduleID);
    }

    // Create booking
    public int createBooking(int customerId, String sessionToken, int scheduleId, int[] scheduleListSeatIds)
            throws AuthenticationException, SeatNotAvailableException, BookingException {
        // ✅ Use stored session token instead of parameter
        return clientService.createBooking(this.customerId, this.sessionToken, scheduleId, scheduleListSeatIds);
    }

    // Get schedule details with seat availability info
    public ScheduleDetails getScheduleDetailsView() throws ScheduleNotFoundException {
        return clientService.getScheduleDetailsView(currentScheduleId);
    }
}