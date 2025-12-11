package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CustomerBookingItemView {

    @FXML private Label lblMovieTitle;
    @FXML private Label lblTimeStatus, lblBookingStatus, lblPaymentStatus;
    @FXML private Label lblBookingDate, lblScreeningDateTime, lblSeats, lblTotalPaid;
    @FXML private Button btnViewTicket;

    public void setMainDetails(String title, String bookDate, String showTime, String seats, String price) {
        lblMovieTitle.setText(title);
        lblBookingDate.setText(bookDate);
        lblScreeningDateTime.setText(showTime);
        lblSeats.setText(seats);
        lblTotalPaid.setText(price);
    }

    public void setStatusBadge(Label label, String text, String color) {
        label.setText(text.toUpperCase());
        label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-weight: bold; -fx-font-size: 11px;");
    }

    // Getters for the controller to apply styles to specific badges
    public Label getLblTimeStatus() { return lblTimeStatus; }
    public Label getLblBookingStatus() { return lblBookingStatus; }
    public Label getLblPaymentStatus() { return lblPaymentStatus; }

    public void setOnViewTicket(Runnable action) {
        btnViewTicket.setOnAction(e -> action.run());
    }
}