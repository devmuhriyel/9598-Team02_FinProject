package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import java.io.IOException;

public class ProfileNavManager {

    private final Customer customer;
    private final String sessionToken; // ✅ ADD
    private Pane profileViewPane;

    // ✅ FIX: Accept sessionToken
    public ProfileNavManager(Customer customer, String sessionToken) {
        this.customer = customer;
        this.sessionToken = sessionToken;
    }

    public Pane getMyProfileView() {
        if (profileViewPane == null) {
            try {
                String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerProfile.fxml";
                java.net.URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    System.err.println("FXML FILE NOT FOUND AT: " + fxmlPath);
                    return null;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                ProfileView view = new ProfileView();
                loader.setController(view);
                VBox root = loader.load();

                // ✅ FIX: Initialize model with session data
                ProfileModel model = new ProfileModel(
                        customer.customerId,
                        sessionToken,
                        customer.username,
                        customer.fullName,
                        customer.email,
                        customer.phoneNumber
                );

                new ProfileController(model, view);

                profileViewPane = root;
                System.out.println("Customer Profile MVC wired successfully");
            } catch (IOException e) {
                System.err.println("IOException during FXML load:");
                e.printStackTrace();
                return null;
            }
        }
        return profileViewPane;
    }
}