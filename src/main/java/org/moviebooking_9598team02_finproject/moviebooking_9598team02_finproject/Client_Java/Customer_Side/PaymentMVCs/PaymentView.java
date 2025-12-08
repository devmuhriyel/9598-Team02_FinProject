package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentView {

    // VARIABLES
    @FXML private Label lblReceiptDate, lblReceiptTime;
    @FXML private Label lblBookingId, lblCustomerName;
    @FXML private Label lblMovieTitle, lblShowDate, lblShowTime, lblCinema, lblSeats;
    @FXML private Label lblTicketPrice, lblTicketCount, lblSubtotal, lblServiceFee, lblAmount;

    @FXML private ComboBox<String> comboPaymentMethod;
    @FXML private TextField txtTransactionId;
    @FXML private Label lblStatus;

    @FXML private Button btnConfirmPayment;
    @FXML private Button btnCancel;


    @FXML
    public void initialize() {
        // Set Date/Time automatically
        LocalDateTime now = LocalDateTime.now();
        lblReceiptDate.setText("Date: " + now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        lblReceiptTime.setText("Time: " + now.format(DateTimeFormatter.ofPattern("hh:mm a")));

        // Populate Combo Box
        comboPaymentMethod.getItems().addAll("credit_card", "cash", "online");
    }

    // SETTERS KO DITO
    public void setBookingDetails(String bookingRef, String customerName, String movie,
                                  String date, String time, String cinema, String seats) {
        lblBookingId.setText(bookingRef);
        lblCustomerName.setText(customerName);
        lblMovieTitle.setText(movie);
        lblShowDate.setText(date);
        lblShowTime.setText(time);
        lblCinema.setText(cinema);
        lblSeats.setText(seats);
    }

    public void setPriceDetails(String price, String count, String sub, String fee, String total) {
        lblTicketPrice.setText(price);
        lblTicketCount.setText(count);
        lblSubtotal.setText(sub);
        lblServiceFee.setText(fee);
        lblAmount.setText(total);
    }

    public void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setVisible(true);
        lblStatus.setStyle(isError ?
                "-fx-background-color: #ff2525; -fx-text-fill: white;" :
                "-fx-background-color: #28a745; -fx-text-fill: white;");
    }

    //GETTERS DAW DITO
    public Button getConfirmButton() { return btnConfirmPayment; }
    public Button getCancelButton() { return btnCancel; }
    public ComboBox<String> getPaymentMethodCombo() { return comboPaymentMethod; }
    public TextField getTransactionField() { return txtTransactionId; }

    public String getSelectedMethod() { return comboPaymentMethod.getValue(); }
    public String getTransactionId() { return txtTransactionId.getText(); }
}