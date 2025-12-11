package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomerBookingsModel extends CustomerParentModel {
    private final int customerId;
    private final String customerName;

    public CustomerBookingsModel(int customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public List<BookingDetails> fetchHistory() throws AuthenticationException {
        // Calls your existing server method
        BookingDetails[] results = clientServices.getCustomerBookingsWithDetails(customerId);
        return Arrays.asList(results);
    }

    public boolean isUpcoming(BookingDetails item) {
        try {
            // JDK 8 LocalDateTime parsing
            String dateTimeStr = item.screeningDate + " " + item.screeningTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime showTime = LocalDateTime.parse(dateTimeStr, formatter);
            return showTime.isAfter(LocalDateTime.now());
        } catch (Exception e) {
            return true;
        }
    }

    public List<BookingDetails> filter(List<BookingDetails> all, String timeFilter, String statusFilter) {
        List<BookingDetails> filtered = new ArrayList<>();
        for (BookingDetails b : all) {
            boolean matchesTime = true;
            boolean upcoming = isUpcoming(b);

            if ("Upcoming".equals(timeFilter)) matchesTime = upcoming;
            else if ("Watched (Past Date)".equals(timeFilter)) matchesTime = !upcoming;

            boolean matchesStatus = true;
            if (statusFilter != null && !"All Statuses".equals(statusFilter)) {
                matchesStatus = b.bookingStatus.equalsIgnoreCase(statusFilter);
            }

            if (matchesTime && matchesStatus) filtered.add(b);
        }
        return filtered;
    }

    public String getCustomerName() { return customerName; }
}