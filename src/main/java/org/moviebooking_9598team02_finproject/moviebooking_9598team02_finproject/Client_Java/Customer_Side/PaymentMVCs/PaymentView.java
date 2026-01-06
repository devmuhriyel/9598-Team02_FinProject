package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentView {

    @FXML private Label lblReceiptDate, lblReceiptTime;
    @FXML private Label lblBookingId, lblCustomerName;
    @FXML private Label lblMovieTitle, lblShowDate, lblShowTime, lblCinema, lblSeats; // Optional fields
    @FXML private Label lblTicketPrice, lblTicketCount, lblSubtotal, lblServiceFee, lblAmount;

    @FXML private ComboBox<String> comboPaymentMethod;
    @FXML private TextField txtTransactionId;
    @FXML private Label lblStatus;

    @FXML private Button btnConfirmPayment;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        LocalDateTime now = LocalDateTime.now();
        lblReceiptDate.setText("Date: " + now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        lblReceiptTime.setText("Time: " + now.format(DateTimeFormatter.ofPattern("hh:mm a")));

        comboPaymentMethod.getItems().addAll("credit_card", "online");
    }

    // ===== METHODS CALLED BY CONTROLLER =====

    public void setCustomerDetails(String name, String bookingId) {
        lblCustomerName.setText(name);
        lblBookingId.setText(bookingId);
    }

    public void setFinancials(double amount) {
        if(lblAmount != null) {
            lblAmount.setText(String.format("P %.2f", amount));
        }
        // Set other labels to empty or defaults if data is missing
        if(lblSubtotal != null) lblSubtotal.setText(String.format("P %.2f", amount));
    }

    // ===== STATUS & UI HELPERS =====

    public void showStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setVisible(true);
        lblStatus.setStyle(isError ? "-fx-background-color: #ff2525; -fx-text-fill: white;" : "-fx-background-color: #28a745; -fx-text-fill: white;");
    }

    public String getSelectedMethod() { return comboPaymentMethod.getValue(); }
    public String getTransactionId() { return txtTransactionId.getText(); }
    public void setTransactionId(String txnId) { txtTransactionId.setText(txnId); }

    public void setTransactionFieldEditable(boolean editable) { txtTransactionId.setDisable(!editable); }
    public void setTransactionFieldPrompt(String prompt) { txtTransactionId.setPromptText(prompt); }

    public void setConfirmButtonEnabled(boolean enabled) { btnConfirmPayment.setDisable(!enabled); }
    public void setConfirmButtonVisible(boolean visible) { btnConfirmPayment.setVisible(visible); }
    public void setCancelButtonText(String text) { btnCancel.setText(text); }

    public void setConfirmButtonAction(Runnable action) { btnConfirmPayment.setOnAction(e -> action.run()); }
    public void setCancelButtonAction(Runnable action) { btnCancel.setOnAction(e -> action.run()); }
    public void setPaymentMethodChangeAction(Runnable action) { comboPaymentMethod.setOnAction(e -> action.run()); }
    public void setMovieInfo(String movieTitle, String showDate, String showTime,
                             String cinema, String seats) {
        if (lblMovieTitle != null) {
            lblMovieTitle.setText(movieTitle);
        }
        if (lblShowDate != null) {
            lblShowDate.setText(showDate);
        }
        if (lblShowTime != null) {
            lblShowTime.setText(showTime);
        }
        if (lblCinema != null) {
            lblCinema.setText(cinema);
        }
        if (lblSeats != null) {
            lblSeats.setText(seats);
        }
    }

    public void setDetailedFinancials(double totalAmount, int ticketCount, double ticketPrice) {
        // Total Amount
        if (lblAmount != null) {
            lblAmount.setText(String.format("₱ %.2f", totalAmount));
        }

        // Individual Ticket Price
        if (lblTicketPrice != null) {
            lblTicketPrice.setText(String.format("₱ %.2f", ticketPrice));
        }

        // Number of Tickets
        if (lblTicketCount != null) {
            lblTicketCount.setText("x " + ticketCount);
        }

        // Subtotal (same as total for now)
        if (lblSubtotal != null) {
            lblSubtotal.setText(String.format("₱ %.2f", totalAmount));
        }

        // Service Fee (currently 0)
        if (lblServiceFee != null) {
            lblServiceFee.setText("₱ 0.00");
        }
    }

    public void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void runOnUIThread(Runnable action) { Platform.runLater(action); }
}