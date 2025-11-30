package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import MovieBookingClient.ClientServicePOA;
import MovieBookingCommon.AuthenticationException;
import MovieBookingCommon.Booking;
import MovieBookingCommon.BookingException;
import MovieBookingCommon.Customer;
import MovieBookingCommon.InvalidInputException;
import MovieBookingCommon.Movie;
import MovieBookingCommon.MovieNotFoundException;
import MovieBookingCommon.Schedule;
import MovieBookingCommon.ScheduleNotFoundException;
import MovieBookingCommon.Seat;
import MovieBookingCommon.SeatNotAvailableException;

public class MBClientServant extends ClientServicePOA {
    @Override
    public int registerCustomer(String username, String email, String password, String phoneNumber, String fullName) throws InvalidInputException {
        return 0;
    }

    @Override
    public Customer loginCustomer(String username, String password) throws AuthenticationException {
        return null;
    }

    @Override
    public void logoutCustomer(int customerId) {

    }

    @Override
    public void updateCustomerProfile(int customerId, String phoneNumber, String fullName) throws AuthenticationException {

    }

    @Override
    public void changePassword(int customerId, String oldPassword, String newPassword) throws AuthenticationException {

    }

    @Override
    public Movie[] browseMovies() {
        return new Movie[0];
    }

    @Override
    public Movie[] searchMovies(String searchString) {
        return new Movie[0];
    }

    @Override
    public Movie getMovieDetails(int movieId) throws MovieBookingCommon.MovieNotFoundException {
        return null;
    }

    @Override
    public Movie[] filterByGenres(String[] genres) {
        return new Movie[0];
    }

    @Override
    public Movie[] filterByRating(int minRating) {
        return new Movie[0];
    }

    @Override
    public Movie[] getMoviesByDate(int dayNumber, int monthNumber, int yearNumber) {
        return new Movie[0];
    }

    @Override
    public Schedule[] getMovieSchedules(int movieId) throws MovieNotFoundException {
        return new Schedule[0];
    }

    @Override
    public Seat[] getAvailableSeats(int scheduleId) throws ScheduleNotFoundException {
        return new Seat[0];
    }

    @Override
    public int createBooking(int customerId, int scheduleId, int[] seatIds) throws AuthenticationException, SeatNotAvailableException, BookingException {
        return 0;
    }

    @Override
    public Booking getBookingDetails(int bookingId) throws BookingException {
        return null;
    }

    @Override
    public Booking[] getCustomerBookings(int customerId) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public void cancelBooking(int customerId, int bookingId) throws AuthenticationException, BookingException {

    }

    @Override
    public void confirmPayment(int customerId, int bookingId) throws AuthenticationException, BookingException {

    }

    @Override
    public Booking[] getBookingHistory(int customerId) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public Booking[] getActiveBookings(int customerId) throws AuthenticationException {
        return new Booking[0];
    }
}
