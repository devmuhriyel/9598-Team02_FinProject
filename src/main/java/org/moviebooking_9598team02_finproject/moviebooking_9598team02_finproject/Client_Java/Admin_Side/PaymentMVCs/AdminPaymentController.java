package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentDetails;

public class AdminPaymentController {

    private AdminPaymentView view;
    private AdminPaymentModel model;
    private int currentAdminId;
    private FilteredList<PaymentDetails> filteredData;

    private ObservableList<PaymentDetails> paymentList = FXCollections.observableArrayList();

    public AdminPaymentController(AdminPaymentView view, AdminPaymentModel model, int adminId) {
        this.view = view;
        this.model = model;
        this.currentAdminId = adminId;

        initializeTable();
        initializeButtons();
        loadData();
    }

    private void initializeTable() {
        // Add null checks to prevent errors
        if (view.getColCustomerName() != null) {
            view.getColCustomerName().setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue() != null && cellData.getValue().customerName != null ? 
                            cellData.getValue().customerName : "N/A"));
        }
        if (view.getColAmount() != null) {
            view.getColAmount().setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue() != null ? cellData.getValue().amount : 0.0));
        }
        if (view.getColMethod() != null) {
            view.getColMethod().setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue() != null && cellData.getValue().paymentMethod != null ? 
                            cellData.getValue().paymentMethod : ""));
        }
        if (view.getColTxnId() != null) {
            view.getColTxnId().setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue() != null && cellData.getValue().transactionId != null ? 
                            cellData.getValue().transactionId : ""));
        }
        if (view.getColDate() != null) {
            view.getColDate().setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue() != null && cellData.getValue().paymentDate != null ? 
                            cellData.getValue().paymentDate : ""));
        }
        if (view.getColStatus() != null) {
            view.getColStatus().setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue() != null && cellData.getValue().paymentStatus != null ? 
                            cellData.getValue().paymentStatus : ""));
        }
        
        filteredData = new FilteredList<>(paymentList, p -> true);
        SortedList<PaymentDetails> sortedData = new SortedList<>(filteredData);
        if (view.getPaymentTable() != null) {
            sortedData.comparatorProperty().bind(view.getPaymentTable().comparatorProperty());
            view.getPaymentTable().setItems(sortedData);
        }
    }

    private void initializeButtons() {
        if (view.getBtnRefresh() != null) {
            view.getBtnRefresh().setOnAction(e -> loadData());
        }
        if (view.getBtnMarkReceived() != null) {
            view.getBtnMarkReceived().setOnAction(e -> handleUpdateStatus("completed"));
        }
        if (view.getBtnMarkFailed() != null) {
            view.getBtnMarkFailed().setOnAction(e -> handleUpdateStatus("failed"));
        }

        if (view.getBtnSearch() != null) {
            view.getBtnSearch().setOnAction(e -> handleSearch());
        }
        if (view.getBtnClearSearch() != null) {
            view.getBtnClearSearch().setOnAction(e -> clearSearch());
        }

        if (view.getSearchField() != null) {
            view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch();
            });
        }
    }

    private void loadData() {
        new Thread(() -> {
            try {
                PaymentDetails[] data = model.getAllPayments(currentAdminId);
                Platform.runLater(() -> {
                    paymentList.clear();
                    if (data != null) {
                        for (PaymentDetails pd : data) {
                            if (pd != null) {
                                paymentList.add(pd);
                            }
                        }
                    }
                    view.setStatus("Data loaded successfully.", false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> view.setStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    private void handleUpdateStatus(String newStatus) {
        PaymentDetails selected = view.getSelectedPayment();
        if (selected == null) {
            view.setStatus("Please select a transaction first.", true);
            return;
        }

        try {
            model.updatePaymentStatus(currentAdminId, selected.paymentId, newStatus);
            view.setStatus("Transaction for " + (selected.customerName != null ? selected.customerName : "customer") + 
                    " marked as " + newStatus, false);
            loadData();
        } catch (Exception e) {
            view.setStatus("Update failed: " + e.getMessage(), true);
        }
    }

    private void handleSearch() {
        if (filteredData == null) return; // Safety check
        
        String filter = view.getSearchField() != null ? view.getSearchField().getText().toLowerCase().trim() : "";

        filteredData.setPredicate(payment -> {
            if (payment == null) return false;
            
            if (filter == null || filter.isEmpty()) {
                return true;
            }

            // Search by customer name
            if (payment.customerName != null && payment.customerName.toLowerCase().contains(filter)) {
                return true;
            }

            // Search by transaction ID
            if (payment.transactionId != null && payment.transactionId.toLowerCase().contains(filter)) {
                return true;
            }

            // Search by payment method
            if (payment.paymentMethod != null && payment.paymentMethod.toLowerCase().contains(filter)) {
                return true;
            }

            return false;
        });
    }

    private void clearSearch() {
        if (view.getSearchField() != null) {
            view.getSearchField().setText("");
            handleSearch();
        }
    }
}