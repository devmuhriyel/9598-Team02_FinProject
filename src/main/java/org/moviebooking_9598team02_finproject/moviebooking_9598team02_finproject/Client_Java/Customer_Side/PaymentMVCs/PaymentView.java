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

    // ===== FXML INJECTED FIELDS =====
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

    // ===== PUBLIC API FOR CONTROLLER =====

    /**
     * Set booking details on the receipt
     */
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

    /**
     * Set price details on the receipt
     */
    public void setPriceDetails(String price, String count, String sub, String fee, String total) {
        lblTicketPrice.setText(price);
        lblTicketCount.setText(count);
        lblSubtotal.setText(sub);
        lblServiceFee.setText(fee);
        lblAmount.setText(total);
    }

    /**
     * Show a status message
     * @param msg The message to display
     * @param isError Whether this is an error message (red) or success (green)
     */
    public void showStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setVisible(true);
        lblStatus.setStyle(isError ?
                "-fx-background-color: #ff2525; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 3;" :
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 3;");
    }

    /**
     * Get the selected payment method
     */
    public String getSelectedMethod() {
        return comboPaymentMethod.getValue();
    }

    /**
     * Get the transaction ID
     */
    public String getTransactionId() {
        return txtTransactionId.getText();
    }

    /**
     * Set the transaction ID
     */
    public void setTransactionId(String txnId) {
        txtTransactionId.setText(txnId);
    }

    /**
     * Set whether the transaction field is editable
     */
    public void setTransactionFieldEditable(boolean editable) {
        txtTransactionId.setDisable(!editable);
    }

    /**
     * Set the prompt text for the transaction field
     */
    public void setTransactionFieldPrompt(String prompt) {
        txtTransactionId.setPromptText(prompt);
    }

    /**
     * Set the confirm button enabled/disabled state
     */
    public void setConfirmButtonEnabled(boolean enabled) {
        btnConfirmPayment.setDisable(!enabled);
    }

    /**
     * Set the confirm button visible/invisible
     */
    public void setConfirmButtonVisible(boolean visible) {
        btnConfirmPayment.setVisible(visible);
    }

    /**
     * Change the cancel button text
     */
    public void setCancelButtonText(String text) {
        btnCancel.setText(text);
    }

    /**
     * Set the confirm button action
     */
    public void setConfirmButtonAction(Runnable action) {
        if (btnConfirmPayment != null) {
            btnConfirmPayment.setOnAction(e -> action.run());
        }
    }

    /**
     * Set the cancel button action
     */
    public void setCancelButtonAction(Runnable action) {
        if (btnCancel != null) {
            btnCancel.setOnAction(e -> action.run());
        }
    }

    /**
     * Set the payment method change action
     */
    public void setPaymentMethodChangeAction(Runnable action) {
        if (comboPaymentMethod != null) {
            comboPaymentMethod.setOnAction(e -> action.run());
        }
    }

    /**
     * Close the current window
     */
    public void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Run code on the JavaFX UI thread
     */
    public void runOnUIThread(Runnable action) {
        Platform.runLater(action);
    }

    // ===== GETTERS (for backwards compatibility) =====

    public Button getConfirmButton() {
        return btnConfirmPayment;
    }

    public Button getCancelButton() {
        return btnCancel;
    }

    public ComboBox<String> getPaymentMethodCombo() {
        return comboPaymentMethod;
    }

    public TextField getTransactionField() {
        return txtTransactionId;
    }
}