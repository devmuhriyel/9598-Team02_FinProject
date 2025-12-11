package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import java.io.IOException;

public class CustomerBookingNavManager {

    private Customer customer;
    private Pane bookingsViewPane;

    public CustomerBookingNavManager(Customer customer) {
        this.customer = customer;
    }

    public Pane getMyBookingsView() {
        if (bookingsViewPane == null) {
            try {
                // 1. Define the path string
                String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerBookings.fxml";

                // 2. Check if resource exists
                java.net.URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    System.err.println("❌ FXML FILE NOT FOUND AT: " + fxmlPath);
                    System.err.println("Please check your src/main/resources folder structure!");
                    return null;
                }

                FXMLLoader loader = new FXMLLoader(resource);

                // Strict MVC Setup
                CustomerBookingsView view = new CustomerBookingsView();
                loader.setController(view);
                VBox root = loader.load();

                CustomerBookingsModel model = new CustomerBookingsModel(customer.customerId, customer.fullName);
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