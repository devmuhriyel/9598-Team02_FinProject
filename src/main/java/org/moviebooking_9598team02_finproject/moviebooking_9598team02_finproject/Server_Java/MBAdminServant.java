package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.omg.CORBA.ORB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MBAdminServant extends AdminServicePOA {

    // ===== CORBA VARIABLES, METHODS, AND SETUP =====
    private ORB orb;

    public void setOrb(ORB orb_value) {
        this.orb = orb_value;
    }

    public void shutdown() throws SQLException {
        DatabaseAccess.closeConnection();
        orb.shutdown(false);
    }

    // ===== CONSTRUCTORS =====
    public MBAdminServant() {}

    // ===== AUTHENTICATION METHODS =====
    @Override
    public Admin loginAdmin(String username, String password) throws AuthenticationException {
        Admin admin = new Admin();
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT * FROM admins where username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                String hashedPassword = rs.getString("password");
                if(PasswordManager.checkPassword(password, hashedPassword)){
                    admin.adminId = rs.getInt("admin_id");
                    admin.username = rs.getString("username");
                    admin.email = rs.getString("email");
                    admin.password = "";
                    admin.fullName = rs.getString("full_name");
                    admin.phoneNumber = rs.getString("phone_number");
                    admin.createdAt = rs.getString("created_at");
                    admin.updatedAt = rs.getString("updated_at");
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
        return admin;
    }

    @Override
    public void logoutAdmin(int adminId) {
        try(Connection connection = DatabaseAccess.getConnection()){

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public int createAdmin(int creatorAdminId, String username, String email, String password, String fullName, String phoneNumber) throws AuthenticationException, InvalidInputException {
        try(Connection connection = DatabaseAccess.getConnection()){

        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int addGenre(int adminId, String genreName, String genreDescription) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public void updateGenre(int adminId, int genreId, String genreName, String genreDescription) throws AuthenticationException {

    }

    @Override
    public void deleteGenre(int adminId, int genreId) throws AuthenticationException {

    }

    @Override
    public Genre[] getAllGenres(int adminId) throws AuthenticationException {
        return new Genre[0];
    }

    @Override
    public Genre getGenreById(int adminId, int genreId) throws AuthenticationException {
        return null;
    }

    @Override
    public int addMovie(int adminId, String movieTitle, int[] genreIds, String movieDescription, int movieRating, String moviePosterURL) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    // ===== ADMIN MANAGEMENT METHODS =====
    @Override
    public Admin getAdminById(int adminId, int requestingAdminId) throws AuthenticationException {
        return null;
    }

    @Override
    public Admin[] getAllAdmins(int adminId) throws AuthenticationException {
        return new Admin[0];
    }

    @Override
    public Admin[] searchAdmins(int adminId, String searchString) throws AuthenticationException {
        return new Admin[0];
    }

    @Override
    public void updateAdminProfile(int adminId, String fullName, String phoneNumber) throws AuthenticationException {

    }

    @Override
    public void updateAdminEmail(int adminId, String newEmail, String password) throws AuthenticationException, InvalidInputException {

    }

    @Override
    public void changeAdminPassword(int adminId, String oldPassword, String newPassword) throws AuthenticationException {

    }

    @Override
    public void deactivateAdmin(int superAdminId, int targetAdminId, String reason) throws AuthenticationException {

    }

    @Override
    public void deleteAdmin(int superAdminId, int targetAdminId) throws AuthenticationException {

    }

    // ===== MOVIE METHODS =====

    @Override
    public void updateMovieTitle(int adminId, int movieId, String newTitle) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void updateMovieGenres(int adminId, int movieId, int[] newGenreIds) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void addGenreToMovie(int adminId, int movieId, int genreId) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public void removeGenreFromMovie(int adminId, int movieId, int genreId) throws AuthenticationException, MovieNotFoundException {

    }

    @Override
    public Genre[] getMovieGenres(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return new Genre[0];
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

    // ===== MOVIE SCHEDULES METHODS =====
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

    // ===== SEATS MANAGEMENT METHODS =====
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

    // ===== SEAT SCHEDULE METHODS =====
    @Override
    public int assignSeatToSchedule(int adminId, int scheduleId, int seatId, String status) throws AuthenticationException, ScheduleNotFoundException {
        return 0;
    }

    @Override
    public void initializeScheduleSeats(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {

    }

    @Override
    public void updateScheduleSeatStatus(int adminId, int scheduleListSeatId, String status) throws AuthenticationException {

    }

    @Override
    public void removeScheduleSeat(int adminId, int scheduleListSeatId) throws AuthenticationException {

    }

    @Override
    public ScheduleListSeat[] getAllScheduleSeats(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return new ScheduleListSeat[0];
    }

    // ===== BOOKINGS METHODS =====
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
    public void updateBookingAdmin(int adminId, int bookingId, int[] newSeatIds, String status) throws AuthenticationException, BookingException {

    }

    @Override
    public BookingDetails getBookingDetailsView(int adminId, int bookingId) throws AuthenticationException, BookingException {
        return null;
    }

    @Override
    public BookingDetails[] getAllBookingsWithDetails(int adminId) throws AuthenticationException {
        return new BookingDetails[0];
    }

    @Override
    public ScheduleDetails getScheduleDetailsView(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return null;
    }

    @Override
    public ScheduleDetails[] getAllSchedulesWithDetails(int adminId) throws AuthenticationException {
        return new ScheduleDetails[0];
    }

    @Override
    public PaymentDetails getPaymentDetailsView(int adminId, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        return null;
    }

    @Override
    public PaymentDetails[] getAllPaymentsWithDetails(int adminId) throws AuthenticationException {
        return new PaymentDetails[0];
    }

    // ===== CUSTOMER MANAGEMENT METHODS =====
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
    public void updateCustomerInfo(int adminId, int customerId, String email, String phoneNumber, String fullName) throws AuthenticationException {

    }

    @Override
    public void deactivateCustomer(int adminId, int customerId, String reason) throws AuthenticationException {

    }

    // ===== PAYMENT PROCESSING METHODS =====
    @Override
    public Payment[] getAllPayments(int adminId) throws AuthenticationException {
        return new Payment[0];
    }

    @Override
    public Payment getPaymentById(int adminId, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        return null;
    }

    @Override
    public Payment[] getPaymentsByStatus(int adminId, String status) throws AuthenticationException {
        return new Payment[0];
    }

    @Override
    public Payment[] getBookingPayments(int adminId, int bookingId) throws AuthenticationException {
        return new Payment[0];
    }

    @Override
    public void updatePaymentStatus(int adminId, int paymentId, String status) throws AuthenticationException, PaymentNotFoundException {

    }

    @Override
    public void refundPayment(int adminId, int paymentId, String reason) throws AuthenticationException, PaymentNotFoundException, PaymentException {

    }

    // ===== ADDITIONAL METHODS =====
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

    @Override
    public Movie[] getMoviesByGenre(int adminId, int genreId) throws AuthenticationException {
        return new Movie[0];
    }
}
