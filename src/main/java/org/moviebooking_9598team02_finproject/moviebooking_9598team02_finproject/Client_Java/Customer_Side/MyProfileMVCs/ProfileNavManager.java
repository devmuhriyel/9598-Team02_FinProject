package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import java.io.IOException;

public class ProfileNavManager {

    private final Customer customer;
    private Pane profileViewPane;

    public ProfileNavManager(Customer customer) {
        this.customer = customer;
    }

    public Pane getMyProfileView() {
        if (profileViewPane == null) {
            try {
                String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerProfile.fxml";
                java.net.URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    System.err.println("FXML FILE NOT FOUND AT: " + fxmlPath);
                    System.err.println("Please check your src/main/resources folder structure!");
                    return null;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                ProfileView view = new ProfileView();
                loader.setController(view);
                VBox root = loader.load();

                // --- THE FIX: Pass the full 'customer' object ---
                ProfileModel model = new ProfileModel();

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