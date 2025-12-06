package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

public class SeatModel extends AdminParentModel {

    private final AdminService adminService;
    private int currentAdminId;

    public SeatModel(AdminService adminService, int adminId) {
        this.adminService = adminService;
        this.currentAdminId = adminId;
    }

    // === SEAT MANAGEMENT METHODS ===

    public void updateSeatPrice(int seatId, double newPrice) throws AuthenticationException {
        adminService.updateSeatPrice(currentAdminId, seatId, newPrice);
    }

    public void updateSeatType(int seatId, String newType) throws AuthenticationException {
        adminService.updateSeatType(currentAdminId, seatId, newType);
    }

    public Seat[] getAllSeats() throws AuthenticationException {
        return adminService.getAllSeats(currentAdminId);
    }

    // === FOR ADMIN ID ===

    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }
}