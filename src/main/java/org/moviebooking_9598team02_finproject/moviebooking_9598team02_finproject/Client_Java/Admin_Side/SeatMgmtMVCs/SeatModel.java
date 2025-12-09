package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

public class SeatModel extends AdminParentModel {

    private int currentAdminId;

    public SeatModel(int adminId) {
        this.currentAdminId = adminId;
    }

    // === SEAT MANAGEMENT METHODS ===

    public void updateSeatPrice(int seatId, double newPrice) throws AuthenticationException {
        adminServices.updateSeatPrice(currentAdminId, seatId, newPrice);
    }

    public void updateSeatType(int seatId, String newType) throws AuthenticationException {
        adminServices.updateSeatType(currentAdminId, seatId, newType);
    }

    public Seat[] getAllSeats() throws AuthenticationException {
        System.out.println(currentAdminId);
        return adminServices.getAllSeats(currentAdminId);
    }

    // === FOR ADMIN ID ===

    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }
}