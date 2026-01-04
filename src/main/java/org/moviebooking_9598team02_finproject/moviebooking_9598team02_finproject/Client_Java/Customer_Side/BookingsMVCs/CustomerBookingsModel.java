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
    private final String customerName;

    // ✅ FIX: Constructor that initializes session data
    public CustomerBookingsModel(int customerId, String sessionToken, String customerName) {
        // Set parent class fields
        this.customerId = customerId;
        this.sessionToken = sessionToken;
        this.username = customerName; // or leave null if not needed
        this.customerName = customerName;

        System.out.println("✅ CustomerBookingsModel initialized with customerId: " + customerId +
                ", sessionToken: " + (sessionToken != null ? sessionToken.substring(0, 8) + "..." : "NULL"));
    }

    public List<BookingDetails> fetchHistory() throws AuthenticationException {
        // ✅ Now customerId and sessionToken are properly set!
        BookingDetails[] results = clientServices.getCustomerBookingsWithDetails(customerId, sessionToken);
        return Arrays.asList(results);
    }

    // ... rest of existing methods unchanged ...

    public boolean isUpcoming(BookingDetails item) {
        try {
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
            String status = b.bookingStatus.toLowerCase();
            if ("cancelled".equals(status) || "failed".equals(status)) {
                continue;
            }

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