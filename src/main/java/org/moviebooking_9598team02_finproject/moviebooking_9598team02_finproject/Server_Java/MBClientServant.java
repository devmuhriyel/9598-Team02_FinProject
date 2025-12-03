package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.sql.Connection;

public class MBClientServant extends ClientServicePOA {

    // ===== DATABASE VARIABLES =====
    private Connection conn = null;

    // ===== AUTHENTICATION METHODS =====
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

    // ===== ACCOUNT MANAGEMENT METHODS =====
    @Override
    public void updateCustomerProfile(int customerId, String phoneNumber, String fullName) throws AuthenticationException {

    }

    @Override
    public void changePassword(int customerId, String oldPassword, String newPassword) throws AuthenticationException {

    }

    @Override
    public Genre[] browseGenres() {
        return new Genre[0];
    }

    @Override
    public Genre getGenreDetails(int genreId) {
        return null;
    }

    @Override
    public Genre[] getMovieGenres(int movieId) throws MovieNotFoundException {
        return new Genre[0];
    }

    // ===== HOME PAGE METHODS =====
    @Override
    public Movie[] browseMovies() {
        return new Movie[0];
    }

    @Override
    public Movie[] searchMovies(String searchString) {
        return new Movie[0];
    }

    @Override
    public Movie getMovieDetails(int movieId) throws MovieNotFoundException {
        return null;
    }

    @Override
    public Movie[] filterByGenreIds(int[] genreIds) {
        return new Movie[0];
    }

    @Override
    public Movie[] filterByGenreNames(String[] genreNames) {
        return new Movie[0];
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


    // ===== SEAT BOOKING METHODS
    @Override
    public Seat[] getAvailableSeats(int scheduleId) throws ScheduleNotFoundException {
        return new Seat[0];
    }

    @Override
    public ScheduleDetails getScheduleDetailsView(int scheduleId) throws ScheduleNotFoundException {
        return null;
    }

    @Override
    public ScheduleDetails[] getAllSchedulesWithDetails() {
        return new ScheduleDetails[0];
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
    public void updateBooking(int customerId, int bookingId, int[] newSeatIds) throws AuthenticationException, BookingException, SeatNotAvailableException {

    }

    @Override
    public void updateBookingStatus(int customerId, int bookingId, String status) throws AuthenticationException, BookingException {

    }

    @Override
    public Booking[] getBookingHistory(int customerId) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public Booking[] getActiveBookings(int customerId) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public BookingDetails getBookingDetailsView(int customerId, int bookingId) throws AuthenticationException, BookingException {
        return null;
    }

    @Override
    public BookingDetails[] getCustomerBookingsWithDetails(int customerId) throws AuthenticationException {
        return new BookingDetails[0];
    }

    @Override
    public int processPayment(int customerId, int bookingId, double amount, String paymentMethod, String transactionId) throws AuthenticationException, BookingException, PaymentException {
        return 0;
    }

    @Override
    public Payment getPaymentDetails(int customerId, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        return null;
    }

    @Override
    public Payment[] getCustomerPayments(int customerId) throws AuthenticationException {
        return new Payment[0];
    }

    @Override
    public Payment getBookingPayment(int customerId, int bookingId) throws AuthenticationException, BookingException {
        return null;
    }

    @Override
    public PaymentDetails getPaymentDetailsView(int customerId, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        return null;
    }

    @Override
    public PaymentDetails[] getCustomerPaymentsWithDetails(int customerId) throws AuthenticationException {
        return new PaymentDetails[0];
    }
}
