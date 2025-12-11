package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.BookingItem;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.CustomerBookingsController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.CustomerBookingsModel;

public class CustomerBookingItemController {
    private final CustomerBookingItemView view;
    private BookingDetails data;
    private CustomerBookingsController parent;

    public CustomerBookingItemController(CustomerBookingItemView view) {
        this.view = view;
    }

    public void initData(BookingDetails b, CustomerBookingsController p, CustomerBookingsModel m) {
        this.data = b;
        this.parent = p;

        // 1. Set text data
        view.setMainDetails(
                b.movieTitle,
                b.bookingDate,
                b.screeningDate + " @ " + b.screeningTime,
                b.seats,
                String.format("P %.2f", b.totalPrice)
        );

        // 2. Wire the button click to the parent controller
        view.setOnViewTicket(() -> parent.requestTicketDetails(data));

        // 3. Status Badge Logic (JDK 8 if/else)
        applyStatusColors(m.isUpcoming(b));
    }

    private void applyStatusColors(boolean isUpcoming) {
        // Timeline Badge
        if (isUpcoming) {
            view.setStatusBadge(view.getLblTimeStatus(), "UPCOMING", "#2E86C1");
        } else {
            view.setStatusBadge(view.getLblTimeStatus(), "WATCHED", "#5D6D7E");
        }

        // Booking Status Badge
        String bStatus = data.bookingStatus.toLowerCase();
        String bCol = "#888888";
        if (bStatus.equals("confirmed")) bCol = "#4CAF50";
        else if (bStatus.equals("pending")) bCol = "#FFD700";
        else if (bStatus.equals("cancelled") || bStatus.equals("failed")) bCol = "#F44336";
        view.setStatusBadge(view.getLblBookingStatus(), data.bookingStatus, bCol);

        // Payment Status Badge (Assuming paid if confirmed)
        if (bStatus.equals("confirmed")) {
            view.setStatusBadge(view.getLblPaymentStatus(), "PAID", "#4CAF50");
        } else {
            view.setStatusBadge(view.getLblPaymentStatus(), "UNPAID", "#F9A825");
        }
    }
}