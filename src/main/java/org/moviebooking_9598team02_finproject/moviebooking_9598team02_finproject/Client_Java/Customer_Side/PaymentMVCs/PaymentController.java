package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

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
        view.setConfirmButtonAction(() -> handleConfirm());
        view.setCancelButtonAction(() -> handleCancel());
        view.setPaymentMethodChangeAction(() -> handleMethodChange());
    }

    public void setPaymentData(int custId, int bookId, double amount) {
        this.customerId = custId;
        this.bookingId = bookId;
        this.totalAmount = amount;
    }

    private void handleMethodChange() {
        String method = view.getSelectedMethod();
        if (method == null) return;

        if (method.equals("cash") || method.equals("online")) {
            // Generate fake transaction ID for cash/online payments
            String prefix = (method.equals("cash") ? "CASH-" : "GW-");
            String fakeID = prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            view.setTransactionId(fakeID);
            view.setTransactionFieldEditable(false);
        } else {
            // Credit Card - user must enter card number
            view.setTransactionId("");
            view.setTransactionFieldPrompt("Enter 16-digit Card Number");
            view.setTransactionFieldEditable(true);
        }
    }

    private void handleConfirm() {
        String method = view.getSelectedMethod();
        String txnId = view.getTransactionId();

        // Validation
        if (method == null || txnId == null || txnId.trim().isEmpty()) {
            view.showStatus("Please select a payment method.", true);
            return;
        }

        // Disable UI to prevent double-clicks
        view.setConfirmButtonEnabled(false);
        view.showStatus("Processing Payment...", false);

        // Run Database Call in background thread to prevent UI freeze
        new Thread(() -> {
            try {
                // CORBA call to process payment
                int paymentId = model.processPayment(customerId, bookingId, totalAmount, method, txnId);

                // If success, update UI on JavaFX thread
                view.runOnUIThread(() -> {
                    view.showStatus("SUCCESS! Payment ID: " + paymentId + " (Status: PENDING)", false);
                    view.setCancelButtonText("Close Receipt");
                    view.setConfirmButtonVisible(false);

                    // Auto close after 3 seconds
                    startAutoClose(3000);
                });

            } catch (Exception e) {
                // Failure handler
                view.runOnUIThread(() -> {
                    e.printStackTrace();
                    view.showStatus("Transaction Failed: " + e.getMessage(), true);
                    view.setConfirmButtonEnabled(true);
                });
            }
        }).start();
    }

    private void handleCancel() {
        view.closeWindow();
    }

    private void startAutoClose(long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                view.runOnUIThread(() -> view.closeWindow());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}