package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomerTicketView {

    @FXML private Label lblBookingId, lblCustomerName, lblBookingStatus;
    @FXML private Label lblMovieTitle, lblShowDate, lblShowTime, lblCinema, lblSeats;
    @FXML private Label lblTicketPrice, lblTicketCount, lblSubtotal, lblServiceFee, lblAmount;
    @FXML private Label lblPaymentStatus, lblPaymentMethod, lblTransactionId;
    @FXML private Button btnClose;

    // 1. Matches b.bookingId (int) and name (String)
    public void setBookingDetails(int bookingId, String customerName) {
        lblBookingId.setText("BK-" + String.format("%05d", bookingId));
        lblCustomerName.setText(customerName);
    }

    // 2. Matches the 5 String arguments from Controller
    public void setMovieInfo(String title, String date, String time, String cinema, String seats) {
        lblMovieTitle.setText(title);
        lblShowDate.setText(date);
        lblShowTime.setText(time);
        lblCinema.setText(cinema);
        lblSeats.setText(seats);
    }

    // 3. Matches the financial strings
    public void setFinancials(String perTicket, String count, String sub, String fee, String total) {
        lblTicketPrice.setText(perTicket);
        lblTicketCount.setText("x " + count);
        lblSubtotal.setText(sub);
        lblServiceFee.setText(fee);
        lblAmount.setText(total);
    }

    // 4. Matches the payment strings
    public void setPaymentInfo(String status, String method, String txn) {
        lblPaymentStatus.setText(status);
        lblPaymentMethod.setText(method);
        lblTransactionId.setText(txn);
    }

    // 5. Matches the status text and color strings
    public void setBookingStatusText(String status, String color) {
        lblBookingStatus.setText(status.toUpperCase());
        lblBookingStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    public void setPaymentStatusText(String status, String color) {
        lblPaymentStatus.setText(status.toUpperCase());
        lblPaymentStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    public void setOnClose(Runnable action) {
        btnClose.setOnAction(e -> action.run());
    }

    public void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (stage != null) stage.close();
    }
}