package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Callbacks;


import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientCallbackPOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Booking;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

public class ClientCallbackImplementation extends ClientCallbackPOA {
    @Override
    public void onBookingConfirmed(Booking booking) {

    }

    @Override
    public void onBookingCancelled(int bookingId, String reason) {

    }

    @Override
    public void onPaymentProcessed(int bookingId, double amount) {

    }

    @Override
    public void onSeatAvailabilityChanged(int scheduleId, int availableSeats) {

    }

    @Override
    public void onNewMovieAdded(Movie movie) {

    }
}
