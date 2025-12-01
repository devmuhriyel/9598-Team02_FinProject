package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Booking;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.MovieNotFoundException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Schedule;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.ScheduleNotFoundException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;
import org.omg.CORBA.ORB;

public class MBAdminServant extends AdminServicePOA {

    //CORBA SETUP AND METHODS
    private ORB orb;

    public void setOrb(ORB orb_value) {
        this.orb = orb_value;
    }

    public void shutdown(){
        orb.shutdown(false);
    }

    @Override
    public Admin loginAdmin(String username, String password) throws AuthenticationException {
        return null;
    }

    @Override
    public void logoutAdmin(int adminId) {

    }

    @Override
    public int addMovie(int adminId, String movieTitle, String[] movieGenres, String movieDescription, int movieRating, String moviePosterURL) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public void updateMovieTitle(int adminId, int movieId, String newTitle) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void updateMovieGenres(int adminId, int movieId, String[] newGenres) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void updateMovieDescription(int adminId, int movieId, String newDescription) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void updateMovieRating(int adminId, int movieId, int newRating) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void updateMoviePoster(int adminId, int movieId, String newPosterURL) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void deleteMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public Movie[] getAllMovies(int adminId) throws AuthenticationException {
        return new Movie[0];
    }

    @Override
    public int createMovieSchedule(int adminId, String screeningDate, String screeningTime) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public int assignMovieToSchedule(int adminId, int movieId, int movieScheduleId) throws AuthenticationException, MovieNotFoundException, ScheduleNotFoundException {
        return 0;
    }

    @Override
    public void updateScheduleDateTime(int adminId, int scheduleId, String newDate, String newTime) throws AuthenticationException, ScheduleNotFoundException {

    }

    @Override
    public void deleteSchedule(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {

    }

    @Override
    public Schedule[] getAllSchedules(int adminId) throws AuthenticationException {
        return new Schedule[0];
    }

    @Override
    public Schedule[] getSchedulesByMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return new Schedule[0];
    }

    @Override
    public int addSeat(int adminId, String seatRow, int seatNumber, String seatType, double seatPrice) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public void updateSeatPrice(int adminId, int seatId, double newPrice) throws AuthenticationException {

    }

    @Override
    public void updateSeatType(int adminId, int seatId, String newType) throws AuthenticationException {

    }

    @Override
    public void deleteSeat(int adminId, int seatId) throws AuthenticationException {

    }

    @Override
    public Seat[] getAllSeats(int adminId) throws AuthenticationException {
        return new Seat[0];
    }

    @Override
    public Booking[] getAllBookings(int adminId) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public Booking[] getBookingsByMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return new Booking[0];
    }

    @Override
    public Booking[] getBookingsBySchedule(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return new Booking[0];
    }

    @Override
    public Booking[] getBookingsByStatus(int adminId, String status) throws AuthenticationException {
        return new Booking[0];
    }

    @Override
    public void cancelBooking(int adminId, int bookingId, String reason) throws AuthenticationException, BookingException {

    }

    @Override
    public Customer[] getAllCustomers(int adminId) throws AuthenticationException {
        return new Customer[0];
    }

    @Override
    public Customer[] searchCustomers(int adminId, String searchString) throws AuthenticationException {
        return new Customer[0];
    }

    @Override
    public Customer getCustomerDetails(int adminId, int customerId) throws AuthenticationException {
        return null;
    }

    @Override
    public String generateSalesReport(int adminId, String startDate, String endDate) throws AuthenticationException {
        return "";
    }

    @Override
    public double getMovieRevenue(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return 0;
    }

    @Override
    public double getScheduleOccupancy(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return 0;
    }

    @Override
    public Movie[] getPopularMovies(int adminId, int top) throws AuthenticationException {
        return new Movie[0];
    }
}
