package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs.SeatController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs.SeatModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs.SeatView;

import java.io.IOException;

public class AdminSMNavManager {

    private final AdminService adminService;
    private final int currentAdminId;
    private Pane seatMgmtPane; // The root pane of the loaded FXML

    private SeatController seatController;
    private SeatModel seatModel;
    private SeatView seatView;

    public AdminSMNavManager(AdminService adminService, Admin admin) {
        this.adminService = adminService;
        this.currentAdminId = admin.adminId;
    }

    private void initializeSeatMgmtView() throws IOException {
        seatModel = new SeatModel(adminService, currentAdminId);
        seatView = new SeatView();
        seatController = new SeatController(seatModel, seatView);
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminSeatMgmt.fxml")
        );

        loader.setControllerFactory(c -> seatController);

        seatMgmtPane = loader.load();
    }

    public Pane getSeatMgmtView() {
        if (seatMgmtPane == null) {
            try {
                initializeSeatMgmtView();
            } catch (IOException e) {
                e.printStackTrace();
                seatMgmtPane = new Pane(new javafx.scene.control.Label("Error loading Seat Management View."));
            }
        }
        return seatMgmtPane;
    }
}