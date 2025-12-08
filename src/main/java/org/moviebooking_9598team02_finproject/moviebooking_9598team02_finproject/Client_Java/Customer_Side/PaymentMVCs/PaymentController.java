package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PaymentController {
    private PaymentView view;
    private PaymentModel model;

    private int customerId;
    private int bookingId;
    private double totalAmount;

    public PaymentController(PaymentView view, PaymentModel model) {
        this.view = view;
        this.model = model;
        initializeListeners();
    }

    private void initializeListeners() {
        view.getConfirmButton().setOnAction(event -> handleConfirm());
        view.getCancelButton().setOnAction(event -> closeWindow());
        view.getPaymentMethodCombo().setOnAction(event -> handleMethodChange());
    }

    public void setPaymentData(int custId, int bookId, double amount) {
        this.customerId = custId;
        this.bookingId = bookId;
        this.totalAmount = amount;
    }

    // for payment stuffs
    private void handleMethodChange() {
        String method = view.getSelectedMethod();
        if (method == null) return;

        if (method.equals("cash") || method.equals("online")) {
            //mag gegenerate ng fake id
            String prefix = (method.equals("cash") ? "CASH-" : "GW-");
            String fakeID = prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            view.getTransactionField().setText(fakeID);
            view.getTransactionField().setDisable(true);
        } else {
            // Credit Card
            view.getTransactionField().setText("");
            view.getTransactionField().setPromptText("Enter 16-digit Card Number");
            view.getTransactionField().setDisable(false);
        }
    }

    // Confirm payment
    private void handleConfirm() {
        String method = view.getSelectedMethod();
        String txnId = view.getTransactionId();

        // Validation
        if (method == null || txnId == null || txnId.trim().isEmpty()) {
            view.setStatus("Please select a method.", true);
            return;
        }

        //Disable UI to prevent double-clicks
        view.getConfirmButton().setDisable(true);
        view.setStatus("Processing Payment...", false);

        // Run Database Call bg thread para di mag freeze ui
        new Thread(() -> {
            try {
                // COBRAA
                int paymentId = model.processPayment(customerId, bookingId, totalAmount, method, txnId);

                // If success
                Platform.runLater(() -> {
                    view.setStatus("SUCCESS! Receipt #: " + paymentId, false);
                    view.getCancelButton().setText("Close Receipt");
                    view.getConfirmButton().setVisible(false); // Hide pay button

                    // auto close ng 2 sec
                    startCloseTimer();
                });

            } catch (Exception e) {
                // Failure handler
                Platform.runLater(() -> {
                    e.printStackTrace();
                    view.setStatus("Transaction Failed: " + e.getMessage(), true);
                    view.getConfirmButton().setDisable(false);
                });
            }
        }).start();
    }

    // Closeeee
    private void closeWindow() {
        // Get the stage (window) from one of the UI elements and close it
        Stage stage = (Stage) view.getCancelButton().getScene().getWindow();
        stage.close();
    }

    // Closing set to 2 sec
    private void startCloseTimer() {
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> closeWindow());
            }
        }, 2000);
    }
}