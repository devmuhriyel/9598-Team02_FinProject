package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;

public class CustomerTicketViewController {
    private final CustomerTicketView view;

    public CustomerTicketViewController(CustomerTicketView view) {
        this.view = view;
        this.view.setOnClose(this.view::closeWindow);
    }

    public void setTicketData(BookingDetails b, String name) {
        view.setBookingDetails(b.bookingId, name);
        view.setMovieInfo(b.movieTitle, b.screeningDate, b.screeningTime, "Screen: " + b.scheduleId, b.seats);

        double total = b.totalPrice;
        double sub = total / 1.05;
        double fee = total - sub;

        view.setFinancials(
                String.format("P %.2f", sub), // Price per ticket (Estimated)
                "1",                          // Count
                String.format("P %.2f", sub), // Subtotal
                String.format("P %.2f", fee), // Fee
                String.format("P %.2f", total)// Total
        );

        view.setPaymentInfo("PAID", "Online Payment", "TXN-" + b.bookingId);

        String col = "confirmed".equalsIgnoreCase(b.bookingStatus) ? "#4CAF50" : "#FFD700";
        view.setBookingStatusText(b.bookingStatus, col);
        view.setPaymentStatusText("PAID", "#4CAF50");
    }
}