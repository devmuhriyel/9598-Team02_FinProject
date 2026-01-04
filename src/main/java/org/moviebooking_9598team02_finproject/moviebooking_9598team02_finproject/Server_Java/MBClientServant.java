package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            String query = "INSERT INTO customers (customer_id, username, email, password, phone_number, full_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextCustomerID);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, hashedPassword);
            statement.setString(5, fullName);
            statement.setString(6, phoneNumber);
            statement.setString(7, timeOfCreation.toString());
            statement.setString(8, timeOfCreation.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully created customer: " + username + " with ID: " + nextCustomerID);
                return nextCustomerID;
            } else {
                throw new InvalidInputException("Failed to create customer", "Database insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new InvalidInputException("Account Already Exists", "Username or email already exists");
            }
            throw new InvalidInputException("Database Error", "Failed to create customer: " + e.getMessage());
        } catch (AuthenticationException e) {
            throw new RuntimeException("Email is already registered");
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

                    synchronized (SessionManager.getCustomerSessionsLock()) {
                        if (SessionManager.isCustomerLoggedIn(customer_id)) {
                            System.out.println("Concurrent login rejected: Customer " + customer_id + " is already logged in.");
                            throw new AuthenticationException("This account is already logged in. Please log out from other devices before logging in again.");
                        }

                        String sessionToken = SessionManager.createCustomerSessionUnsafe(customer_id, rs.getString("username"));
                        System.out.println("Customer " + customer_id + " logged in successfully. Session token: " + sessionToken.substring(0, 8) + "...");
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
        SessionManager.SessionInfo session = SessionManager.getCustomerSession(customerId);
        if (session == null) {
            System.out.println("Customer " + customerId + " not logged in - logout ignored");
            return;
        }
        
        SessionManager.removeCustomerSession(customerId);
        System.out.println("Customer " + customerId + " logged out successfully - session invalidated");
    }

    // ===== ACCOUNT MANAGEMENT METHODS =====
    @Override
    public void updateCustomerProfile(int customerId, String phoneNumber, String fullName) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "UPDATE customers SET phone_number = ?, full_name = ?, updated_at = ? WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, phoneNumber);
            statement.setString(2, fullName);
            statement.setString(3, LocalDateTime.now().toString());
            statement.setInt(4, customerId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Customer profile updated: " + customerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Failed to update profile: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(int customerId, String oldPassword, String newPassword) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Verify old password
            String checkQuery = "SELECT password FROM customers WHERE customer_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, customerId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentHash = rs.getString("password");
                if (!PasswordManager.checkPassword(oldPassword, currentHash)) {
                    throw new AuthenticationException("Incorrect old password");
                }

                // Update to new password
                String updateQuery = "UPDATE customers SET password = ?, updated_at = ? WHERE customer_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, PasswordManager.hashPassword(newPassword));
                updateStmt.setString(2, LocalDateTime.now().toString());
                updateStmt.setInt(3, customerId);
                updateStmt.executeUpdate();

                System.out.println("Password changed for customer: " + customerId);
            } else {
                throw new AuthenticationException("Customer not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Failed to change password: " + e.getMessage());
        }
    }

    // ===== GENRE METHODS =====
    @Override
    public Genre[] browseGenres() {
        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM genres ORDER BY genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                genreList.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genreList.toArray(new Genre[0]);
    }

    @Override
    public Genre getGenreDetails(int genreId) {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM genres WHERE genre_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, genreId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                return genre;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Genre[] getMovieGenres(int movieId) throws MovieNotFoundException {
        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT g.* FROM genres g " +
                    "INNER JOIN movie_genres mg ON g.genre_id = mg.genre_id " +
                    "WHERE mg.movie_id = ? ORDER BY g.genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                genreList.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching genres for movie", movieId);
        }
        return genreList.toArray(new Genre[0]);
    }

    // ===== MOVIE BROWSING METHODS =====
    @Override
    public Movie[] browseMovies() {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies ORDER BY movie_title";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");

                // ✅ ATTACH IMAGE DATA before sending to client
                movie = attachImageDataToMovie(movie);

                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] searchMovies(String searchString) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_title LIKE ? ORDER BY movie_title";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + searchString + "%");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie getMovieDetails(int movieId) throws MovieNotFoundException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                return movie;
            } else {
                throw new MovieNotFoundException("Movie not found", movieId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching movie details", movieId);
        }
    }

    @Override
    public Movie[] filterByGenreIds(int[] genreIds) {
        if (genreIds == null || genreIds.length == 0) {
            return new Movie[0];
        }

        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT m.* FROM movies m " +
                            "INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                            "WHERE mg.genre_id IN ("
            );

            for (int i = 0; i < genreIds.length; i++) {
                query.append("?");
                if (i < genreIds.length - 1) query.append(",");
            }
            query.append(") ORDER BY m.movie_title");

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < genreIds.length; i++) {
                statement.setInt(i + 1, genreIds[i]);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] filterByGenreNames(String[] genreNames) {
        if (genreNames == null || genreNames.length == 0) {
            return new Movie[0];
        }

        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT m.* FROM movies m " +
                            "INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                            "INNER JOIN genres g ON mg.genre_id = g.genre_id " +
                            "WHERE g.genre_name IN ("
            );

            for (int i = 0; i < genreNames.length; i++) {
                query.append("?");
                if (i < genreNames.length - 1) query.append(",");
            }
            query.append(") ORDER BY m.movie_title");

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < genreNames.length; i++) {
                statement.setString(i + 1, genreNames[i]);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] filterByRating(int minRating) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_rating >= ? ORDER BY movie_rating DESC, movie_title";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, minRating);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] getMoviesByDate(int dayNumber, int monthNumber, int yearNumber) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT DISTINCT m.* FROM movies m " +
                    "INNER JOIN schedules s ON m.movie_id = s.movie_id " +
                    "INNER JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                    "WHERE DAY(ms.screening_date) = ? " +
                    "AND MONTH(ms.screening_date) = ? " +
                    "AND YEAR(ms.screening_date) = ? " +
                    "ORDER BY m.movie_title";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, dayNumber);
            statement.setInt(2, monthNumber);
            statement.setInt(3, yearNumber);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    // ===== SCHEDULE METHODS =====
    @Override
    public Schedule[] getMovieSchedules(int movieId) throws MovieNotFoundException {
        List<Schedule> scheduleList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            // First check if movie exists
            String checkQuery = "SELECT COUNT(*) FROM movies WHERE movie_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, movieId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) == 0) {
                throw new MovieNotFoundException("Movie not found", movieId);
            }

            // Get schedules
            String query = "SELECT * FROM schedules WHERE movie_id = ? ORDER BY schedule_id";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.scheduleId = rs.getInt("schedule_id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching schedules for movie", movieId);
        }
        return scheduleList.toArray(new Schedule[0]);
    }

    // ===== SEAT METHODS =====@Override
    public Seat[] getAvailableSeats(int scheduleId) throws ScheduleNotFoundException {
        List<Seat> seatList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if schedule exists
            String checkQuery = "SELECT COUNT(*) FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, scheduleId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) == 0) {
                throw new ScheduleNotFoundException("Schedule not found", scheduleId);
            }

            // Get ALL seats for this schedule (not just available ones)
            // We need ALL seats to show their types and prices in the UI
            String query = "SELECT s.* FROM seats s " +
                    "INNER JOIN schedule_list_seats sls ON s.seat_id = sls.seat_id " +
                    "WHERE sls.schedule_id = ? " +
                    "ORDER BY s.seat_row, s.seat_number";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat();
                seat.seatId = rs.getInt("seat_id");
                seat.seatRow = rs.getString("seat_row");
                seat.seatNumber = rs.getInt("seat_number");
                seat.seatType = rs.getString("seat_type");
                seat.seatPrice = rs.getDouble("seat_price");
                seat.createdAt = rs.getString("created_at");
                seat.updatedAt = rs.getString("updated_at");
                seatList.add(seat);
            }

            System.out.println("✅ Retrieved " + seatList.size() + " total seats for schedule " + scheduleId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching seats for schedule", scheduleId);
        }
        return seatList.toArray(new Seat[0]);
    }

    @Override
    public ScheduleDetails getScheduleDetailsView(int scheduleId) throws ScheduleNotFoundException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_schedule_details WHERE schedule_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                ScheduleDetails details = new ScheduleDetails();

                details.scheduleId = rs.getInt("schedule_id");
                details.movieId = rs.getInt("movie_id");
                details.movieScheduleId = rs.getInt("movie_schedule_id");
                details.movieRating = rs.getInt("movie_rating");
                details.availableSeats = rs.getInt("available_seats");
                details.totalSeats = rs.getInt("total_seats");

                details.movieTitle = safeString(rs.getString("movie_title"));
                details.movieDescription = safeString(rs.getString("movie_description"));
                details.moviePosterURL = safeString(rs.getString("moviePosterURL"));
                details.movieGenres = safeString(rs.getString("movie_genres"));
                details.screeningDate = safeString(rs.getString("screening_date"));
                details.screeningTime = safeString(rs.getString("screening_time"));
                details.createdAt = safeString(rs.getString("created_at"));
                details.updatedAt = safeString(rs.getString("updated_at"));

                return details;

            } else {
                ScheduleNotFoundException ex = new ScheduleNotFoundException();
                ex.message = "Schedule not found with ID: " + scheduleId;
                ex.scheduleId = scheduleId;
                throw ex;
            }

        } catch (ScheduleNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            ScheduleNotFoundException ex = new ScheduleNotFoundException();
            ex.message = "Database error: " + e.getMessage();
            ex.scheduleId = scheduleId;
            throw ex;
        }
    }

    @Override
    public ScheduleDetails[] getAllSchedulesWithDetails() {
        List<ScheduleDetails> detailsList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_schedule_details ORDER BY screening_date, screening_time";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ScheduleDetails details = new ScheduleDetails();
                details.scheduleId = rs.getInt("schedule_id");
                details.movieId = rs.getInt("movie_id");
                details.movieScheduleId = rs.getInt("movie_schedule_id");
                details.movieTitle = rs.getString("movie_title");
                details.movieDescription = rs.getString("movie_description");
                details.movieRating = rs.getInt("movie_rating");
                details.moviePosterURL = rs.getString("movie_poster_url");
                details.movieGenres = rs.getString("movie_genres");
                details.screeningDate = rs.getString("screening_date");
                details.screeningTime = rs.getString("screening_time");
                details.availableSeats = rs.getInt("available_seats");
                details.totalSeats = rs.getInt("total_seats");
                details.createdAt = rs.getString("created_at");
                details.updatedAt = rs.getString("updated_at");
                detailsList.add(details);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList.toArray(new ScheduleDetails[0]);
    }

    // ===== BOOKING METHODS =====
    @Override
    public int createBooking(int customerId, int scheduleId, int[] scheduleListSeatIds)
            throws AuthenticationException, SeatNotAvailableException, BookingException {

        // Validate customer session
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // 1. Validate schedule exists
            String checkSchedule = "SELECT COUNT(*) FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSchedule);
            checkStmt.setInt(1, scheduleId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) == 0) {
                throw new BookingException("Schedule not found", 404);
            }

            // 2. Validate all seats are available and calculate total price
            double totalPrice = 0.0;
            String checkSeatsQuery = "SELECT sls.schedule_list_seat_id, sls.status, s.seat_price " +
                    "FROM schedule_list_seats sls " +
                    "INNER JOIN seats s ON sls.seat_id = s.seat_id " +
                    "WHERE sls.schedule_list_seat_id = ? AND sls.schedule_id = ?";

            for (int seatId : scheduleListSeatIds) {
                PreparedStatement seatStmt = connection.prepareStatement(checkSeatsQuery);
                seatStmt.setInt(1, seatId);
                seatStmt.setInt(2, scheduleId);
                ResultSet seatRs = seatStmt.executeQuery();

                if (seatRs.next()) {
                    String status = seatRs.getString("status");
                    if (!"available".equals(status)) {
                        connection.rollback();
                        throw new SeatNotAvailableException("Seat is not available", seatId);
                    }
                    totalPrice += seatRs.getDouble("seat_price");
                } else {
                    connection.rollback();
                    throw new BookingException("Invalid seat ID: " + seatId, 400);
                }
            }

            // 3. Get next booking ID
            int nextBookingId = getLatestBookingID(connection) + 1;

            // 4. Insert booking
            String insertBooking = "INSERT INTO bookings (booking_id, customer_id, schedule_id, " +
                    "booking_status, total_price, booking_date, created_at, updated_at) " +
                    "VALUES (?, ?, ?, 'pending', ?, ?, ?, ?)";

            PreparedStatement bookingStmt = connection.prepareStatement(insertBooking);
            LocalDateTime now = LocalDateTime.now();
            bookingStmt.setInt(1, nextBookingId);
            bookingStmt.setInt(2, customerId);
            bookingStmt.setInt(3, scheduleId);
            bookingStmt.setDouble(4, totalPrice);
            bookingStmt.setString(5, now.toString());
            bookingStmt.setString(6, now.toString());
            bookingStmt.setString(7, now.toString());

            int rowsInserted = bookingStmt.executeUpdate();
            if (rowsInserted == 0) {
                connection.rollback();
                throw new BookingException("Failed to create booking", 500);
            }

            // 5. Update seat statuses to 'booked' and link to booking
            String updateSeats = "UPDATE schedule_list_seats SET status = 'booked', booking_id = ?, updated_at = ? " +
                    "WHERE schedule_list_seat_id = ? AND schedule_id = ?";

            for (int seatId : scheduleListSeatIds) {
                PreparedStatement updateStmt = connection.prepareStatement(updateSeats);
                updateStmt.setInt(1, nextBookingId);
                updateStmt.setString(2, now.toString());
                updateStmt.setInt(3, seatId);
                updateStmt.setInt(4, scheduleId);
                updateStmt.executeUpdate();
            }

            // Commit transaction
            connection.commit();
            System.out.println("✅ Booking created successfully! ID: " + nextBookingId +
                    " | Customer: " + customerId + " | Total: ₱" + totalPrice);

            return nextBookingId;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new BookingException("Database error: " + e.getMessage(), 500);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Booking getBookingDetails(int bookingId) throws BookingException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT b.*, " +
                    "GROUP_CONCAT(sls.schedule_list_seat_id) as seat_ids " +
                    "FROM bookings b " +
                    "LEFT JOIN schedule_list_seats sls ON b.booking_id = sls.booking_id " +
                    "WHERE b.booking_id = ? " +
                    "GROUP BY b.booking_id";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, bookingId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Booking booking = new Booking();
                booking.bookingId = rs.getInt("booking_id");
                booking.customerId = rs.getInt("customer_id");
                booking.scheduleId = rs.getInt("schedule_id");

                // ✅ FIX: Wrap ALL strings in safeString()
                booking.bookingStatus = safeString(rs.getString("booking_status"));
                booking.totalPrice = rs.getDouble("total_price");
                booking.bookingDate = safeString(rs.getString("booking_date"));
                booking.cancellationReason = safeString(rs.getString("cancellation_reason"));
                booking.createdAt = safeString(rs.getString("created_at"));
                booking.updatedAt = safeString(rs.getString("updated_at"));

                // Parse seat IDs
                String seatIdsStr = rs.getString("seat_ids");
                if (seatIdsStr != null && !seatIdsStr.isEmpty()) {
                    String[] seatIdArray = seatIdsStr.split(",");
                    int[] seatIds = new int[seatIdArray.length];
                    for (int i = 0; i < seatIdArray.length; i++) {
                        seatIds[i] = Integer.parseInt(seatIdArray[i].trim());
                    }
                    booking.scheduleListSeatIds = seatIds;
                } else {
                    booking.scheduleListSeatIds = new int[0];
                }

                return booking;
            } else {
                throw new BookingException("Booking not found", 404);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BookingException("Error fetching booking: " + e.getMessage(), 500);
        }
    }

    // REPLACE these three methods in MBClientServant.java to fix the GROUP BY errors:

    // FIX 1: Line ~809 - getCustomerBookings method
    @Override
    public Booking[] getCustomerBookings(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<Booking> bookingList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT b.*, " +
                    "GROUP_CONCAT(sls.schedule_list_seat_id) as seat_ids " +
                    "FROM bookings b " +
                    "LEFT JOIN schedule_list_seats sls ON b.booking_id = sls.booking_id " +
                    "WHERE b.customer_id = ? " +
                    "GROUP BY b.booking_id, b.customer_id, b.schedule_id, b.booking_status, " +
                    "b.total_price, b.booking_date, b.cancellation_reason, b.created_at, b.updated_at " +
                    "ORDER BY b.booking_date DESC";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.bookingId = rs.getInt("booking_id");
                booking.customerId = rs.getInt("customer_id");
                booking.scheduleId = rs.getInt("schedule_id");
                booking.bookingStatus = safeString(rs.getString("booking_status"));
                booking.totalPrice = rs.getDouble("total_price");
                booking.bookingDate = safeString(rs.getString("booking_date"));
                booking.cancellationReason = safeString(rs.getString("cancellation_reason"));
                booking.createdAt = safeString(rs.getString("created_at"));
                booking.updatedAt = safeString(rs.getString("updated_at"));

                String seatIdsStr = rs.getString("seat_ids");
                if (seatIdsStr != null && !seatIdsStr.isEmpty()) {
                    String[] seatIdArray = seatIdsStr.split(",");
                    int[] seatIds = new int[seatIdArray.length];
                    for (int i = 0; i < seatIdArray.length; i++) {
                        seatIds[i] = Integer.parseInt(seatIdArray[i].trim());
                    }
                    booking.scheduleListSeatIds = seatIds;
                } else {
                    booking.scheduleListSeatIds = new int[0];
                }

                bookingList.add(booking);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingList.toArray(new Booking[0]);
    }
    @Override
    public void cancelBooking(int customerId, int bookingId) throws AuthenticationException, BookingException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false);

            // Verify booking belongs to customer and is cancellable
            String checkQuery = "SELECT booking_status FROM bookings WHERE booking_id = ? AND customer_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookingId);
            checkStmt.setInt(2, customerId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                connection.rollback();
                throw new BookingException("Booking not found or unauthorized", 404);
            }

            String status = rs.getString("booking_status");
            if ("cancelled".equals(status)) {
                connection.rollback();
                throw new BookingException("Booking already cancelled", 400);
            }

            // Update booking status
            String updateBooking = "UPDATE bookings SET booking_status = 'cancelled', " +
                    "cancellation_reason = 'Cancelled by customer', updated_at = ? WHERE booking_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateBooking);
            updateStmt.setString(1, LocalDateTime.now().toString());
            updateStmt.setInt(2, bookingId);
            updateStmt.executeUpdate();

            // Release seats
            String releaseSeats = "UPDATE schedule_list_seats SET status = 'available', " +
                    "booking_id = NULL, updated_at = ? WHERE booking_id = ?";
            PreparedStatement releaseStmt = connection.prepareStatement(releaseSeats);
            releaseStmt.setString(1, LocalDateTime.now().toString());
            releaseStmt.setInt(2, bookingId);
            releaseStmt.executeUpdate();

            connection.commit();
            System.out.println("✅ Booking cancelled: " + bookingId);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new BookingException("Error cancelling booking: " + e.getMessage(), 500);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void confirmPayment(int customerId, int bookingId) throws AuthenticationException, BookingException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Verify booking belongs to customer
            String checkQuery = "SELECT booking_status FROM bookings WHERE booking_id = ? AND customer_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookingId);
            checkStmt.setInt(2, customerId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new BookingException("Booking not found or unauthorized", 404);
            }

            // Update status to confirmed
            String updateQuery = "UPDATE bookings SET booking_status = 'confirmed', updated_at = ? WHERE booking_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, LocalDateTime.now().toString());
            updateStmt.setInt(2, bookingId);
            updateStmt.executeUpdate();

            System.out.println("✅ Payment confirmed for booking: " + bookingId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BookingException("Error confirming payment: " + e.getMessage(), 500);
        }
    }

    @Override
    public void updateBooking(int customerId, int bookingId, int[] newSeatIds)
            throws AuthenticationException, BookingException, SeatNotAvailableException {
        // TODO: Implement if needed - allows changing seats before confirmation
        throw new BookingException("Feature not yet implemented", 501);
    }

    @Override
    public void updateBookingStatus(int customerId, int bookingId, String status)
            throws AuthenticationException, BookingException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "UPDATE bookings SET booking_status = ?, updated_at = ? " +
                    "WHERE booking_id = ? AND customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, status);
            statement.setString(2, LocalDateTime.now().toString());
            statement.setInt(3, bookingId);
            statement.setInt(4, customerId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new BookingException("Booking not found or unauthorized", 404);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BookingException("Error updating booking status: " + e.getMessage(), 500);
        }
    }

    @Override
    public Booking[] getBookingHistory(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<Booking> bookingList = new ArrayList<>();
        String query = "SELECT b.*, GROUP_CONCAT(sls.schedule_list_seat_id) as seat_ids " +
                "FROM bookings b " +
                "LEFT JOIN schedule_list_seats sls ON b.booking_id = sls.booking_id " +
                "WHERE b.customer_id = ? AND b.booking_status IN ('confirmed', 'cancelled') " +
                "GROUP BY b.booking_id, b.customer_id, b.schedule_id, b.booking_status, " +
                "b.total_price, b.booking_date, b.cancellation_reason, b.created_at, b.updated_at " +
                "ORDER BY b.booking_date DESC";

        try (Connection connection = DatabaseAccess.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.bookingId = rs.getInt("booking_id");
                booking.customerId = rs.getInt("customer_id");
                booking.scheduleId = rs.getInt("schedule_id");
                booking.totalPrice = rs.getDouble("total_price");

                booking.bookingStatus = safeString(rs.getString("booking_status"));
                booking.bookingDate = safeString(rs.getString("booking_date"));

                booking.cancellationReason = safeString(rs.getString("cancellation_reason"));

                booking.createdAt = safeString(rs.getString("created_at"));
                booking.updatedAt = safeString(rs.getString("updated_at"));

                String seatIdsStr = rs.getString("seat_ids");
                if (seatIdsStr != null && !seatIdsStr.isEmpty()) {
                    String[] seatIdArray = seatIdsStr.split(",");
                    int[] seatIds = new int[seatIdArray.length];
                    for (int i = 0; i < seatIdArray.length; i++) {
                        try {
                            seatIds[i] = Integer.parseInt(seatIdArray[i].trim());
                        } catch (NumberFormatException e) { seatIds[i] = 0; }
                    }
                    booking.scheduleListSeatIds = seatIds;
                } else {
                    booking.scheduleListSeatIds = new int[0];
                }

                bookingList.add(booking);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingList.toArray(new Booking[0]);
    }

    @Override
    public Booking[] getActiveBookings(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<Booking> bookingList = new ArrayList<>();
        // Note: Active bookings are 'pending'
        String query = "SELECT b.*, GROUP_CONCAT(sls.schedule_list_seat_id) as seat_ids " +
                "FROM bookings b " +
                "LEFT JOIN schedule_list_seats sls ON b.booking_id = sls.booking_id " +
                "WHERE b.customer_id = ? AND b.booking_status = 'pending' " +
                "GROUP BY b.booking_id, b.customer_id, b.schedule_id, b.booking_status, " +
                "b.total_price, b.booking_date, b.cancellation_reason, b.created_at, b.updated_at " +
                "ORDER BY b.booking_date DESC";

        try (Connection connection = DatabaseAccess.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.bookingId = rs.getInt("booking_id");
                booking.customerId = rs.getInt("customer_id");
                booking.scheduleId = rs.getInt("schedule_id");
                booking.totalPrice = rs.getDouble("total_price");

                // --- CRITICAL FIX: WRAP EVERYTHING IN safeString() ---
                booking.bookingStatus = safeString(rs.getString("booking_status"));
                booking.bookingDate = safeString(rs.getString("booking_date"));
                booking.cancellationReason = safeString(rs.getString("cancellation_reason"));
                booking.createdAt = safeString(rs.getString("created_at"));
                booking.updatedAt = safeString(rs.getString("updated_at"));

                // Handle Seats
                String seatIdsStr = rs.getString("seat_ids");
                if (seatIdsStr != null && !seatIdsStr.isEmpty()) {
                    String[] seatIdArray = seatIdsStr.split(",");
                    int[] seatIds = new int[seatIdArray.length];
                    for (int i = 0; i < seatIdArray.length; i++) {
                        try {
                            seatIds[i] = Integer.parseInt(seatIdArray[i].trim());
                        } catch (NumberFormatException e) { seatIds[i] = 0; }
                    }
                    booking.scheduleListSeatIds = seatIds;
                } else {
                    booking.scheduleListSeatIds = new int[0];
                }

                bookingList.add(booking);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingList.toArray(new Booking[0]);
    }

    @Override
    public BookingDetails getBookingDetailsView(int customerId, int bookingId)
            throws AuthenticationException, BookingException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_booking_details WHERE booking_id = ? AND customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, bookingId);
            statement.setInt(2, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                BookingDetails details = new BookingDetails();
                details.bookingId = rs.getInt("booking_id");
                details.bookingStatus = rs.getString("booking_status");
                details.totalPrice = rs.getDouble("total_price");
                details.bookingDate = rs.getString("booking_date");
                details.cancellationReason = rs.getString("cancellation_reason");
                details.customerId = rs.getInt("customer_id");
                details.customerName = rs.getString("customer_name");
                details.customerEmail = rs.getString("customer_email");
                details.scheduleId = rs.getInt("schedule_id");
                details.movieId = rs.getInt("movie_id");
                details.movieTitle = rs.getString("movie_title");
                details.screeningDate = rs.getString("screening_date");
                details.screeningTime = rs.getString("screening_time");
                details.seats = rs.getString("seats");
                details.createdAt = rs.getString("created_at");
                details.updatedAt = rs.getString("updated_at");
                return details;
            } else {
                throw new BookingException("Booking not found or unauthorized", 404);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BookingException("Error fetching booking details: " + e.getMessage(), 500);
        }
    }

    @Override
    public BookingDetails[] getCustomerBookingsWithDetails(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<BookingDetails> detailsList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {

            String query = "SELECT * FROM v_booking_details WHERE customer_id = ? ORDER BY booking_date DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                BookingDetails details = new BookingDetails();
                details.bookingId = rs.getInt("booking_id");
                details.totalPrice = rs.getDouble("total_price");
                details.customerId = rs.getInt("customer_id");
                details.scheduleId = rs.getInt("schedule_id");
                details.movieId = rs.getInt("movie_id");

                // --- THE CRASH FIX: Wrap ALL strings ---
                details.bookingStatus = safeString(rs.getString("booking_status"));
                details.bookingDate = safeString(rs.getString("booking_date"));
                details.cancellationReason = safeString(rs.getString("cancellation_reason"));
                details.customerName = safeString(rs.getString("customer_name"));
                details.customerEmail = safeString(rs.getString("customer_email"));
                details.movieTitle = safeString(rs.getString("movie_title"));
                details.screeningDate = safeString(rs.getString("screening_date"));
                details.screeningTime = safeString(rs.getString("screening_time"));
                details.seats = safeString(rs.getString("seats"));
                details.createdAt = safeString(rs.getString("created_at"));
                details.updatedAt = safeString(rs.getString("updated_at"));

                detailsList.add(details);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList.toArray(new BookingDetails[0]);
    }

    // ===== PAYMENT METHODS =====
    @Override
    public int processPayment(int customerId, int bookingId, double amount, String paymentMethod, String transactionId)
            throws AuthenticationException, BookingException, PaymentException {

        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // 1. Validate booking exists and belongs to customer
            String checkBooking = "SELECT booking_status, total_price FROM bookings WHERE booking_id = ? AND customer_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkBooking);
            checkStmt.setInt(1, bookingId);
            checkStmt.setInt(2, customerId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                connection.rollback();
                throw new BookingException("Booking not found or unauthorized", 404);
            }

            String status = rs.getString("booking_status");
            double bookingTotal = rs.getDouble("total_price");

            // Allow payment only if status is pending (or whatever your initial state is)
            if (!"pending".equals(status)) {
                connection.rollback();
                throw new PaymentException("Booking is already processed or cancelled", 400);
            }

            if (Math.abs(amount - bookingTotal) > 0.01) {
                connection.rollback();
                throw new PaymentException("Payment amount does not match booking total", 400);
            }

            // 2. Get next payment ID
            int nextPaymentId = getLatestPaymentID(connection) + 1;

            // 3. Insert payment record (Status: PENDING)
            // This record tells the Admin: "Money has been sent, please check."
            String insertPayment = "INSERT INTO payments (payment_id, booking_id, amount, payment_method, " +
                    "payment_status, payment_date, transaction_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, 'pending', ?, ?, ?, ?)";

            PreparedStatement paymentStmt = connection.prepareStatement(insertPayment);
            LocalDateTime now = LocalDateTime.now();
            paymentStmt.setInt(1, nextPaymentId);
            paymentStmt.setInt(2, bookingId);
            paymentStmt.setDouble(3, amount);
            paymentStmt.setString(4, paymentMethod);
            paymentStmt.setString(5, now.toString());
            paymentStmt.setString(6, transactionId);
            paymentStmt.setString(7, now.toString());
            paymentStmt.setString(8, now.toString());

            int rowsInserted = paymentStmt.executeUpdate();
            if (rowsInserted == 0) {
                connection.rollback();
                throw new PaymentException("Failed to process payment", 500);
            }

            String updateBooking = "UPDATE bookings SET booking_status = 'pending', updated_at = ? WHERE booking_id = ?";

            PreparedStatement updateStmt = connection.prepareStatement(updateBooking);
            updateStmt.setString(1, now.toString());
            updateStmt.setInt(2, bookingId);
            updateStmt.executeUpdate();

            // Commit transaction
            connection.commit();
            System.out.println("✅ Payment Submitted! ID: " + nextPaymentId +
                    " | Booking: " + bookingId + " | Status: PENDING ADMIN APPROVAL");

            return nextPaymentId;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new PaymentException("Database error: " + e.getMessage(), 500);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Payment getPaymentDetails(int customerId, int paymentId)
            throws AuthenticationException, PaymentNotFoundException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT p.* FROM payments p " +
                    "INNER JOIN bookings b ON p.booking_id = b.booking_id " +
                    "WHERE p.payment_id = ? AND b.customer_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, paymentId);
            statement.setInt(2, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.paymentId = rs.getInt("payment_id");
                payment.bookingId = rs.getInt("booking_id");
                payment.amount = rs.getDouble("amount");
                payment.paymentMethod = rs.getString("payment_method");
                payment.paymentStatus = rs.getString("payment_status");
                payment.paymentDate = rs.getString("payment_date");
                payment.transactionId = rs.getString("transaction_id");
                payment.refundReason = rs.getString("refund_reason");
                return payment;
            } else {
                throw new PaymentNotFoundException("Payment not found", paymentId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PaymentNotFoundException("Error fetching payment: " + e.getMessage(), paymentId);
        }
    }

    @Override
    public Payment[] getCustomerPayments(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<Payment> paymentList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT p.* FROM payments p " +
                    "INNER JOIN bookings b ON p.booking_id = b.booking_id " +
                    "WHERE b.customer_id = ? ORDER BY p.payment_date DESC";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment();
                payment.paymentId = rs.getInt("payment_id");
                payment.bookingId = rs.getInt("booking_id");
                payment.amount = rs.getDouble("amount");
                payment.paymentMethod = rs.getString("payment_method");
                payment.paymentStatus = rs.getString("payment_status");
                payment.paymentDate = rs.getString("payment_date");
                payment.transactionId = rs.getString("transaction_id");
                payment.refundReason = rs.getString("refund_reason");
                paymentList.add(payment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentList.toArray(new Payment[0]);
    }

    @Override
    public Payment getBookingPayment(int customerId, int bookingId)
            throws AuthenticationException, BookingException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT p.* FROM payments p " +
                    "INNER JOIN bookings b ON p.booking_id = b.booking_id " +
                    "WHERE p.booking_id = ? AND b.customer_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, bookingId);
            statement.setInt(2, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.paymentId = rs.getInt("payment_id");
                payment.bookingId = rs.getInt("booking_id");
                payment.amount = rs.getDouble("amount");
                payment.paymentMethod = rs.getString("payment_method");
                payment.paymentStatus = rs.getString("payment_status");
                payment.paymentDate = rs.getString("payment_date");
                payment.transactionId = rs.getString("transaction_id");
                payment.refundReason = rs.getString("refund_reason");
                return payment;
            } else {
                throw new BookingException("Payment not found for booking", 404);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BookingException("Error fetching payment: " + e.getMessage(), 500);
        }
    }

    // Continue from getPaymentDetailsView - the part that was cut off

    @Override
    public PaymentDetails getPaymentDetailsView(int customerId, int paymentId)
            throws AuthenticationException, PaymentNotFoundException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_payment_details WHERE payment_id = ? AND customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, paymentId);
            statement.setInt(2, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                PaymentDetails details = new PaymentDetails();
                details.paymentId = rs.getInt("payment_id");
                details.bookingId = rs.getInt("booking_id");
                details.amount = rs.getDouble("amount");
                details.paymentMethod = rs.getString("payment_method");
                details.paymentStatus = rs.getString("payment_status");
                details.paymentDate = rs.getString("payment_date");
                details.transactionId = rs.getString("transaction_id");
                details.refundReason = rs.getString("refund_reason");
                details.customerId = rs.getInt("customer_id");
                details.customerName = rs.getString("customer_name");
                details.movieTitle = rs.getString("movie_title");
                details.screeningDate = rs.getString("screening_date");
                details.screeningTime = rs.getString("screening_time");
                details.createdAt = rs.getString("created_at");
                details.updatedAt = rs.getString("updated_at");
                return details;
            } else {
                throw new PaymentNotFoundException("Payment not found", paymentId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PaymentNotFoundException("Error fetching payment details: " + e.getMessage(), paymentId);
        }
    }

    @Override
    public PaymentDetails[] getCustomerPaymentsWithDetails(int customerId) throws AuthenticationException {
        if (!SessionManager.isCustomerLoggedIn(customerId)) {
            throw new AuthenticationException("Customer not logged in");
        }

        List<PaymentDetails> detailsList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_payment_details WHERE customer_id = ? ORDER BY payment_date DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                PaymentDetails details = new PaymentDetails();
                details.paymentId = rs.getInt("payment_id");
                details.bookingId = rs.getInt("booking_id");
                details.amount = rs.getDouble("amount");
                details.paymentMethod = rs.getString("payment_method");
                details.paymentStatus = rs.getString("payment_status");
                details.paymentDate = rs.getString("payment_date");
                details.transactionId = rs.getString("transaction_id");
                details.refundReason = rs.getString("refund_reason");
                details.customerId = rs.getInt("customer_id");
                details.customerName = rs.getString("customer_name");
                details.movieTitle = rs.getString("movie_title");
                details.screeningDate = rs.getString("screening_date");
                details.screeningTime = rs.getString("screening_time");
                details.createdAt = rs.getString("created_at");
                details.updatedAt = rs.getString("updated_at");
                detailsList.add(details);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList.toArray(new PaymentDetails[0]);
    }

    // ===== HELPER METHODS (NOT IN THE IDL) =====
    private int getLatestCustomerID() throws AuthenticationException {
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT MAX(customer_id) as max_id FROM customers";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                int maxID = resultSet.getInt("max_id");
                return resultSet.wasNull() ? 0 : maxID;
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
        return 0;
    }

    private int getLatestBookingID(Connection connection) throws SQLException {
        String query = "SELECT MAX(booking_id) as max_id FROM bookings";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        if(resultSet.next()){
            int maxID = resultSet.getInt("max_id");
            return resultSet.wasNull() ? 0 : maxID;
        }
        return 0;
    }

    private int getLatestPaymentID(Connection connection) throws SQLException {
        String query = "SELECT MAX(payment_id) as max_id FROM payments";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        if(resultSet.next()){
            int maxID = resultSet.getInt("max_id");
            return resultSet.wasNull() ? 0 : maxID;
        }
        return 0;
    }
    private Movie attachImageDataToMovie(Movie movie) {
        if (movie.moviePosterURL == null || movie.moviePosterURL.isEmpty()) {
            return movie;
        }

        try {
            // Read image file
            java.nio.file.Path imagePath = java.nio.file.Paths.get(movie.moviePosterURL);

            if (!java.nio.file.Files.exists(imagePath)) {
                System.err.println("⚠️ Image file not found: " + movie.moviePosterURL);
                return movie;
            }

            byte[] imageBytes = java.nio.file.Files.readAllBytes(imagePath);
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

            // Concatenate: path|||IMAGE_DATA|||base64data
            movie.moviePosterURL = movie.moviePosterURL + "|||IMAGE_DATA|||" + base64Image;

            System.out.println("✅ Attached image data for movie " + movie.movieId);

        } catch (IOException e) {
            System.err.println("❌ Error attaching image data: " + e.getMessage());
        }

        return movie;
    }

    private String safeString(String input) {
        return (input == null) ? "" : input;
    }
}