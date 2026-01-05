package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.MovieNotFoundException;

public class AdminBookingModel extends AdminParentModel {

    public AdminBookingModel(int adminId, String sessionToken) {
        this.adminId = adminId;
        this.sessionToken = sessionToken;
        System.out.println("✅ AdminBookingModel initialized with adminId: " + adminId);
    }

    //Get All Bookings with Details (includes customer name)
    public BookingDetails[] getAllBookings(int adminId) throws AuthenticationException {
        return adminServices.getAllBookingsWithDetails(adminId, sessionToken);
    }

    //Get Bookings by Movie - Note: This still returns Booking[], so we'll need to convert or use a different approach
    // For now, we'll filter the details list client-side
    public BookingDetails[] getBookingsByMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        BookingDetails[] allBookings = getAllBookings(adminId);
        if (allBookings == null) return new BookingDetails[0];
        
        java.util.List<BookingDetails> filtered = new java.util.ArrayList<>();
        for (BookingDetails bd : allBookings) {
            if (bd.movieId == movieId) {
                filtered.add(bd);
            }
        }
        return filtered.toArray(new BookingDetails[0]);
    }

    // Get All Movies
    public Movie[] getAllMovies(int adminId) throws AuthenticationException {
        return adminServices.getAllMovies(adminId, sessionToken);
    }
}