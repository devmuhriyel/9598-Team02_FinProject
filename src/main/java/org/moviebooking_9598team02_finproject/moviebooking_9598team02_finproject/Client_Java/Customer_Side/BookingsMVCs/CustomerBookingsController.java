package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import java.util.List;

public class CustomerBookingsController {
    private final CustomerBookingsView view;
    private final CustomerBookingsModel model;
    private List<BookingDetails> allBookings;

    public CustomerBookingsController(CustomerBookingsView view, CustomerBookingsModel model) {
        this.view = view;
        this.model = model;

        // Wire events via the view's abstraction methods
        this.view.setOnRefresh(this::loadData);
        this.view.setOnFilterChange(this::applyFilters);

        loadData();
    }

    private void loadData() {
        view.setStatus("Fetching bookings...", false);
        new Thread(() -> {
            try {
                allBookings = model.fetchHistory();
                view.runOnUIThread(this::applyFilters);
            } catch (Exception e) {
                view.runOnUIThread(() -> view.setStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    private void applyFilters() {
        if (allBookings == null) return;

        List<BookingDetails> filtered = model.filter(allBookings, view.getWatchedFilter(), view.getStatusFilter());

        view.clearList();
        for (BookingDetails b : filtered) {
            // Delegate creation to View to keep Controller "Strict" (No JFX imports)
            view.createAndAddItem(b, this, model);
        }
        view.setStatus("Showing " + filtered.size() + " bookings.", false);
    }

    public void requestTicketDetails(BookingDetails item) {
        // Delegate modal launch to View
        view.launchTicketModal(item, model.getCustomerName());
    }
}