package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

import java.io.IOException;

public class AdminBookingNavManager {

    private Admin admin;
    private String sessionToken;
    private Pane bookingViewPane;

    public AdminBookingNavManager(Admin admin, String sessionToken) {
        this.admin = admin;
        this.sessionToken = sessionToken; // ✅ STORE
    }

    public Pane getBookingMgmtView() {
        if (bookingViewPane == null) {
            try {
                // FIXED PATH: Added "/Admin/" before the filename
                String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminBookingMgmt.fxml";

                // Debug check to verify path before loading
                if (getClass().getResource(fxmlPath) == null) {
                    System.err.println("❌ CRITICAL ERROR: Could not find FXML at: " + fxmlPath);
                    return null;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                bookingViewPane = loader.load();

                AdminBookingView view = loader.getController();
                AdminBookingModel model = new AdminBookingModel();

                // Wire the controller manually
                new AdminBookingController(view, model, admin.adminId);

            } catch (IOException e) {
                System.err.println("❌ Error loading Admin Booking FXML:");
                e.printStackTrace();
                bookingViewPane = new Pane(); // Return empty pane to prevent crash
            }
        }
        return bookingViewPane;
    }
}