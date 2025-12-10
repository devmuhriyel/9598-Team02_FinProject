package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Booking;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.MovieNotFoundException;

public class AdminBookingModel extends AdminParentModel {

    //Get All Bookings
    public Booking[] getAllBookings(int adminId) throws AuthenticationException {
        return adminServices.getAllBookings(adminId);
    }

    //Get Bookings by Movie
    public Booking[] getBookingsByMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return adminServices.getBookingsByMovie(adminId, movieId);
    }

    // Get All Movies
    public Movie[] getAllMovies(int adminId) throws AuthenticationException {
        return adminServices.getAllMovies(adminId);
    }
}