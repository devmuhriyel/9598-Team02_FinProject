package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentDetails;

public class AdminPaymentView {

    @FXML private TableView<PaymentDetails> paymentTable;
    @FXML private TableColumn<PaymentDetails, String> colCustomerName;
    @FXML private TableColumn<PaymentDetails, Double> colAmount;
    @FXML private TableColumn<PaymentDetails, String> colMethod, colTxnId, colDate, colStatus;

    @FXML private Button btnMarkReceived;
    @FXML private Button btnMarkFailed;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatus;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnClearSearch;

    public void setPaymentList(ObservableList<PaymentDetails> list) {
        paymentTable.setItems(list);
    }

    public void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setStyle(isError ? "-fx-text-fill: #ff2525;" : "-fx-text-fill: #28a745;");
    }

    public PaymentDetails getSelectedPayment() {
        return paymentTable.getSelectionModel().getSelectedItem();
    }

    public Button getBtnMarkReceived() { return btnMarkReceived; }
    public Button getBtnMarkFailed() { return btnMarkFailed; }
    public Button getBtnRefresh() { return btnRefresh; }

    public TableView<PaymentDetails> getPaymentTable() { return paymentTable; }

    public TableColumn<PaymentDetails, String> getColCustomerName() { return colCustomerName; }
    public TableColumn<PaymentDetails, Double> getColAmount() { return colAmount; }
    public TableColumn<PaymentDetails, String> getColMethod() { return colMethod; }
    public TableColumn<PaymentDetails, String> getColTxnId() { return colTxnId; }
    public TableColumn<PaymentDetails, String> getColDate() { return colDate; }
    public TableColumn<PaymentDetails, String> getColStatus() { return colStatus; }
    public TextField getSearchField() { return searchField; }
    public Button getBtnSearch() { return btnSearch; }
    public Button getBtnClearSearch() { return btnClearSearch; }
}