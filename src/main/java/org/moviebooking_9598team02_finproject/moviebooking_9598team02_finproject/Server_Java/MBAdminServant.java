package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.omg.CORBA.ORB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                    int admin_id = rs.getInt("admin_id");

                    // ✅ CORRECT ORDER: Check if already logged in FIRST
                    if (SessionManager.isAdminLoggedIn(admin_id)) {
                        System.out.println("Admin " + admin_id + " already has active session. Removing old session.");
                        SessionManager.removeAdminSession(admin_id);
                    }

                    // ✅ NOW create new session
                    SessionManager.createAdminSession(admin_id, rs.getString("username"));
                    System.out.println("✅ Admin " + admin_id + " logged in successfully");

                    // Populate admin object
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
        if(SessionManager.isAdminLoggedIn(adminId)){
            SessionManager.removeAdminSession(adminId);
        }
    }

    @Override
    public int createAdmin(int creatorAdminId, String username, String email, String password, String fullName, String phoneNumber) throws AuthenticationException, InvalidInputException {
        try (Connection connection = DatabaseAccess.getConnection()){
            int nextAdminID = getLatestAdminID() + 1;
            String hashedPassword = PasswordManager.hashPassword(password);
            LocalDateTime timeOfCreation = LocalDateTime.now();
            String query = "INSERT INTO admins (admin_id, username, email, password, full_name, phone_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextAdminID);
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
                System.out.println("Successfully created admin: " + username + " with ID: " + nextAdminID);
                return nextAdminID;
            } else {
                throw new InvalidInputException("Failed to create admin", "Database insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new InvalidInputException("Account Already Exists", "Username or email already exists");
            }
            throw new InvalidInputException("Database Error", "Failed to create admin: " + e.getMessage());
        }
    }

    @Override
    public int addGenre(int adminId, String genreName, String genreDescription) throws AuthenticationException, InvalidInputException {
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate input
        if (genreName == null || genreName.trim().isEmpty()) {
            throw new InvalidInputException("Genre name cannot be empty", "Genre name is required");
        }
        if (genreDescription == null) {
            genreDescription = "";
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if genre name already exists
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_name = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, genreName.trim());
            ResultSet checkRs = checkStatement.executeQuery();
            if (checkRs.next()) {
                throw new InvalidInputException("Genre already exists", "A genre with this name already exists");
            }

            // Get next genre ID
            int nextGenreId = getLatestGenreId() + 1;
            LocalDateTime timeOfCreation = LocalDateTime.now();

            // Insert new genre
            String query = "INSERT INTO genres (genre_id, genre_name, genre_description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextGenreId);
            statement.setString(2, genreName.trim());
            statement.setString(3, genreDescription.trim());
            statement.setString(4, timeOfCreation.toString());
            statement.setString(5, timeOfCreation.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully created genre: " + genreName + " with ID: " + nextGenreId);
                return nextGenreId;
            } else {
                throw new InvalidInputException("Failed to create genre", "Database insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new InvalidInputException("Genre already exists", "A genre with this name already exists");
            }
            throw new InvalidInputException("Database Error", "Failed to create genre: " + e.getMessage());
        }
    }

    @Override
    public void updateGenre(int adminId, int genreId, String genreName, String genreDescription) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate input
        if (genreName == null || genreName.trim().isEmpty()) {
            throw new AuthenticationException("Genre name cannot be empty");
        }
        if (genreDescription == null) {
            genreDescription = "";
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if genre exists
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, genreId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new AuthenticationException("Genre not found");
            }

            // Check if another genre with the same name exists
            String duplicateQuery = "SELECT genre_id FROM genres WHERE genre_name = ? AND genre_id != ?";
            PreparedStatement duplicateStatement = connection.prepareStatement(duplicateQuery);
            duplicateStatement.setString(1, genreName.trim());
            duplicateStatement.setInt(2, genreId);
            ResultSet duplicateRs = duplicateStatement.executeQuery();
            if (duplicateRs.next()) {
                throw new AuthenticationException("A genre with this name already exists");
            }

            // Update genre
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE genres SET genre_name = ?, genre_description = ?, updated_at = ? WHERE genre_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, genreName.trim());
            statement.setString(2, genreDescription.trim());
            statement.setString(3, timeOfUpdate.toString());
            statement.setInt(4, genreId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated genre ID: " + genreId);
            } else {
                throw new AuthenticationException("Failed to update genre");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteGenre(int adminId, int genreId) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if genre exists
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, genreId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new AuthenticationException("Genre not found");
            }

            // Check if genre is being used by any movies
            String usageQuery = "SELECT COUNT(*) as count FROM movie_genres WHERE genre_id = ?";
            PreparedStatement usageStatement = connection.prepareStatement(usageQuery);
            usageStatement.setInt(1, genreId);
            ResultSet usageRs = usageStatement.executeQuery();
            if (usageRs.next() && usageRs.getInt("count") > 0) {
                throw new AuthenticationException("Cannot delete genre: It is currently assigned to one or more movies");
            }

            // Delete genre
            String query = "DELETE FROM genres WHERE genre_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, genreId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully deleted genre ID: " + genreId);
            } else {
                throw new AuthenticationException("Failed to delete genre");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Genre[] getAllGenres(int adminId) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM genres ORDER BY genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            java.util.List<Genre> genreList = new java.util.ArrayList<>();
            while (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                genreList.add(genre);
            }

            return genreList.toArray(new Genre[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Genre getGenreById(int adminId, int genreId) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

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
            } else {
                throw new AuthenticationException("Genre not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public int addMovie(int adminId, String movieTitle, int[] genreIds, String movieDescription, int movieRating, String moviePosterURL) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    // ===== ADMIN MANAGEMENT METHODS IMPLEMENTATION =====

    @Override
    public Admin getAdminById(int adminId, int requestingAdminId) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(requestingAdminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT admin_id, username, email, full_name, phone_number, created_at, updated_at " +
                    "FROM admins WHERE admin_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, adminId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.adminId = rs.getInt("admin_id");
                admin.username = rs.getString("username");
                admin.email = rs.getString("email");
                admin.password = ""; // Never return password
                admin.fullName = rs.getString("full_name");
                admin.phoneNumber = rs.getString("phone_number");
                admin.createdAt = rs.getString("created_at");
                admin.updatedAt = rs.getString("updated_at");

                return admin;
            } else {
                throw new AuthenticationException("Admin not found with ID: " + adminId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Admin[] getAllAdmins(int adminId) throws AuthenticationException {
        System.out.println("IS RUNNING");
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT admin_id, username, email, full_name, phone_number, created_at, updated_at " +
                    "FROM admins ORDER BY created_at DESC";
            PreparedStatement statement = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            // Count rows
            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst();
            }

            if (rowCount == 0) {
                return new Admin[0];
            }

            Admin[] admins = new Admin[rowCount];
            int index = 0;

            while (rs.next()) {
                Admin admin = new Admin();
                admin.adminId = rs.getInt("admin_id");
                admin.username = rs.getString("username");
                admin.email = rs.getString("email");
                admin.password = "";
                admin.fullName = rs.getString("full_name");
                admin.phoneNumber = rs.getString("phone_number");
                admin.createdAt = rs.getString("created_at");
                admin.updatedAt = rs.getString("updated_at");

                admins[index++] = admin;
            }

            System.out.println("Admin " + adminId + " retrieved " + rowCount + " admins");
            return admins;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Admin[] searchAdmins(int adminId, String searchString) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT admin_id, username, email, full_name, phone_number, created_at, updated_at " +
                    "FROM admins " +
                    "WHERE username LIKE ? OR email LIKE ? OR full_name LIKE ? OR CAST(admin_id AS CHAR) LIKE ? " +
                    "ORDER BY full_name";
            PreparedStatement statement = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            String searchPattern = "%" + searchString + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);

            ResultSet rs = statement.executeQuery();

            // Count rows
            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst();
            }

            if (rowCount == 0) {
                return new Admin[0];
            }

            Admin[] admins = new Admin[rowCount];
            int index = 0;

            while (rs.next()) {
                Admin admin = new Admin();
                admin.adminId = rs.getInt("admin_id");
                admin.username = rs.getString("username");
                admin.email = rs.getString("email");
                admin.password = "";
                admin.fullName = rs.getString("full_name");
                admin.phoneNumber = rs.getString("phone_number");
                admin.createdAt = rs.getString("created_at");
                admin.updatedAt = rs.getString("updated_at");

                admins[index++] = admin;
            }

            return admins;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void updateAdminProfile(int adminId, String fullName, String phoneNumber) throws AuthenticationException {
        // NOTE: adminId here is the admin BEING UPDATED, not necessarily the requester
        // In a production system, you'd want to verify the requester has permission

        // For now, we just check if the target admin exists by trying to update
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "UPDATE admins SET full_name = ?, phone_number = ?, updated_at = NOW() " +
                    "WHERE admin_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, fullName);
            statement.setString(2, phoneNumber);
            statement.setInt(3, adminId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new AuthenticationException("Admin not found with ID: " + adminId);
            }

            System.out.println("Admin " + adminId + " profile updated successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }


    @Override
    public void updateAdminEmail(int adminId, String newEmail, String password)
            throws AuthenticationException, InvalidInputException {
        // adminId is the admin BEING UPDATED

        // 1. Verify password first
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Get current password hash
            String checkQuery = "SELECT password FROM admins WHERE admin_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, adminId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (!PasswordManager.checkPassword(password, hashedPassword)) {
                    throw new AuthenticationException("Incorrect password");
                }
            } else {
                throw new AuthenticationException("Admin not found with ID: " + adminId);
            }

            // 2. Update email
            String updateQuery = "UPDATE admins SET email = ?, updated_at = NOW() WHERE admin_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, newEmail);
            updateStmt.setInt(2, adminId);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Admin " + adminId + " updated email to: " + newEmail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new InvalidInputException("Email already exists", "email");
            }
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void changeAdminPassword(int adminId, String oldPassword, String newPassword)
            throws AuthenticationException {
        // adminId is the admin BEING UPDATED

        // 1. Verify old password
        try (Connection connection = DatabaseAccess.getConnection()) {
            String checkQuery = "SELECT password FROM admins WHERE admin_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, adminId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentHashedPassword = rs.getString("password");
                if (!PasswordManager.checkPassword(oldPassword, currentHashedPassword)) {
                    throw new AuthenticationException("Incorrect old password");
                }
            } else {
                throw new AuthenticationException("Admin not found with ID: " + adminId);
            }

            // 2. Update password
            String newHashedPassword = PasswordManager.hashPassword(newPassword);
            String updateQuery = "UPDATE admins SET password = ?, updated_at = NOW() WHERE admin_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, newHashedPassword);
            updateStmt.setInt(2, adminId);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Admin " + adminId + " changed password successfully");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deactivateAdmin(int superAdminId, int targetAdminId, String reason)
            throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(superAdminId)) {
            throw new AuthenticationException("Unauthorized access: Super admin must be logged in.");
        }

        // 2. Prevent self-deactivation
        if (superAdminId == targetAdminId) {
            throw new AuthenticationException("Cannot deactivate your own account");
        }

        // 3. Log the deactivation
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Create audit log
            String auditQuery = "INSERT INTO admin_audit_log (admin_id, performed_by_admin_id, action, reason, created_at) " +
                    "VALUES (?, ?, 'DEACTIVATED', ?, NOW())";
            PreparedStatement auditStmt = connection.prepareStatement(auditQuery);
            auditStmt.setInt(1, targetAdminId);
            auditStmt.setInt(2, superAdminId);
            auditStmt.setString(3, reason);

            auditStmt.executeUpdate();

            // Remove active session
            SessionManager.removeAdminSession(targetAdminId);

            System.out.println("Super Admin " + superAdminId + " deactivated Admin " + targetAdminId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAdmin(int superAdminId, int targetAdminId) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(superAdminId)) {
            throw new AuthenticationException("Unauthorized access: Super admin must be logged in.");
        }

        // 2. Prevent self-deletion
        if (superAdminId == targetAdminId) {
            throw new AuthenticationException("Cannot delete your own account");
        }

        // 3. Soft delete (recommended) or hard delete
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Option 1: Soft delete (recommended) - add 'deleted_at' column
            // String query = "UPDATE admins SET deleted_at = NOW() WHERE admin_id = ?";

            // Option 2: Hard delete (use with caution)
            String query = "DELETE FROM admins WHERE admin_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, targetAdminId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new AuthenticationException("Admin not found");
            }

            // Remove active session
            SessionManager.removeAdminSession(targetAdminId);

            System.out.println("Super Admin " + superAdminId + " deleted Admin " + targetAdminId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
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
    @Override //do not use
    public int addSeat(int adminId, String seatRow, int seatNumber, String seatType, double seatPrice) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public void updateSeatPrice(int adminId, int seatId, double newPrice) throws AuthenticationException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if seat exists
            String checkQuery = "SELECT seat_id FROM seats WHERE seat_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, seatId);
            ResultSet checkRs = checkStatement.executeQuery();

            if (!checkRs.next()) {
                throw new AuthenticationException("Seat not found with ID: " + seatId);
            }

            // Update seat price
            LocalDateTime now = LocalDateTime.now();
            String updateQuery = "UPDATE seats SET seat_price = ?, updated_at = ? WHERE seat_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setDouble(1, newPrice);
            updateStatement.setString(2, now.toString());
            updateStatement.setInt(3, seatId);

            int rowsAffected = updateStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated price for seat ID: " + seatId + " to: " + newPrice);
            } else {
                throw new AuthenticationException("Failed to update seat price");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void updateSeatType(int adminId, int seatId, String newType) throws AuthenticationException {
        // Validate seat type (only allow: regular, vip, maintenance)
        if (!newType.equalsIgnoreCase("regular") &&
                !newType.equalsIgnoreCase("vip") &&
                !newType.equalsIgnoreCase("maintenance")) {
            throw new AuthenticationException("Invalid seat type. Allowed types: regular, vip, maintence");
        }
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if seat exists
            String checkQuery = "SELECT seat_id FROM seats WHERE seat_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, seatId);
            ResultSet checkRs = checkStatement.executeQuery();

            if (!checkRs.next()) {
                throw new AuthenticationException("Seat not found with ID: " + seatId);
            }

            // Update seat type
            LocalDateTime now = LocalDateTime.now();
            String updateQuery = "UPDATE seats SET seat_type = ?, updated_at = ? WHERE seat_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, newType);
            updateStatement.setString(2, now.toString());
            updateStatement.setInt(3, seatId);

            int rowsAffected = updateStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated type for seat ID: " + seatId + " to: " + newType);
            } else {
                throw new AuthenticationException("Failed to update seat type");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }


    @Override //do not use
    public void deleteSeat(int adminId, int seatId) throws AuthenticationException {

    }

    @Override
    public Seat[] getAllSeats(int adminId) throws AuthenticationException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Query to get all seats ordered by row and number
            String query = "SELECT * FROM seats ORDER BY seat_row ASC, seat_number ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Count total seats
            java.util.List<Seat> seatList = new java.util.ArrayList<>();

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

            System.out.println("Retrieved " + seatList.size() + " seats from database");

            // Convert ArrayList to Array
            return seatList.toArray(new Seat[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
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

    // ===== CUSTOMER MANAGEMENT METHODS IMPLEMENTATION =====

    @Override
    public Customer[] getAllCustomers(int adminId) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT customer_id, username, email, phone_number, full_name, created_at, updated_at " +
                    "FROM customers ORDER BY created_at DESC";
            PreparedStatement statement = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            // Count rows
            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst();
            }

            if (rowCount == 0) {
                return new Customer[0];
            }

            Customer[] customers = new Customer[rowCount];
            int index = 0;

            while (rs.next()) {
                Customer customer = new Customer();
                customer.customerId = rs.getInt("customer_id");
                customer.username = rs.getString("username");
                customer.email = rs.getString("email");
                customer.password = ""; // Never return password
                customer.phoneNumber = rs.getString("phone_number");
                customer.fullName = rs.getString("full_name");
                customer.createdAt = rs.getString("created_at");
                customer.updatedAt = rs.getString("updated_at");

                customers[index++] = customer;
            }

            System.out.println("Admin " + adminId + " retrieved " + rowCount + " customers");
            return customers;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Customer[] searchCustomers(int adminId, String searchString) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query with LIKE search
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT customer_id, username, email, phone_number, full_name, created_at, updated_at " +
                    "FROM customers " +
                    "WHERE username LIKE ? OR email LIKE ? OR full_name LIKE ? OR CAST(customer_id AS CHAR) LIKE ? " +
                    "ORDER BY full_name";
            PreparedStatement statement = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            String searchPattern = "%" + searchString + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);

            ResultSet rs = statement.executeQuery();

            // Count rows
            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst();
            }

            if (rowCount == 0) {
                return new Customer[0];
            }

            Customer[] customers = new Customer[rowCount];
            int index = 0;

            while (rs.next()) {
                Customer customer = new Customer();
                customer.customerId = rs.getInt("customer_id");
                customer.username = rs.getString("username");
                customer.email = rs.getString("email");
                customer.password = "";
                customer.phoneNumber = rs.getString("phone_number");
                customer.fullName = rs.getString("full_name");
                customer.createdAt = rs.getString("created_at");
                customer.updatedAt = rs.getString("updated_at");

                customers[index++] = customer;
            }

            System.out.println("Admin " + adminId + " searched customers with '" + searchString + "', found " + rowCount);
            return customers;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }


    public Customer getCustomerDetails(int adminId, int customerId) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Query
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT customer_id, username, email, phone_number, full_name, created_at, updated_at " +
                    "FROM customers WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer();
                customer.customerId = rs.getInt("customer_id");
                customer.username = rs.getString("username");
                customer.email = rs.getString("email");
                customer.password = "";
                customer.phoneNumber = rs.getString("phone_number");
                customer.fullName = rs.getString("full_name");
                customer.createdAt = rs.getString("created_at");
                customer.updatedAt = rs.getString("updated_at");

                System.out.println("Admin " + adminId + " retrieved details for customer " + customerId);
                return customer;
            } else {
                throw new AuthenticationException("Customer not found with ID: " + customerId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }


    @Override
    public void updateCustomerInfo(int adminId, int customerId, String email, String phoneNumber, String fullName)
            throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Unauthorized access: Admin must be logged in.");
        }

        // 2. Database Update
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "UPDATE customers SET email = ?, phone_number = ?, full_name = ?, updated_at = NOW() " +
                    "WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, phoneNumber);
            statement.setString(3, fullName);
            statement.setInt(4, customerId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new AuthenticationException("Customer not found with ID: " + customerId);
            }

            System.out.println("Admin " + adminId + " updated customer " + customerId + " info");

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                throw new AuthenticationException("Email already exists");
            }
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deactivateCustomer(int adminId, int customerId, String reason) throws AuthenticationException {

    }

    // ===== HELPER METHOD: Get Customer Booking Count =====
    public int getCustomerBookingCount(int customerId) throws SQLException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT COUNT(*) as booking_count FROM bookings WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("booking_count");
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // ===== PAYMENT PROCESSING METHODS =====
    @Override
    public Payment[] getAllPayments(int adminId) throws AuthenticationException {
        System.out.println("Admin " + adminId + " requested all payments.");
        List<Payment> paymentList = new ArrayList<>();

        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                paymentList.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paymentList.toArray(new Payment[0]);
    }

    @Override
    public Payment getPaymentById(int adminId, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new PaymentNotFoundException();
    }

    @Override
    public Payment[] getPaymentsByStatus(int adminId, String status) throws AuthenticationException {
        List<Payment> paymentList = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE payment_status = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    paymentList.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentList.toArray(new Payment[0]);
    }

    @Override
    public Payment[] getBookingPayments(int adminId, int bookingId) throws AuthenticationException {
        List<Payment> paymentList = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ?";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    paymentList.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentList.toArray(new Payment[0]);
    }

    @Override
    public void updatePaymentStatus(int adminId, int paymentId, String status) throws AuthenticationException, PaymentNotFoundException {
        String sql = "UPDATE payments SET payment_status = ?, updated_at = NOW() WHERE payment_id = ?";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, paymentId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new PaymentNotFoundException();
            }
            System.out.println("Admin " + adminId + " updated payment " + paymentId + " to " + status);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void refundPayment(int adminId, int paymentId, String reason) throws AuthenticationException, PaymentNotFoundException, PaymentException {
        // not sure here pa pero working on
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

    // ===== HELPER METHODS (NOT IN THE IDL) =====

    private int getLatestAdminID() throws AuthenticationException {
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT MAX(admin_id) as max_id FROM admins";
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

    private int getLatestGenreId() throws AuthenticationException {
        try(Connection connection = DatabaseAccess.getConnection()){
            String query = "SELECT MAX(genre_id) as max_id FROM genres";
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

    // Helper para sa payment

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();

        p.paymentId = rs.getInt("payment_id");
        p.bookingId = rs.getInt("booking_id");
        p.amount = rs.getDouble("amount");

        String method = rs.getString("payment_method");
        p.paymentMethod = (method != null) ? method : "Unknown";

        String status = rs.getString("payment_status");
        p.paymentStatus = (status != null) ? status : "Pending";

        String txn = rs.getString("transaction_id");
        p.transactionId = (txn != null) ? txn : "N/A";

        String pDate = rs.getString("payment_date");
        if (pDate == null || pDate.isEmpty()) {
            try {
                pDate = rs.getString("created_at");
            } catch (SQLException e) {
                pDate = "N/A";
            }
        }
        p.paymentDate = (pDate != null) ? pDate : "N/A";

        try {
            String reason = rs.getString("refund_reason");
            p.refundReason = (reason != null) ? reason : "";
        } catch (SQLException e) {
            p.refundReason = "";
        }

        return p;
    }
}