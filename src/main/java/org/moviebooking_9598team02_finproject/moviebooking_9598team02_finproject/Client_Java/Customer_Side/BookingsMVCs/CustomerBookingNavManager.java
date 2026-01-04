package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import java.io.IOException;

public class CustomerBookingNavManager {

    private Customer customer;
    private String sessionToken; // ✅ ADD: Store session token
    private Pane bookingsViewPane;

    // ✅ FIX: Constructor now accepts sessionToken
    public CustomerBookingNavManager(Customer customer, String sessionToken) {
        this.customer = customer;
        this.sessionToken = sessionToken;
    }

    public Pane getMyBookingsView() {
        if (bookingsViewPane == null) {
            try {
                String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerBookings.fxml";

                java.net.URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    System.err.println("❌ FXML FILE NOT FOUND AT: " + fxmlPath);
                    return null;
                }

                FXMLLoader loader = new FXMLLoader(resource);

                CustomerBookingsView view = new CustomerBookingsView();
                loader.setController(view);
                VBox root = loader.load();

                // ✅ FIX: Pass session data to model
                CustomerBookingsModel model = new CustomerBookingsModel(
                        customer.customerId,
                        sessionToken,
                        customer.fullName
                );

                new CustomerBookingsController(view, model);

                bookingsViewPane = root;
                System.out.println("✅ Customer Bookings MVC wired successfully");
            } catch (IOException e) {
                System.err.println("❌ IOException during FXML load:");
                e.printStackTrace();
                return null;
            }
        }
        return bookingsViewPane;
    }
}