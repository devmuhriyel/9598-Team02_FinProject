package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Payment;

public class AdminPaymentView {

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> colId, colBookingId;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, String> colMethod, colTxnId, colDate, colStatus;

    @FXML private Button btnMarkReceived;
    @FXML private Button btnMarkFailed;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatus;

    public void setPaymentList(ObservableList<Payment> list) {
        paymentTable.setItems(list);
    }

    public void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setStyle(isError ? "-fx-text-fill: #ff2525;" : "-fx-text-fill: #28a745;");
    }

    public Payment getSelectedPayment() {
        return paymentTable.getSelectionModel().getSelectedItem();
    }

    public Button getBtnMarkReceived() { return btnMarkReceived; }
    public Button getBtnMarkFailed() { return btnMarkFailed; }
    public Button getBtnRefresh() { return btnRefresh; }

    public TableView<Payment> getPaymentTable() { return paymentTable; }

    public TableColumn<Payment, Integer> getColId() { return colId; }
    public TableColumn<Payment, Integer> getColBookingId() { return colBookingId; }
    public TableColumn<Payment, Double> getColAmount() { return colAmount; }
    public TableColumn<Payment, String> getColMethod() { return colMethod; }
    public TableColumn<Payment, String> getColTxnId() { return colTxnId; }
    public TableColumn<Payment, String> getColDate() { return colDate; }
    public TableColumn<Payment, String> getColStatus() { return colStatus; }
}