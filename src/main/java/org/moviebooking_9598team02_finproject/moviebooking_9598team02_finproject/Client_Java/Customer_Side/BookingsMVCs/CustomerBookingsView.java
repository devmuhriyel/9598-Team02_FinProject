package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem.*;

import java.io.IOException;

public class CustomerBookingsView {
    @FXML private VBox vbBookingsList;
    @FXML private ComboBox<String> comboFilterWatched, comboFilterStatus;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatusMessage;

    @FXML
    public void initialize() {
        // Setup initial dropdown options for JDK 8
        comboFilterWatched.setItems(javafx.collections.FXCollections.observableArrayList(
                "All Bookings", "Upcoming", "Watched (Past Date)"
        ));
        comboFilterWatched.getSelectionModel().selectFirst();

        comboFilterStatus.setItems(javafx.collections.FXCollections.observableArrayList(
                "All Statuses", "Confirmed", "Pending", "Cancelled", "Failed"
        ));
        comboFilterStatus.getSelectionModel().selectFirst();
    }

    public void clearList() { vbBookingsList.getChildren().clear(); }

    public void setStatus(String msg, boolean err) {
        lblStatusMessage.setText(msg);
        lblStatusMessage.setStyle(err ? "-fx-text-fill: red;" : "-fx-text-fill: yellow;");
    }

    public String getWatchedFilter() { return comboFilterWatched.getValue(); }
    public String getStatusFilter() { return comboFilterStatus.getValue(); }

    public void setOnRefresh(Runnable r) { btnRefresh.setOnAction(e -> r.run()); }
    public void setOnFilterChange(Runnable r) {
        comboFilterWatched.setOnAction(e -> r.run());
        comboFilterStatus.setOnAction(e -> r.run());
    }

    // Factory method for individual items
    public void createAndAddItem(BookingDetails data, CustomerBookingsController parentCtrl, CustomerBookingsModel parentMdl) {
        try {
            // 1. Use an absolute path starting from the root of the resources folder
            // ADJUST THIS PATH to match exactly where CustomerBookingItem.fxml is in your IntelliJ sidebar
            String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerBookingItem.fxml";

            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ ERROR: FXML not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);

            CustomerBookingItemView itemView = new CustomerBookingItemView();
            loader.setController(itemView);
            VBox node = loader.load();

            CustomerBookingItemController itemCtrl = new CustomerBookingItemController(itemView);
            itemCtrl.initData(data, parentCtrl, parentMdl);

            vbBookingsList.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modal Logic: Loads the Ticket FXML using an absolute path.
     */
    public void launchTicketModal(BookingDetails data, String custName) {
        try {
            String fxmlPath = "/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/TicketViewDetails.fxml";

            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ ERROR: FXML not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            CustomerTicketView ticketView = new CustomerTicketView();
            loader.setController(ticketView);

            // FIXED: Changed VBox to BorderPane to match TicketViewDetails.fxml
            javafx.scene.layout.BorderPane root = loader.load();

            CustomerTicketViewController ctrl = new CustomerTicketViewController(ticketView);
            ctrl.setTicketData(data, custName);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            // Scene can take a BorderPane perfectly fine
            stage.setScene(new Scene(root));
            stage.setTitle("E-Ticket | Booking ID: " + data.bookingId);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ IOException in launchTicketModal:");
            e.printStackTrace();
        }
    }

    public void runOnUIThread(Runnable r) { Platform.runLater(r); }
}