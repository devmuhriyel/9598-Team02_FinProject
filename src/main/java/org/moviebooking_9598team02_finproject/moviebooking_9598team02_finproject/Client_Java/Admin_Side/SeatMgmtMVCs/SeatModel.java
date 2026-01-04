package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

public class SeatModel extends AdminParentModel {

    public SeatModel() {
    }

    // === SEAT MANAGEMENT METHODS ===

    public void updateSeatPrice(int seatId, double newPrice) throws AuthenticationException {
        adminServices.updateSeatPrice(adminId, sessionToken, seatId, newPrice);
    }

    public void updateSeatType(int seatId, String newType) throws AuthenticationException {
        adminServices.updateSeatType(adminId, sessionToken, seatId, newType);
    }

    public Seat[] getAllSeats() throws AuthenticationException {
        return adminServices.getAllSeats(adminId, sessionToken);
    }
}