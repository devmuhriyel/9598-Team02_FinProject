package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MBClientServant extends ClientServicePOA {

    // ===== DATABASE VARIABLES =====
    private Connection conn = null;

    // ===== AUTHENTICATION METHODS =====
    @Override
    public int registerCustomer(String username, String email, String password, String phoneNumber, String fullName) throws InvalidInputException {
        try (Connection connection = DatabaseAccess.getConnection()){
            int nextCustomerID = getLatestCustomerID() + 1;
            String hashedPassword = PasswordManager.hashPassword(password);
            LocalDateTime timeOfCreation = LocalDateTime.now();
            String query = "INSERT INTO customers (customer_id, username, email, password, full_name, phone_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextCustomerID);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, hashedPassword);  // Store hashed password
            statement.setString(5, fullName);
            statement.setString(6, phoneNumber);
            statement.setString(7, timeOfCreation.toString());
            statement.setString(8, timeOfCreation.toString());

            // Use executeUpdate() for INSERT statements, not executeQuery()
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully created admin: " + username + " with ID: " + nextCustomerID);
                return nextCustomerID;
            } else {
                throw new InvalidInputException("Failed to create admin", "Database insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new InvalidInputException("Account Already Exists", "Username or email already exists");
            }
            throw new InvalidInputException("Database Error", "Failed to create admin: " + e.getMessage());
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Customer loginCustomer(String username, String password) throws AuthenticationException {
        Customer customer = new Customer();
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT * FROM customers where username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                String hashedPassword = rs.getString("password");
                if(PasswordManager.checkPassword(password, hashedPassword)){
                    int customer_id = rs.getInt("customer_id");
                    SessionManager.createCustomerSession(customer_id, rs.getString("username"));
                    if (SessionManager.isCustomerLoggedIn(customer_id)) {
                        logoutCustomer(customer_id);
                        SessionManager.removeAdminSession(customer_id);
                    }
                    customer.customerId = rs.getInt("customer_id");
                    customer.username = rs.getString("username");
                    customer.email = rs.getString("email");
                    customer.password = "";
                    customer.fullName = rs.getString("full_name");
                    customer.phoneNumber = rs.getString("phone_number");
                    customer.createdAt = rs.getString("created_at");
                    customer.updatedAt = rs.getString("updated_at");
                } else {
                    throw new AuthenticationException("Incorrect password");
                }
            } else {
                throw new AuthenticationException("Account does not exist");
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new AuthenticationException("Database error:" + e.getMessage());
        }
        return customer;
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

    // ===== HELPER METHODS (NOT IN THE IDL) =====
    private int getLatestCustomerID() throws AuthenticationException {
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT MAX(customer_id) as max_id FROM customers";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                int maxID = resultSet.getInt("max_id");
                return resultSet.wasNull() ? 0 : maxID;  // Return 0 if table is empty
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
        return 0;
    }
}
