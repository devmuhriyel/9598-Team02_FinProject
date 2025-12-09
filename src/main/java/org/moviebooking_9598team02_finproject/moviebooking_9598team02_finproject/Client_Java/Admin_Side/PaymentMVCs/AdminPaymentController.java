package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Payment;

public class AdminPaymentController {

    private AdminPaymentView view;
    private AdminPaymentModel model;
    private int currentAdminId;
    private FilteredList<Payment> filteredData;

    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    public AdminPaymentController(AdminPaymentView view, AdminPaymentModel model, int adminId) {
        this.view = view;
        this.model = model;
        this.currentAdminId = adminId;

        initializeTable();
        initializeButtons();
        loadData();
    }

    private void initializeTable() {
        view.getColId().setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().paymentId));
        view.getColBookingId().setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().bookingId));
        view.getColAmount().setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().amount));
        view.getColMethod().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().paymentMethod));
        view.getColTxnId().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().transactionId));
        view.getColDate().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().paymentDate));
        view.getColStatus().setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().paymentStatus));
        view.setPaymentList(paymentList);
        filteredData = new FilteredList<>(paymentList, p -> true);
        view.getPaymentTable().setItems(filteredData);
    }

    private void initializeButtons() {
        view.getBtnRefresh().setOnAction(e -> loadData());
        view.getBtnMarkReceived().setOnAction(e -> handleUpdateStatus("completed"));
        view.getBtnMarkFailed().setOnAction(e -> handleUpdateStatus("failed"));

        view.getBtnSearch().setOnAction(e -> handleSearch());
        view.getBtnClearSearch().setOnAction(e -> clearSearch());

        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    private void loadData() {
        try {
            Payment[] data = model.getAllPayments(currentAdminId);
            paymentList.clear();
            if (data != null) {
                paymentList.addAll(data);
            }
            view.setStatus("Data loaded successfully.", false);
        } catch (Exception e) {
            e.printStackTrace();
            view.setStatus("Error: " + e.getMessage(), true);
        }
    }

    private void handleUpdateStatus(String newStatus) {
        Payment selected = view.getSelectedPayment();
        if (selected == null) {
            view.setStatus("Please select a transaction first.", true);
            return;
        }

        try {
            model.updatePaymentStatus(currentAdminId, selected.paymentId, newStatus);
            view.setStatus("Transaction " + selected.paymentId + " marked as " + newStatus, false);
            loadData();
        } catch (Exception e) {
            view.setStatus("Update failed: " + e.getMessage(), true);
        }
    }

    private void handleSearch() {
        String filter = view.getSearchField().getText().toLowerCase().trim();

        filteredData.setPredicate(payment -> {
            if (filter == null || filter.isEmpty()) {
                return true;
            }

            if (String.valueOf(payment.bookingId).contains(filter)) {
                return true;
            }

            if (payment.transactionId != null && payment.transactionId.toLowerCase().contains(filter)) {
                return true;
            }

            if (payment.paymentMethod != null && payment.paymentMethod.toLowerCase().contains(filter)) {
                return true;
            }

            return false;
        });
    }

    private void clearSearch() {
        view.getSearchField().setText("");
        handleSearch();
    }
}