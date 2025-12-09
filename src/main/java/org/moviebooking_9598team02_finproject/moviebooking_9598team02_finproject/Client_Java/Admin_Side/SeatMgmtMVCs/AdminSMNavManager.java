package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

import java.io.IOException;

public class AdminSMNavManager {
    private final int currentAdminId;
    private Pane seatMgmtPane;

    private SeatController seatController;
    private SeatModel seatModel;
    private SeatView seatView;

    public AdminSMNavManager(Admin admin) {
        this.currentAdminId = admin.adminId;
    }

    private void initializeSeatMgmtView() throws IOException {
        seatModel = new SeatModel(currentAdminId);
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminSeatMgmt.fxml")
        );
        seatMgmtPane = loader.load();
        seatView = loader.getController();
        seatController = new SeatController(seatModel, seatView, this);
        seatController.initializeSeatButtons();
    }

    public Pane getSeatMgmtView() {
        if (seatMgmtPane == null) {
            try {
                initializeSeatMgmtView();
            } catch (IOException e) {
                e.printStackTrace();
                seatMgmtPane = new Pane(new javafx.scene.control.Label("Error loading Seat Management View: " + e.getMessage()));
            }
        }
        return seatMgmtPane;
    }
}