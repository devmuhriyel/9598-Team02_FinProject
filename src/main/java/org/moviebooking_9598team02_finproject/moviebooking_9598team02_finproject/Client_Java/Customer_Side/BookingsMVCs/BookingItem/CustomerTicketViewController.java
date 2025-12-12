package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;

public class CustomerTicketViewController {
    private final CustomerTicketView view;

    public CustomerTicketViewController(CustomerTicketView view) {
        this.view = view;
        this.view.setOnClose(this.view::closeWindow);
    }

    public void setTicketData(BookingDetails b, String name) {
        // 1. Set Header Info
        view.setBookingDetails(b.bookingId, name);
        view.setMovieInfo(b.movieTitle, b.screeningDate, b.screeningTime, "Screen: " + b.scheduleId, b.seats);

        // 2. Financials
        double total = b.totalPrice;
        double sub = total / 1.05; // Reverse 5% tax calculation
        double fee = total - sub;

        view.setFinancials(
                String.format("P %.2f", sub), // Price per ticket
                "1",                          // Count (Simplified)
                String.format("P %.2f", sub), // Subtotal
                String.format("P %.2f", fee), // Service Fee
                String.format("P %.2f", total)// Total Paid
        );

        // 3. Status & Payment Logic (Updated for Pending Workflow)
        String bStatus = b.bookingStatus.toLowerCase();

        if (bStatus.equals("confirmed")) {
            // GREEN: Payment accepted by Admin
            view.setBookingStatusText("CONFIRMED", "#4CAF50");
            view.setPaymentStatusText("PAYMENT VERIFIED", "#4CAF50");
            view.setPaymentInfo("Online Payment", "Completed", "TXN-" + b.bookingId);

        } else if (bStatus.equals("pending")) {
            // ORANGE: Waiting for Admin to check
            view.setBookingStatusText("WAITING FOR APPROVAL", "#F39C12");
            view.setPaymentStatusText("VERIFYING...", "#F39C12");
            view.setPaymentInfo("Pending Admin Check", "Processing", "---");

        } else {
            // RED: Cancelled or Failed
            view.setBookingStatusText(bStatus.toUpperCase(), "#E74C3C");
            view.setPaymentStatusText("FAILED", "#E74C3C");
            view.setPaymentInfo("N/A", "Void", "---");
        }
    }
}