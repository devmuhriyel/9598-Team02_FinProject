package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

import java.io.IOException;

public class AdminBookingNavManager {

    private Admin admin;
    private Pane bookingViewPane;
    public AdminBookingNavManager(Admin admin) {
        this.admin = admin;
    }

    public Pane getBookingMgmtView() {
        if (bookingViewPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminBookingMgmt.fxml"));
                bookingViewPane = loader.load();
                AdminBookingView view = loader.getController();
                AdminBookingModel model = new AdminBookingModel();
                new AdminBookingController(view, model, admin.adminId);

            } catch (IOException e) {
                e.printStackTrace();
                bookingViewPane = new Pane();
            }
        }
        return bookingViewPane;
    }
}