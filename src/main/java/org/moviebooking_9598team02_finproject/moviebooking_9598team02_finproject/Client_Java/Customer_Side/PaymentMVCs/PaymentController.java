package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

import java.util.UUID;

public class PaymentController {
    private PaymentView view;
    private PaymentModel model;

    private int customerId;
    private int bookingId;
    private double totalAmount;

    // 🔥 NEW: Track payment state
    private boolean paymentSubmitted = false;

    public PaymentController(PaymentView view, PaymentModel model) {
        this.view = view;
        this.model = model;
        initializeListeners();
    }

    private void initializeListeners() {
        view.setConfirmButtonAction(this::handleConfirm);
        view.setCancelButtonAction(this::handleCancel);
        view.setPaymentMethodChangeAction(this::handleMethodChange);
    }

    public void setPaymentData(int custId, String custName, int bookId, double amount) {
        this.customerId = custId;
        this.bookingId = bookId;
        this.totalAmount = amount;

        view.setCustomerDetails(custName, String.valueOf(bookId));
        view.setFinancials(amount);
    }

    private void handleMethodChange() {
        String method = view.getSelectedMethod();
        if (method == null) return;

        if (method.equals("cash") || method.equals("online")) {
            String prefix = (method.equals("cash") ? "CASH-" : "GW-");
            String fakeID = prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            view.setTransactionId(fakeID);
            view.setTransactionFieldEditable(false);
        } else {
            view.setTransactionId("");
            view.setTransactionFieldPrompt("Enter 16-digit Card Number");
            view.setTransactionFieldEditable(true);
        }
    }

    private void handleConfirm() {
        String method = view.getSelectedMethod();
        String txnId = view.getTransactionId();

        if (method == null || txnId == null || txnId.trim().isEmpty()) {
            view.showStatus("Please select a payment method.", true);
            return;
        }

        view.setConfirmButtonEnabled(false);
        view.showStatus("Processing Payment...", false);

        new Thread(() -> {
            try {
                int paymentId = model.processPayment(customerId, model.getSessionToken(), bookingId, totalAmount, method, txnId);

                // 🔥 FIX: Mark payment as submitted
                paymentSubmitted = true;

                view.runOnUIThread(() -> {
                    view.showStatus("SUCCESS! Payment ID: " + paymentId + " (Status: PENDING)", false);
                    view.setCancelButtonText("Close Receipt");
                    view.setConfirmButtonVisible(false);
                    startAutoClose(3000);
                });
            } catch (Exception e) {
                view.runOnUIThread(() -> {
                    e.printStackTrace();
                    view.showStatus("Transaction Failed: " + e.getMessage(), true);
                    view.setConfirmButtonEnabled(true);
                });
            }
        }).start();
    }

    private void handleCancel() {
        // 🔥 FIX: If payment was already submitted, just close window
        if (paymentSubmitted) {
            view.closeWindow();
            return;
        }

        // Otherwise, cancel the booking
        view.setConfirmButtonEnabled(false);
        view.showStatus("Cancelling booking...", false);

        new Thread(() -> {
            try {
                model.cancelBooking(customerId, bookingId);
                view.runOnUIThread(() -> {
                    view.showStatus("Booking cancelled successfully!", false);
                    new Thread(() -> {
                        try { Thread.sleep(2000); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                        view.runOnUIThread(view::closeWindow);
                    }).start();
                });
            } catch (Exception e) {
                view.runOnUIThread(() -> {
                    e.printStackTrace();
                    view.showStatus("Failed to cancel booking: " + e.getMessage(), true);
                    view.setConfirmButtonEnabled(true);
                });
            }
        }).start();
    }

    private void startAutoClose(long delayMillis) {
        new Thread(() -> {
            try { Thread.sleep(delayMillis); view.runOnUIThread(view::closeWindow); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }).start();
    }
}