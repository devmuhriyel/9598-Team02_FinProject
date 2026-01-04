package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.omg.CORBA.ORB;

import java.io.IOException;
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
    public MBAdminServant() {
    }

    // ===== AUTHENTICATION METHODS =====
    @Override
    public AdminLoginResponse loginAdmin(String username, String password) throws AuthenticationException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM admins WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordManager.checkPassword(password, hashedPassword)) {
                    int adminId = rs.getInt("admin_id");

                    synchronized (SessionManager.getAdminSessionsLock()) {
                        // ✅ FIX: Check if already logged in BEFORE creating session
                        if (SessionManager.isAdminLoggedIn(adminId)) {
                            System.out.println("⚠️ Concurrent login rejected: Admin " + adminId + " already logged in");
                            throw new AuthenticationException(
                                    "This account is already logged in from another device. " +
                                            "Please log out from the other device first, or wait 30 minutes for the session to expire."
                            );
                        }

                        // Create new session and get token
                        String sessionToken = SessionManager.createAdminSession(adminId, rs.getString("username"));

                        // Build response
                        Admin admin = new Admin();
                        admin.adminId = adminId;
                        admin.username = rs.getString("username");
                        admin.email = rs.getString("email");
                        admin.password = ""; // NEVER send password
                        admin.fullName = rs.getString("full_name");
                        admin.phoneNumber = rs.getString("phone_number");
                        admin.createdAt = rs.getString("created_at");
                        admin.updatedAt = rs.getString("updated_at");

                        AdminLoginResponse response = new AdminLoginResponse();
                        response.admin = admin;
                        response.sessionToken = sessionToken;

                        System.out.println("✅ Admin " + adminId + " logged in successfully");
                        return response;
                    }
                } else {
                    throw new AuthenticationException("Incorrect password");
                }
            } else {
                throw new AuthenticationException("Account does not exist");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void logoutAdmin(int adminId, String sessionToken) throws AuthenticationException {
        // Validate token before logout
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - already logged out or session expired");
        }

        SessionManager.removeAdminSession(adminId);
        System.out.println("✅ Admin " + adminId + " logged out successfully");
    }

    @Override
    public int createAdmin(int creatorAdminId, String sessionToken, String username, String email, String password, String fullName, String phoneNumber) throws AuthenticationException, InvalidInputException {

        if (!SessionManager.validateAdminSession(creatorAdminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
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
    public int addGenre(int adminId, String sessionToken, String genreName, String genreDescription)
            throws AuthenticationException, InvalidInputException {

        // ✅ CRITICAL: Validate session token BEFORE processing
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        // Validate input
        if (genreName == null || genreName.trim().isEmpty()) {
            throw new InvalidInputException("Genre name cannot be empty", "genreName");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if genre exists
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_name = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, genreName.trim());
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                throw new InvalidInputException("Genre already exists", "genreName");
            }

            // Get next ID
            int nextGenreId = getLatestGenreId() + 1;

            // Insert genre
            String query = "INSERT INTO genres (genre_id, genre_name, genre_description, created_at, updated_at) " +
                    "VALUES (?, ?, ?, NOW(), NOW())";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, nextGenreId);
            stmt.setString(2, genreName.trim());
            stmt.setString(3, genreDescription.trim());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Genre created: " + genreName + " (ID: " + nextGenreId + ") by admin " + adminId);
                return nextGenreId;
            } else {
                throw new InvalidInputException("Failed to create genre", "database");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new InvalidInputException("Database error: " + e.getMessage(), "database");
        }
    }

    @Override
    public void updateGenre(int adminId, String sessionToken, int genreId, String genreName, String genreDescription)
            throws AuthenticationException {

        // ✅ CRITICAL: Validate session token FIRST
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        if (genreName == null || genreName.trim().isEmpty()) {
            throw new AuthenticationException("Genre name cannot be empty");
        }
        if (genreDescription == null) {
            genreDescription = "";
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, genreId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new AuthenticationException("Genre not found");
            }

            String duplicateQuery = "SELECT genre_id FROM genres WHERE genre_name = ? AND genre_id != ?";
            PreparedStatement duplicateStatement = connection.prepareStatement(duplicateQuery);
            duplicateStatement.setString(1, genreName.trim());
            duplicateStatement.setInt(2, genreId);
            ResultSet duplicateRs = duplicateStatement.executeQuery();
            if (duplicateRs.next()) {
                throw new AuthenticationException("A genre with this name already exists");
            }

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
    public void deleteGenre(int adminId, String sessionToken, int genreId)
            throws AuthenticationException {

        // ✅ CRITICAL: Validate session token FIRST
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String checkQuery = "SELECT genre_id FROM genres WHERE genre_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, genreId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new AuthenticationException("Genre not found");
            }

            String usageQuery = "SELECT COUNT(*) as count FROM movie_genres WHERE genre_id = ?";
            PreparedStatement usageStatement = connection.prepareStatement(usageQuery);
            usageStatement.setInt(1, genreId);
            ResultSet usageRs = usageStatement.executeQuery();
            if (usageRs.next() && usageRs.getInt("count") > 0) {
                throw new AuthenticationException("Cannot delete genre: It is currently assigned to one or more movies");
            }

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
    public Genre[] getAllGenres(int adminId, String sessionToken)
            throws AuthenticationException {

        // ✅ CRITICAL: Validate session token FIRST
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public Genre getGenreById(int adminId, String sessionToken, int genreId)
            throws AuthenticationException {

        // ✅ CRITICAL: Validate session token FIRST
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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

    // ===== ADMIN MANAGEMENT METHODS IMPLEMENTATION =====

    @Override
    public Admin getAdminById(int adminId, String sessionToken, int requestingAdminId)
            throws AuthenticationException {

        // ✅ STEP 1: Validate session token
        if (!SessionManager.validateAdminSession(requestingAdminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
                admin.password = "";
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
    public Admin[] getAllAdmins(int adminId, String sessionToken) throws AuthenticationException {
        System.out.println("IS RUNNING");
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public Admin[] searchAdmins(int adminId, String sessionToken, String searchString) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public void updateAdminProfile(int adminId, String sessionToken, String fullName, String phoneNumber) throws AuthenticationException {
        // NOTE: adminId here is the admin BEING UPDATED, not necessarily the requester
        // In a production system, you'd want to verify the requester has permission

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }
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
    public void updateAdminEmail(int adminId, String sessionToken, String newEmail, String password)
            throws AuthenticationException, InvalidInputException {
        // adminId is the admin BEING UPDATED

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public void changeAdminPassword(int adminId, String sessionToken, String oldPassword, String newPassword)
            throws AuthenticationException {
        // adminId is the admin BEING UPDATED

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public void deactivateAdmin(int superAdminId, String sessionToken, int targetAdminId, String reason)
            throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(superAdminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        // 2. Prevent self-deactivation
        if (superAdminId == targetAdminId) {
            throw new AuthenticationException("Cannot deactivate your own account");
        }

        // 3. Log the deactivation
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Create audit log
            System.out.println("🔒 Admin " + superAdminId + " deactivated Admin " + targetAdminId +
                    " | Reason: " + reason);
            // Remove active session
            SessionManager.removeAdminSession(targetAdminId);

            System.out.println("Super Admin " + superAdminId + " deactivated Admin " + targetAdminId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAdmin(int superAdminId, String sessionToken, int targetAdminId) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(superAdminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    // ===== MOVIE METHODS - CORBA EXCEPTIONS (NO PARAMETERS) =====

    @Override
    public int addMovie(int adminId, String sessionToken, String movieTitle, int[] genreIds,
                        String movieDescription, int movieRating, String moviePosterURL)
            throws AuthenticationException, InvalidInputException {

        // ✅ CRITICAL: Validate session token FIRST
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        if (movieTitle == null || movieTitle.trim().isEmpty()) {
            throw new InvalidInputException("Movie title cannot be empty", "Movie title is required");
        }
        if (genreIds == null || genreIds.length == 0) {
            throw new InvalidInputException("At least one genre is required", "Please select at least one genre");
        }
        if (movieRating < 1 || movieRating > 10) {
            throw new InvalidInputException("Invalid rating", "Rating must be between 1 and 10");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            int nextMovieId = getLatestMovieId() + 1;
            LocalDateTime timeOfCreation = LocalDateTime.now();

            String dbPosterPath = processAndSaveImage(moviePosterURL);

            String query = "INSERT INTO movies (movie_id, movie_title, movie_description, movie_rating, movie_poster_url, created_by_admin, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextMovieId);
            statement.setString(2, movieTitle.trim());
            statement.setString(3, movieDescription != null ? movieDescription.trim() : "");
            statement.setInt(4, movieRating);
            statement.setString(5, dbPosterPath);
            statement.setInt(6, adminId);
            statement.setString(7, timeOfCreation.toString());
            statement.setString(8, timeOfCreation.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                String genreQuery = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
                PreparedStatement genreStatement = connection.prepareStatement(genreQuery);

                for (int genreId : genreIds) {
                    genreStatement.setInt(1, nextMovieId);
                    genreStatement.setInt(2, genreId);
                    genreStatement.addBatch();
                }

                genreStatement.executeBatch();

                System.out.println("Successfully created movie: " + movieTitle + " with ID: " + nextMovieId);
                return nextMovieId;
            } else {
                throw new InvalidInputException("Failed to create movie", "Database insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new InvalidInputException("Database Error", "Failed to create movie: " + e.getMessage());
        } catch (IOException e) {
            throw new InvalidInputException("Image Processing Error", "Failed to save movie poster: " + e.getMessage());
        }
    }

    @Override
    public void updateMovieTitle(int adminId, String sessionToken, int movieId, String newTitle)
            throws AuthenticationException, MovieNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new AuthenticationException("Movie title cannot be empty");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new MovieNotFoundException();
            }

            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE movies SET movie_title = ?, updated_at = ? WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newTitle.trim());
            statement.setString(2, timeOfUpdate.toString());
            statement.setInt(3, movieId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Successfully updated movie title for ID: " + movieId);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }


    @Override
    public void updateMovieGenres(int adminId, String sessionToken, int movieId, int[] newGenreIds)
            throws AuthenticationException, MovieNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        if (newGenreIds == null || newGenreIds.length == 0) {
            throw new AuthenticationException("At least one genre is required");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                throw new MovieNotFoundException();
            }

            String deleteQuery = "DELETE FROM movie_genres WHERE movie_id = ?";
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setInt(1, movieId);
            deleteStatement.executeUpdate();

            String insertQuery = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);

            for (int genreId : newGenreIds) {
                insertStatement.setInt(1, movieId);
                insertStatement.setInt(2, genreId);
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();

            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String updateQuery = "UPDATE movies SET updated_at = ? WHERE movie_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, timeOfUpdate.toString());
            updateStatement.setInt(2, movieId);
            updateStatement.executeUpdate();

            System.out.println("Successfully updated genres for movie ID: " + movieId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void addGenreToMovie(int adminId, String sessionToken, int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkMovieQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkMovieStatement = connection.prepareStatement(checkMovieQuery);
            checkMovieStatement.setInt(1, movieId);
            ResultSet checkMovieRs = checkMovieStatement.executeQuery();
            if (!checkMovieRs.next()) {
                System.err.println("Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            // Check if genre already exists for this movie
            String checkGenreQuery = "SELECT * FROM movie_genres WHERE movie_id = ? AND genre_id = ?";
            PreparedStatement checkGenreStatement = connection.prepareStatement(checkGenreQuery);
            checkGenreStatement.setInt(1, movieId);
            checkGenreStatement.setInt(2, genreId);
            ResultSet checkGenreRs = checkGenreStatement.executeQuery();

            if (checkGenreRs.next()) {
                System.out.println("Genre already exists for this movie");
                return;
            }

            // Insert genre
            String query = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            statement.setInt(2, genreId);
            statement.executeUpdate();

            // Update movie timestamp
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String updateQuery = "UPDATE movies SET updated_at = ? WHERE movie_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, timeOfUpdate.toString());
            updateStatement.setInt(2, movieId);
            updateStatement.executeUpdate();

            System.out.println("Successfully added genre to movie ID: " + movieId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void removeGenreFromMovie(int adminId, String sessionToken, int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            // Delete genre
            String query = "DELETE FROM movie_genres WHERE movie_id = ? AND genre_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            statement.setInt(2, genreId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Genre not found for this movie");
                return;
            }

            // Update movie timestamp
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String updateQuery = "UPDATE movies SET updated_at = ? WHERE movie_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, timeOfUpdate.toString());
            updateStatement.setInt(2, movieId);
            updateStatement.executeUpdate();

            System.out.println("Successfully removed genre from movie ID: " + movieId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Genre[] getMovieGenres(int adminId, String sessionToken, int movieId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            // Get genres
            String query = "SELECT g.* FROM genres g " +
                    "INNER JOIN movie_genres mg ON g.genre_id = mg.genre_id " +
                    "WHERE mg.movie_id = ? ORDER BY g.genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
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
    public void updateMovieDescription(int adminId, String sessionToken, int movieId, String newDescription)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            // Update description
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE movies SET movie_description = ?, updated_at = ? WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newDescription != null ? newDescription.trim() : "");
            statement.setString(2, timeOfUpdate.toString());
            statement.setInt(3, movieId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated movie description for ID: " + movieId);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void updateMovieRating(int adminId, String sessionToken, int movieId, int newRating)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        // Validate rating
        if (newRating < 1 || newRating > 10) {
            throw new AuthenticationException("Rating must be between 1 and 10");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, movieId);
            ResultSet checkRs = checkStatement.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            // Update rating
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE movies SET movie_rating = ?, updated_at = ? WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, newRating);
            statement.setString(2, timeOfUpdate.toString());
            statement.setInt(3, movieId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated movie rating for ID: " + movieId);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void updateMoviePoster(int adminId, String sessionToken, int movieId, String newPosterURL)
            throws AuthenticationException, MovieNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // ✅ STEP 1: Get the OLD poster URL BEFORE updating
            String getOldPosterQuery = "SELECT movie_poster_url FROM movies WHERE movie_id = ?";
            PreparedStatement getOldPosterStmt = connection.prepareStatement(getOldPosterQuery);
            getOldPosterStmt.setInt(1, movieId);
            ResultSet oldPosterRs = getOldPosterStmt.executeQuery();

            String oldPosterUrl = null;
            if (oldPosterRs.next()) {
                oldPosterUrl = oldPosterRs.getString("movie_poster_url");
            } else {
                throw new MovieNotFoundException();
            }

            // ✅ STEP 2: Process and save the NEW image
            String posterPathToStore = newPosterURL;

            if (newPosterURL != null && newPosterURL.contains("|||IMAGE_DATA|||")) {
                try {
                    posterPathToStore = processAndSaveImage(newPosterURL);
                    System.out.println("✅ Processed and saved new image: " + posterPathToStore);
                } catch (IOException e) {
                    System.err.println("❌ Failed to process image: " + e.getMessage());
                    e.printStackTrace();
                    throw new AuthenticationException("Failed to process image: " + e.getMessage());
                }
            }

            // ✅ STEP 3: Update database with new poster URL
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE movies SET movie_poster_url = ?, updated_at = ? WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, posterPathToStore);
            statement.setString(2, timeOfUpdate.toString());
            statement.setInt(3, movieId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // ✅ STEP 4: Delete old image AFTER successful database update
                if (oldPosterUrl != null && !oldPosterUrl.isEmpty()
                        && !oldPosterUrl.equals(posterPathToStore)) {
                    deleteImageFile(oldPosterUrl);
                }

                System.out.println("✅ Successfully updated movie poster for ID: " + movieId);
                System.out.println("   Old: " + oldPosterUrl);
                System.out.println("   New: " + posterPathToStore);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteMovie(int adminId, String sessionToken, int movieId)
            throws AuthenticationException, MovieNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // ✅ START TRANSACTION

            // 1️⃣ Check if movie exists AND get poster URL
            String checkMovieQuery = "SELECT movie_id, movie_poster_url FROM movies WHERE movie_id = ?";
            PreparedStatement checkMovieStmt = connection.prepareStatement(checkMovieQuery);
            checkMovieStmt.setInt(1, movieId);
            ResultSet movieRs = checkMovieStmt.executeQuery();

            String posterUrl = null;
            if (!movieRs.next()) {
                connection.rollback();
                throw new MovieNotFoundException();
            } else {
                posterUrl = movieRs.getString("movie_poster_url"); // ✅ Store poster URL
            }

            // 2️⃣ Get all schedules AND their movie_schedule_ids
            String getSchedulesQuery = "SELECT schedule_id, movie_schedule_id FROM schedules WHERE movie_id = ?";
            PreparedStatement getSchedulesStmt = connection.prepareStatement(getSchedulesQuery);
            getSchedulesStmt.setInt(1, movieId);
            ResultSet schedulesRs = getSchedulesStmt.executeQuery();

            List<Integer> scheduleIds = new ArrayList<>();
            List<Integer> movieScheduleIds = new ArrayList<>();

            while (schedulesRs.next()) {
                scheduleIds.add(schedulesRs.getInt("schedule_id"));
                movieScheduleIds.add(schedulesRs.getInt("movie_schedule_id"));
            }

            // 3️⃣ Check for confirmed bookings
            if (!scheduleIds.isEmpty()) {
                String checkBookingsQuery = "SELECT COUNT(*) as booking_count FROM bookings " +
                        "WHERE schedule_id IN (" +
                        scheduleIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) +
                        ") AND booking_status = 'confirmed'";
                PreparedStatement checkBookingsStmt = connection.prepareStatement(checkBookingsQuery);
                ResultSet bookingRs = checkBookingsStmt.executeQuery();

                if (bookingRs.next() && bookingRs.getInt("booking_count") > 0) {
                    connection.rollback();
                    throw new AuthenticationException("Cannot delete movie: There are confirmed bookings");
                }
            }

            // 4️⃣ Delete schedule_list_seats
            if (!scheduleIds.isEmpty()) {
                String deleteScheduleSeatsQuery = "DELETE FROM schedule_list_seats WHERE schedule_id IN (" +
                        scheduleIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) + ")";
                PreparedStatement deleteScheduleSeatsStmt = connection.prepareStatement(deleteScheduleSeatsQuery);
                int seatsDeleted = deleteScheduleSeatsStmt.executeUpdate();
                System.out.println("🗑️ Deleted " + seatsDeleted + " schedule_list_seats");
            }

            // 5️⃣ Delete bookings
            String deleteBookingsQuery = "DELETE FROM bookings WHERE schedule_id IN " +
                    "(SELECT schedule_id FROM schedules WHERE movie_id = ?)";
            PreparedStatement deleteBookingsStmt = connection.prepareStatement(deleteBookingsQuery);
            deleteBookingsStmt.setInt(1, movieId);
            int bookingsDeleted = deleteBookingsStmt.executeUpdate();
            System.out.println("🗑️ Deleted " + bookingsDeleted + " bookings");

            // 6️⃣ Delete schedules
            String deleteSchedulesQuery = "DELETE FROM schedules WHERE movie_id = ?";
            PreparedStatement deleteSchedulesStmt = connection.prepareStatement(deleteSchedulesQuery);
            deleteSchedulesStmt.setInt(1, movieId);
            int schedulesDeleted = deleteSchedulesStmt.executeUpdate();
            System.out.println("🗑️ Deleted " + schedulesDeleted + " schedules");

            // 7️⃣ Delete movie_schedules
            if (!movieScheduleIds.isEmpty()) {
                String deleteMovieSchedulesQuery = "DELETE FROM movie_schedules WHERE movie_schedule_id IN (" +
                        movieScheduleIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) + ")";
                PreparedStatement deleteMovieSchedulesStmt = connection.prepareStatement(deleteMovieSchedulesQuery);
                int movieSchedulesDeleted = deleteMovieSchedulesStmt.executeUpdate();
                System.out.println("🗑️ Deleted " + movieSchedulesDeleted + " movie_schedules");
            }

            // 8️⃣ Delete movie_genres
            String deleteGenresQuery = "DELETE FROM movie_genres WHERE movie_id = ?";
            PreparedStatement deleteGenresStmt = connection.prepareStatement(deleteGenresQuery);
            deleteGenresStmt.setInt(1, movieId);
            int genresDeleted = deleteGenresStmt.executeUpdate();
            System.out.println("🗑️ Deleted " + genresDeleted + " movie_genres");

            // 9️⃣ Delete reviews
            String deleteReviewsQuery = "DELETE FROM reviews WHERE movie_id = ?";
            PreparedStatement deleteReviewsStmt = connection.prepareStatement(deleteReviewsQuery);
            deleteReviewsStmt.setInt(1, movieId);
            int reviewsDeleted = deleteReviewsStmt.executeUpdate();
            System.out.println("🗑️ Deleted " + reviewsDeleted + " reviews");

            // 🔟 Delete the movie
            String deleteMovieQuery = "DELETE FROM movies WHERE movie_id = ?";
            PreparedStatement deleteMovieStmt = connection.prepareStatement(deleteMovieQuery);
            deleteMovieStmt.setInt(1, movieId);
            int movieDeleted = deleteMovieStmt.executeUpdate();

            if (movieDeleted == 0) {
                connection.rollback();
                throw new MovieNotFoundException();
            }

            connection.commit(); // ✅ COMMIT TRANSACTION

            // ✅ Delete image file AFTER successful database deletion
            if (posterUrl != null && !posterUrl.isEmpty()) {
                deleteImageFile(posterUrl);
            }

            System.out.println("✅ Successfully deleted movie " + movieId + " and its poster");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
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

    private void deleteImageFile(String posterUrl) {
        try {
            // Try both locations (project root and resources)
            java.nio.file.Path projectRootPath = java.nio.file.Paths.get(posterUrl);
            java.nio.file.Path resourcesPath = java.nio.file.Paths.get("src/main/resources/" + posterUrl);

            boolean deleted = false;

            if (java.nio.file.Files.exists(projectRootPath)) {
                java.nio.file.Files.delete(projectRootPath);
                System.out.println("🗑️ Deleted image from project root: " + posterUrl);
                deleted = true;
            }

            if (java.nio.file.Files.exists(resourcesPath)) {
                java.nio.file.Files.delete(resourcesPath);
                System.out.println("🗑️ Deleted image from resources: " + posterUrl);
                deleted = true;
            }

            if (!deleted) {
                System.out.println("ℹ️Image file not found (may have been deleted already): " + posterUrl);
            }

        } catch (IOException e) {
            System.err.println("⚠️ Failed to delete image file: " + posterUrl);
            System.err.println("   Error: " + e.getMessage());
            // Don't throw exception - movie is already deleted from DB
        }
    }

    @Override
    public Movie[] getAllMovies(int adminId, String sessionToken) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies ORDER BY created_at DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            java.util.List<Movie> movieList = new java.util.ArrayList<>();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");

                // JUST return the path - NO image data embedding
                movie.moviePosterURL = rs.getString("movie_poster_url");

                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");

                // REMOVE THIS LINE (or comment it out):
                // movie = attachImageDataToMovie(movie);

                movieList.add(movie);
            }

            return movieList.toArray(new Movie[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    // Helper method to get latest movie ID
    private int getLatestMovieId() throws SQLException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT MAX(movie_id) as max_id FROM movies";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return maxId > 0 ? maxId : 0;
            }
            return 0;
        }
    }

    // ===== MOVIE SCHEDULES METHODS =====
    @Override
    public int createMovieSchedule(int adminId, String sessionToken, String screeningDate, String screeningTime)
            throws AuthenticationException, InvalidInputException {

        System.out.println("🎬 createMovieSchedule called - Date: " + screeningDate + ", Time: " + screeningTime);

        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        // Validate inputs
        if (screeningDate == null || screeningDate.trim().isEmpty()) {
            throw new InvalidInputException("Screening date cannot be empty", "date");
        }
        if (screeningTime == null || screeningTime.trim().isEmpty()) {
            throw new InvalidInputException("Screening time cannot be empty", "time");
        }

        // Validate date format (YYYY-MM-DD)
        if (!screeningDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new InvalidInputException("Invalid date format. Use YYYY-MM-DD", "date");
        }

        // Validate time format (HH:mm or HH:MM)
        if (!screeningTime.matches("\\d{2}:\\d{2}")) {
            throw new InvalidInputException("Invalid time format. Use HH:mm", "time");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            // Check if this time slot is already occupied
            String checkOccupiedQuery =
                    "SELECT ms.movie_schedule_id, COUNT(s.schedule_id) as movie_count " +
                            "FROM movie_schedules ms " +
                            "LEFT JOIN schedules s ON ms.movie_schedule_id = s.movie_schedule_id " +
                            "WHERE ms.screening_date = ? AND ms.screening_time = ? " +
                            "GROUP BY ms.movie_schedule_id";

            PreparedStatement checkOccupiedStmt = connection.prepareStatement(checkOccupiedQuery);
            checkOccupiedStmt.setString(1, screeningDate);
            checkOccupiedStmt.setString(2, screeningTime);
            ResultSet occupiedRs = checkOccupiedStmt.executeQuery();

            if (occupiedRs.next()) {
                int movieCount = occupiedRs.getInt("movie_count");
                int existingScheduleId = occupiedRs.getInt("movie_schedule_id");

                if (movieCount > 0) {
                    // Time slot already has a movie scheduled
                    connection.rollback();
                    throw new InvalidInputException(
                            "Time slot conflict: Another movie is already scheduled for " +
                                    screeningDate + " at " + screeningTime + ". " +
                                    "Please choose a different date or time.",
                            "schedule_conflict"
                    );
                } else {
                    // Time slot exists but no movie assigned - can reuse it
                    connection.commit();
                    System.out.println("♻️ Reusing empty time slot ID: " + existingScheduleId);
                    return existingScheduleId;
                }
            }

            // Time slot doesn't exist - create new one
            String maxIdQuery = "SELECT MAX(movie_schedule_id) as max_id FROM movie_schedules";
            PreparedStatement maxIdStmt = connection.prepareStatement(maxIdQuery);
            ResultSet maxIdRs = maxIdStmt.executeQuery();

            int nextScheduleId = 1;
            if (maxIdRs.next()) {
                int maxId = maxIdRs.getInt("max_id");
                nextScheduleId = maxId + 1;
            }

            // Insert new movie_schedule
            LocalDateTime now = LocalDateTime.now();
            String query = "INSERT INTO movie_schedules " +
                    "(movie_schedule_id, screening_date, screening_time, created_by_admin, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextScheduleId);
            statement.setString(2, screeningDate);
            statement.setString(3, screeningTime);
            statement.setInt(4, adminId);
            statement.setString(5, now.toString());
            statement.setString(6, now.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit(); //COMMIT TRANSACTION
                System.out.println(" Created movie schedule ID: " + nextScheduleId +
                        " for " + screeningDate + " at " + screeningTime);
                return nextScheduleId;
            } else {
                connection.rollback();
                throw new InvalidInputException("Failed to create movie schedule", "database");
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new InvalidInputException("Database error: " + e.getMessage(), "database");
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
    public int assignMovieToSchedule(int adminId, String sessionToken, int movieId, int movieScheduleId)
            throws AuthenticationException, MovieNotFoundException, ScheduleNotFoundException {

        System.out.println("🎬 assignMovieToSchedule called - Movie: " + movieId +
                ", MovieSchedule: " + movieScheduleId);

        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            // Check if movie exists
            String checkMovieQuery = "SELECT movie_id, movie_title FROM movies WHERE movie_id = ?";
            PreparedStatement checkMovieStmt = connection.prepareStatement(checkMovieQuery);
            checkMovieStmt.setInt(1, movieId);
            ResultSet movieRs = checkMovieStmt.executeQuery();

            if (!movieRs.next()) {
                connection.rollback();
                System.err.println("❌ Movie not found: " + movieId);
                throw new MovieNotFoundException();
            }

            String movieTitle = movieRs.getString("movie_title");
            System.out.println("✅ Movie found: " + movieTitle);

            //  Check if movie_schedule exists
            String checkScheduleQuery = "SELECT movie_schedule_id, screening_date, screening_time " +
                    "FROM movie_schedules WHERE movie_schedule_id = ?";
            PreparedStatement checkScheduleStmt = connection.prepareStatement(checkScheduleQuery);
            checkScheduleStmt.setInt(1, movieScheduleId);
            ResultSet scheduleRs = checkScheduleStmt.executeQuery();

            if (!scheduleRs.next()) {
                connection.rollback();
                System.err.println("❌ Movie schedule not found: " + movieScheduleId);
                throw new ScheduleNotFoundException();
            }

            String date = scheduleRs.getString("screening_date");
            String time = scheduleRs.getString("screening_time");
            System.out.println("Movie schedule found: " + date + " " + time);

            // Check if this EXACT movie is already scheduled at this time
            String checkSameMovieQuery = "SELECT schedule_id FROM schedules " +
                    "WHERE movie_id = ? AND movie_schedule_id = ?";
            PreparedStatement checkSameMovieStmt = connection.prepareStatement(checkSameMovieQuery);
            checkSameMovieStmt.setInt(1, movieId);
            checkSameMovieStmt.setInt(2, movieScheduleId);
            ResultSet sameMovieRs = checkSameMovieStmt.executeQuery();

            if (sameMovieRs.next()) {
                int existingScheduleId = sameMovieRs.getInt("schedule_id");
                connection.rollback();

                // FIXED: Use AuthenticationException instead of InvalidInputException
                throw new AuthenticationException(
                        "Schedule already exists: Movie '" + movieTitle + "' is already scheduled for " +
                                date + " at " + time + "."
                );
            }

            // NEW: Check if ANY OTHER movie is scheduled at this time
            String checkOtherMovieQuery =
                    "SELECT s.schedule_id, m.movie_title " +
                            "FROM schedules s " +
                            "JOIN movies m ON s.movie_id = m.movie_id " +
                            "WHERE s.movie_schedule_id = ? AND s.movie_id != ?";

            PreparedStatement checkOtherMovieStmt = connection.prepareStatement(checkOtherMovieQuery);
            checkOtherMovieStmt.setInt(1, movieScheduleId);
            checkOtherMovieStmt.setInt(2, movieId);
            ResultSet otherMovieRs = checkOtherMovieStmt.executeQuery();

            if (otherMovieRs.next()) {
                String conflictingMovie = otherMovieRs.getString("movie_title");
                connection.rollback();

                // FIXED: Use AuthenticationException instead of InvalidInputException
                throw new AuthenticationException(
                        "Time slot conflict: Movie '" + conflictingMovie + "' is already scheduled for " +
                                date + " at " + time + ". Please choose a different time slot."
                );
            }

            //  Get next schedule_id
            String maxIdQuery = "SELECT MAX(schedule_id) as max_id FROM schedules";
            PreparedStatement maxIdStmt = connection.prepareStatement(maxIdQuery);
            ResultSet maxIdRs = maxIdStmt.executeQuery();

            int nextScheduleId = 1;
            if (maxIdRs.next()) {
                int maxId = maxIdRs.getInt("max_id");
                nextScheduleId = maxId + 1;
            }

            // Insert into schedules table
            LocalDateTime now = LocalDateTime.now();
            String query = "INSERT INTO schedules (schedule_id, movie_id, movie_schedule_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextScheduleId);
            statement.setInt(2, movieId);
            statement.setInt(3, movieScheduleId);
            statement.setString(4, now.toString());
            statement.setString(5, now.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Initialize seats for this schedule
                System.out.println("🪑 Initializing seats for schedule " + nextScheduleId + "...");
                initializeScheduleSeatsInternal(connection, nextScheduleId);

                connection.commit(); // COMMIT TRANSACTION
                System.out.println(" Assigned movie " + movieId + " ('" + movieTitle +
                        "') to schedule " + nextScheduleId);
                return nextScheduleId;
            } else {
                connection.rollback();
                throw new AuthenticationException("Failed to assign movie to schedule");
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
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


    // Helper method to initialize seats for a schedule
    private void initializeScheduleSeatsInternal(Connection connection, int scheduleId) throws SQLException {
        // Get all seats
        String getAllSeatsQuery = "SELECT seat_id FROM seats ORDER BY seat_row, seat_number";
        PreparedStatement getAllSeatsStmt = connection.prepareStatement(getAllSeatsQuery);
        ResultSet seatsRs = getAllSeatsStmt.executeQuery();

        // Get next schedule_list_seat_id
        String maxIdQuery = "SELECT MAX(schedule_list_seat_id) as max_id FROM schedule_list_seats";
        PreparedStatement maxIdStmt = connection.prepareStatement(maxIdQuery);
        ResultSet maxIdRs = maxIdStmt.executeQuery();

        int nextSeatId = 1;
        if (maxIdRs.next()) {
            int maxId = maxIdRs.getInt("max_id");
            nextSeatId = maxId + 1;
        }

        // Insert schedule_list_seats
        String insertQuery = "INSERT INTO schedule_list_seats " +
                "(schedule_list_seat_id, schedule_id, seat_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, 'available', NOW(), NOW())";
        PreparedStatement insertStmt = connection.prepareStatement(insertQuery);

        int count = 0;
        while (seatsRs.next()) {
            int seatId = seatsRs.getInt("seat_id");
            insertStmt.setInt(1, nextSeatId++);
            insertStmt.setInt(2, scheduleId);
            insertStmt.setInt(3, seatId);
            insertStmt.addBatch();
            count++;
        }

        int[] results = insertStmt.executeBatch();
        System.out.println("Initialized " + count + " seats for schedule " + scheduleId);
    }

    @Override
    public void updateScheduleDateTime(int adminId, String sessionToken, int scheduleId, String newDate, String newTime)
            throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Get movie_schedule_id from schedule
            String getQuery = "SELECT movie_schedule_id FROM schedules WHERE schedule_id = ?";
            PreparedStatement getStmt = connection.prepareStatement(getQuery);
            getStmt.setInt(1, scheduleId);
            ResultSet rs = getStmt.executeQuery();

            if (!rs.next()) {
                throw new ScheduleNotFoundException();
            }

            int movieScheduleId = rs.getInt("movie_schedule_id");

            // Update movie_schedules
            String updateQuery = "UPDATE movie_schedules SET screening_date = ?, screening_time = ?, updated_at = NOW() " +
                    "WHERE movie_schedule_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, newDate);
            updateStmt.setString(2, newTime);
            updateStmt.setInt(3, movieScheduleId);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Updated schedule " + scheduleId + " to " + newDate + " at " + newTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteSchedule(int adminId, String sessionToken, int scheduleId)
            throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            // Check if schedule exists
            String checkQuery = "SELECT schedule_id, movie_schedule_id FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, scheduleId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                connection.rollback();
                throw new ScheduleNotFoundException();
            }

            int movieScheduleId = rs.getInt("movie_schedule_id");

            //  Check if there are any bookings for this schedule
            String checkBookingsQuery = "SELECT COUNT(*) as booking_count FROM bookings WHERE schedule_id = ?";
            PreparedStatement checkBookingsStmt = connection.prepareStatement(checkBookingsQuery);
            checkBookingsStmt.setInt(1, scheduleId);
            ResultSet bookingRs = checkBookingsStmt.executeQuery();

            if (bookingRs.next() && bookingRs.getInt("booking_count") > 0) {
                connection.rollback();
                throw new AuthenticationException("Cannot delete schedule: There are existing bookings for this schedule");
            }

            //  Delete schedule_list_seats first (foreign key constraint)
            String deleteSeatsQuery = "DELETE FROM schedule_list_seats WHERE schedule_id = ?";
            PreparedStatement deleteSeatsStmt = connection.prepareStatement(deleteSeatsQuery);
            deleteSeatsStmt.setInt(1, scheduleId);
            int seatsDeleted = deleteSeatsStmt.executeUpdate();
            System.out.println("🗑️ Deleted " + seatsDeleted + " schedule_list_seats for schedule " + scheduleId);

            //  Delete the schedule
            String deleteScheduleQuery = "DELETE FROM schedules WHERE schedule_id = ?";
            PreparedStatement deleteScheduleStmt = connection.prepareStatement(deleteScheduleQuery);
            deleteScheduleStmt.setInt(1, scheduleId);
            int scheduleDeleted = deleteScheduleStmt.executeUpdate();

            if (scheduleDeleted == 0) {
                connection.rollback();
                throw new ScheduleNotFoundException();
            }

            //  Check if other schedules are using this movie_schedule_id
            String checkOtherSchedulesQuery = "SELECT COUNT(*) as count FROM schedules WHERE movie_schedule_id = ?";
            PreparedStatement checkOtherStmt = connection.prepareStatement(checkOtherSchedulesQuery);
            checkOtherStmt.setInt(1, movieScheduleId);
            ResultSet otherRs = checkOtherStmt.executeQuery();

            //  If no other schedules use this movie_schedule, delete it too
            if (otherRs.next() && otherRs.getInt("count") == 0) {
                String deleteMovieScheduleQuery = "DELETE FROM movie_schedules WHERE movie_schedule_id = ?";
                PreparedStatement deleteMovieScheduleStmt = connection.prepareStatement(deleteMovieScheduleQuery);
                deleteMovieScheduleStmt.setInt(1, movieScheduleId);
                deleteMovieScheduleStmt.executeUpdate();
                System.out.println("🗑️ Deleted movie_schedule " + movieScheduleId + " (no longer used)");
            }

            connection.commit(); // COMMIT TRANSACTION
            System.out.println("Successfully deleted schedule " + scheduleId);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new AuthenticationException("Database error while deleting schedule: " + e.getMessage());
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
    public Schedule[] getAllSchedules(int adminId, String sessionToken) throws AuthenticationException {
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT s.schedule_id, s.movie_id, s.movie_schedule_id, " +
                    "s.created_at, s.updated_at " +
                    "FROM schedules s " +
                    "ORDER BY s.schedule_id";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            List<Schedule> scheduleList = new ArrayList<>();
            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.scheduleId = rs.getInt("schedule_id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

            return scheduleList.toArray(new Schedule[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }
    @Override
    public Schedule[] getSchedulesByMovie(int adminId, String sessionToken, int movieId)
            throws AuthenticationException, MovieNotFoundException {

        // Check authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, movieId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next()) {
                throw new MovieNotFoundException();
            }

            // ✅ JOIN with movie_schedules to get screening date and time
            String query = "SELECT s.schedule_id, s.movie_id, s.movie_schedule_id, " +
                    "ms.screening_date, ms.screening_time, ms.created_by_admin, " +
                    "s.created_at, s.updated_at " +
                    "FROM schedules s " +
                    "JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                    "WHERE s.movie_id = ? " +
                    "ORDER BY ms.screening_date, ms.screening_time";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            List<Schedule> scheduleList = new ArrayList<>();
            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.scheduleId = rs.getInt("schedule_id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");

                // ✅ FIX: Populate the nested MovieSchedule object
                MovieSchedule movieSchedule = new MovieSchedule();
                movieSchedule.movieScheduleId = rs.getInt("movie_schedule_id");
                movieSchedule.screeningDate = rs.getString("screening_date");
                movieSchedule.screeningTime = rs.getString("screening_time");
                movieSchedule.createdByAdmin = rs.getInt("created_by_admin");

//                schedule.movieSchedule = movieSchedule;

                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

            System.out.println("✅ Found " + scheduleList.size() + " schedules for movie " + movieId);
            return scheduleList.toArray(new Schedule[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    // ===== SEATS MANAGEMENT METHODS =====
    @Override //do not use
    public int addSeat(int adminId, String sessionToken, String seatRow, int seatNumber, String seatType, double seatPrice) throws AuthenticationException, InvalidInputException {
        return 0;
    }

    @Override
    public void updateSeatPrice(int adminId, String sessionToken, int seatId, double newPrice) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public void updateSeatType(int adminId, String sessionToken, int seatId, String newType) throws AuthenticationException {
        // Validate seat type (only allow: regular, vip, maintenance)

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public void deleteSeat(int adminId, String sessionToken, int seatId) throws AuthenticationException {

    }

    @Override
    public Seat[] getAllSeats(int adminId, String sessionToken) throws AuthenticationException {
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public int assignSeatToSchedule(int adminId, String sessionToken, int scheduleId, int seatId, String status) throws AuthenticationException, ScheduleNotFoundException {
        return 0;
    }

    @Override
    public void initializeScheduleSeats(int adminId, String sessionToken, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {

    }

    @Override
    public void updateScheduleSeatStatus(int adminId, String sessionToken, int scheduleListSeatId, String status) throws AuthenticationException {

    }

    @Override
    public void removeScheduleSeat(int adminId, String sessionToken, int scheduleListSeatId) throws AuthenticationException {

    }

    @Override
    public ScheduleListSeat[] getAllScheduleSeats(int adminId, String sessionToken, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return new ScheduleListSeat[0];
    }

    // ===== BOOKINGS METHODS =====
    @Override
    public Booking[] getAllBookings(int adminId, String sessionToken) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY created_at DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.toArray(new Booking[0]);
    }

    @Override
    public Booking[] getBookingsByMovie(int adminId, String sessionToken, int movieId) throws AuthenticationException, MovieNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.* FROM bookings b " +
                "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE m.movie_id = ? ORDER BY b.created_at DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new Booking[0]);
    }

    @Override
    public Booking[] getBookingsBySchedule(int adminId, String sessionToken, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE schedule_id = ?";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new Booking[0]);
    }

    @Override
    public Booking[] getBookingsByStatus(int adminId, String sessionToken, String status) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE booking_status = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new Booking[0]);
    }

    @Override
    public void cancelBooking(int adminId, String sessionToken, int bookingId, String reason) throws AuthenticationException, BookingException {}

    @Override
    public void updateBookingAdmin(int adminId, String sessionToken, int bookingId, int[] newSeatIds, String status) throws AuthenticationException, BookingException {}

    @Override
    public BookingDetails getBookingDetailsView(int adminId, String sessionToken, int bookingId) throws AuthenticationException, BookingException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        String sql = "SELECT b.booking_id, b.total_price, b.booking_status, b.created_at, " +
                "c.username, c.full_name, m.movie_title, ms.screening_date, ms.screening_time " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToBookingDetails(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new BookingException();
    }

    @Override
    public BookingDetails[] getAllBookingsWithDetails(int adminId, String sessionToken) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        List<BookingDetails> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.total_price, b.booking_status, b.created_at, " +
                "c.username, c.full_name, m.movie_title, ms.screening_date, ms.screening_time " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                "ORDER BY b.created_at DESC";

        try (Connection conn = DatabaseAccess.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToBookingDetails(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new BookingDetails[0]);
    }

    @Override
    public ScheduleDetails getScheduleDetailsView(int adminId, String sessionToken, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return null;
    }

    @Override
    public ScheduleDetails[] getAllSchedulesWithDetails(int adminId, String sessionToken) throws AuthenticationException {
        return new ScheduleDetails[0];
    }

    @Override
    public PaymentDetails getPaymentDetailsView(int adminId, String sessionToken, int paymentId) throws AuthenticationException, PaymentNotFoundException {
        return null;
    }

    @Override
    public PaymentDetails[] getAllPaymentsWithDetails(int adminId, String sessionToken) throws AuthenticationException {
        return new PaymentDetails[0];
    }

    // ===== CUSTOMER MANAGEMENT METHODS IMPLEMENTATION =====

    @Override
    public Customer[] getAllCustomers(int adminId, String sessionToken) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public Customer[] searchCustomers(int adminId, String sessionToken, String searchString) throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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

    @Override
    public Customer getCustomerDetails(int adminId, String sessionToken, int customerId) throws AuthenticationException {
        return null;
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
    public void updateCustomerInfo(int adminId, String sessionToken, int customerId, String email, String phoneNumber, String fullName)
            throws AuthenticationException {
        // 1. Check Authentication
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
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
    public void deactivateCustomer(int adminId, String sessionToken, int customerId, String reason) throws AuthenticationException {

    }

    @Override
    public String generateSalesReport(int adminId, String sessionToken, String startDate, String endDate)
            throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT " +
                    "COUNT(DISTINCT b.booking_id) as total_bookings, " +
                    "COUNT(DISTINCT b.customer_id) as unique_customers, " +
                    "SUM(p.amount) as total_revenue, " +
                    "AVG(p.amount) as avg_booking_value, " +
                    "COUNT(DISTINCT s.movie_id) as movies_shown " +
                    "FROM bookings b " +
                    "JOIN payments p ON b.booking_id = p.booking_id " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                    "WHERE ms.screening_date BETWEEN ? AND ? " +
                    "AND p.payment_status = 'completed'";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StringBuilder report = new StringBuilder();
                report.append("=== SALES REPORT ===\n");
                report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
                report.append("Total Bookings: ").append(rs.getInt("total_bookings")).append("\n");
                report.append("Unique Customers: ").append(rs.getInt("unique_customers")).append("\n");
                report.append("Total Revenue: ₱").append(String.format("%.2f", rs.getDouble("total_revenue"))).append("\n");
                report.append("Average Booking Value: ₱").append(String.format("%.2f", rs.getDouble("avg_booking_value"))).append("\n");
                report.append("Movies Shown: ").append(rs.getInt("movies_shown")).append("\n");

                return report.toString();
            }

            return "No data available for the selected period.";

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    // ===== PAYMENT PROCESSING METHODS =====
    @Override
    public Payment[] getAllPayments(int adminId, String sessionToken) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public Payment getPaymentById(int adminId, String sessionToken, int paymentId) throws AuthenticationException, PaymentNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public Payment[] getPaymentsByStatus(int adminId, String sessionToken, String status) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public Payment[] getBookingPayments(int adminId, String sessionToken, int bookingId) throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

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
    public void updatePaymentStatus(int adminId, String sessionToken, int paymentId, String status)
            throws AuthenticationException, PaymentNotFoundException {

        // Validate session
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false); // ✅ START TRANSACTION

            // 1️⃣ Get the booking_id for this payment
            String getBookingQuery = "SELECT booking_id FROM payments WHERE payment_id = ?";
            PreparedStatement getBookingStmt = connection.prepareStatement(getBookingQuery);
            getBookingStmt.setInt(1, paymentId);
            ResultSet bookingRs = getBookingStmt.executeQuery();

            if (!bookingRs.next()) {
                connection.rollback();
                throw new PaymentNotFoundException("Payment not found", paymentId);
            }

            int bookingId = bookingRs.getInt("booking_id");

            // 2️⃣ Update payment status
            String updatePaymentQuery = "UPDATE payments SET payment_status = ?, updated_at = NOW() WHERE payment_id = ?";
            PreparedStatement paymentStmt = connection.prepareStatement(updatePaymentQuery);
            paymentStmt.setString(1, status);
            paymentStmt.setInt(2, paymentId);

            int paymentUpdated = paymentStmt.executeUpdate();
            if (paymentUpdated == 0) {
                connection.rollback();
                throw new PaymentNotFoundException("Payment not found", paymentId);
            }

            // 3️⃣ ✅ FIX: Update booking status based on payment status
            String newBookingStatus = null;
            String cancellationReason = null;

            if ("completed".equalsIgnoreCase(status)) {
                newBookingStatus = "confirmed";
            } else if ("failed".equalsIgnoreCase(status)) {
                newBookingStatus = "cancelled";
                cancellationReason = "Payment not received by admin";
            } else if ("refunded".equalsIgnoreCase(status)) {
                newBookingStatus = "cancelled";
                cancellationReason = "Payment refunded by admin";
            }

            // Update booking if status changed
            if (newBookingStatus != null) {
                String updateBookingQuery = "UPDATE bookings SET " +
                        "booking_status = ?, " +
                        "cancellation_reason = ?, " +
                        "updated_at = NOW() " +
                        "WHERE booking_id = ?";
                PreparedStatement bookingStmt = connection.prepareStatement(updateBookingQuery);
                bookingStmt.setString(1, newBookingStatus);
                bookingStmt.setString(2, cancellationReason);
                bookingStmt.setInt(3, bookingId);
                bookingStmt.executeUpdate();

                System.out.println("✅ Updated booking " + bookingId + " status to: " + newBookingStatus);
            }

            // 4️⃣ ✅ CRITICAL FIX: Release seats if payment failed/refunded
            if ("failed".equalsIgnoreCase(status) || "refunded".equalsIgnoreCase(status)) {
                String releaseSeatsQuery = "UPDATE schedule_list_seats SET " +
                        "status = 'available', " +
                        "booking_id = NULL, " +
                        "updated_at = NOW() " +
                        "WHERE booking_id = ?";
                PreparedStatement releaseStmt = connection.prepareStatement(releaseSeatsQuery);
                releaseStmt.setInt(1, bookingId);
                int seatsReleased = releaseStmt.executeUpdate();

                System.out.println("✅ Released " + seatsReleased + " seats back to available");
            }

            connection.commit(); // ✅ COMMIT TRANSACTION
            System.out.println("✅ Payment " + paymentId + " updated to: " + status);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
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
    public void refundPayment(int adminId, String sessionToken, int paymentId, String reason)
            throws AuthenticationException, PaymentNotFoundException, PaymentException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        Connection connection = null;
        try {
            connection = DatabaseAccess.getConnection();
            connection.setAutoCommit(false);

            // Get booking info
            String getBookingQuery = "SELECT p.booking_id, p.payment_status FROM payments p " +
                    "WHERE p.payment_id = ?";
            PreparedStatement getBookingStmt = connection.prepareStatement(getBookingQuery);
            getBookingStmt.setInt(1, paymentId);
            ResultSet rs = getBookingStmt.executeQuery();

            if (!rs.next()) {
                connection.rollback();
                throw new PaymentNotFoundException("Payment not found", paymentId);
            }

            int bookingId = rs.getInt("booking_id");
            String currentStatus = rs.getString("payment_status");

            // Check if already refunded
            if ("refunded".equals(currentStatus)) {
                connection.rollback();
                throw new PaymentException("Payment already refunded", 400);
            }

            // Update payment
            String updatePayment = "UPDATE payments SET " +
                    "payment_status = 'refunded', " +
                    "refund_reason = ?, " +
                    "updated_at = NOW() " +
                    "WHERE payment_id = ?";
            PreparedStatement paymentStmt = connection.prepareStatement(updatePayment);
            paymentStmt.setString(1, reason);
            paymentStmt.setInt(2, paymentId);
            paymentStmt.executeUpdate();

            // Update booking
            String updateBooking = "UPDATE bookings SET " +
                    "booking_status = 'cancelled', " +
                    "cancellation_reason = ?, " +
                    "updated_at = NOW() " +
                    "WHERE booking_id = ?";
            PreparedStatement bookingStmt = connection.prepareStatement(updateBooking);
            bookingStmt.setString(1, "Refunded: " + reason);
            bookingStmt.setInt(2, bookingId);
            bookingStmt.executeUpdate();

            // ✅ Release seats
            String releaseSeats = "UPDATE schedule_list_seats SET " +
                    "status = 'available', " +
                    "booking_id = NULL, " +
                    "updated_at = NOW() " +
                    "WHERE booking_id = ?";
            PreparedStatement releaseStmt = connection.prepareStatement(releaseSeats);
            releaseStmt.setInt(1, bookingId);
            int seatsReleased = releaseStmt.executeUpdate();

            connection.commit();
            System.out.println("✅ Payment " + paymentId + " refunded - " + seatsReleased + " seats released");

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

    // ===== ADDITIONAL METHODS =====

    // In MBAdminServant.java
    @Override
    public double getMovieRevenue(int adminId, String sessionToken, int movieId) throws AuthenticationException, MovieNotFoundException {
        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }
        if (movieId == 0) {
            return getTotalSystemRevenue(adminId);
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Step 1: Check if the movie exists (if it doesn't, we throw MovieNotFoundException)
            String checkMovieQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkMovieStatement = connection.prepareStatement(checkMovieQuery);
            checkMovieStatement.setInt(1, movieId);
            if (!checkMovieStatement.executeQuery().next()) {
                throw new MovieNotFoundException();
            }

            // Step 2: Calculate Revenue
            String query = "SELECT COALESCE(SUM(p.amount), 0.0) AS CalculatedRevenue FROM payments p " + // 🆕 Use COALESCE for guaranteed non-null result
                    "JOIN bookings b ON p.booking_id = b.booking_id " +
                    "JOIN schedules s ON b.schedule_id = s.schedule_id " +
                    "WHERE s.movie_id = ? AND p.payment_status = 'completed'";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // 🆕 Use the new robust alias "CalculatedRevenue"
                double revenue = rs.getDouble("CalculatedRevenue");
                return revenue;
            }

            // This line should technically never be hit due to COALESCE, but kept for safety
            return 0.0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    public double getTotalSystemRevenue(int adminId) throws AuthenticationException {
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT SUM(amount) AS TotalRevenue FROM payments WHERE payment_status = 'completed'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalRevenue");
            }
            return 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    public double getScheduleOccupancy(int adminId, String sessionToken, int scheduleId)
            throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if schedule exists
            String checkQuery = "SELECT schedule_id FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, scheduleId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (!checkRs.next()) {
                throw new ScheduleNotFoundException("Schedule not found", scheduleId);
            }

            // Calculate occupancy
            String query = "SELECT " +
                    "COUNT(*) as total_seats, " +
                    "SUM(CASE WHEN status = 'booked' THEN 1 ELSE 0 END) as booked_seats " +
                    "FROM schedule_list_seats WHERE schedule_id = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_seats");
                int booked = rs.getInt("booked_seats");

                if (total == 0) return 0.0;

                double occupancy = (double) booked / total * 100.0;
                return Math.round(occupancy * 100.0) / 100.0; // Round to 2 decimals
            }

            return 0.0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Movie[] getPopularMovies(int adminId, String sessionToken, int top)
            throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT m.*, COUNT(b.booking_id) as booking_count " +
                    "FROM movies m " +
                    "LEFT JOIN schedules s ON m.movie_id = s.movie_id " +
                    "LEFT JOIN bookings b ON s.schedule_id = b.schedule_id " +
                    "WHERE b.booking_status = 'confirmed' " +
                    "GROUP BY m.movie_id " +
                    "ORDER BY booking_count DESC " +
                    "LIMIT ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, top);
            ResultSet rs = stmt.executeQuery();

            List<Movie> movieList = new ArrayList<>();
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

            return movieList.toArray(new Movie[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Movie[] getMoviesByGenre(int adminId, String sessionToken, int genreId)
            throws AuthenticationException {

        if (!SessionManager.validateAdminSession(adminId, sessionToken)) {
            throw new AuthenticationException("Invalid session - please log in again");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT DISTINCT m.* FROM movies m " +
                    "INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                    "WHERE mg.genre_id = ? " +
                    "ORDER BY m.movie_title";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, genreId);
            ResultSet rs = stmt.executeQuery();

            List<Movie> movieList = new ArrayList<>();
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

            return movieList.toArray(new Movie[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
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

    private int getLatestGenreId() throws SQLException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT MAX(genre_id) as max_id FROM genres";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return rs.wasNull() ? 0 : maxId;
            }
            return 0;
        }
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
    //helper for booking
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();

        b.bookingId = rs.getInt("booking_id");
        b.customerId = rs.getInt("customer_id");
        b.scheduleId = rs.getInt("schedule_id");

        try { b.totalPrice = rs.getDouble("total_price"); } catch (Exception e) { b.totalPrice = 0.0; }

        b.scheduleListSeatIds = new int[0];

        // ✅ CRITICAL: Use safeString helper
        b.bookingStatus = safeString(rs.getString("booking_status"));
        b.bookingDate = safeString(rs.getString("booking_date"));
        b.cancellationReason = safeString(rs.getString("cancellation_reason"));
        b.createdAt = safeString(rs.getString("created_at"));
        b.updatedAt = safeString(rs.getString("updated_at"));

        return b;
    }

    //helper for booking
    private BookingDetails mapResultSetToBookingDetails(ResultSet rs) throws SQLException {
        BookingDetails bd = new BookingDetails();

        bd.bookingId = rs.getInt("booking_id");
        bd.totalPrice = rs.getDouble("total_price");

        String status = rs.getString("booking_status");
        bd.bookingStatus = (status != null) ? status : "Unknown";

        String custName = rs.getString("full_name");
        bd.customerName = (custName != null) ? custName : "Guest";

        String movieTitle = rs.getString("movie_title");
        bd.movieTitle = (movieTitle != null) ? movieTitle : "Untitled";

        String sDate = rs.getString("screening_date");
        bd.screeningDate = (sDate != null) ? sDate : "N/A";

        String sTime = rs.getString("screening_time");
        bd.screeningTime = (sTime != null) ? sTime : "N/A";

        String created = rs.getString("created_at");
        bd.bookingDate = (created != null) ? created : "N/A";

        return bd;
    }

    private String processAndSaveImage(String concatenatedPosterURL) throws IOException {
        if (concatenatedPosterURL == null || concatenatedPosterURL.isEmpty()) {
            return "";
        }

        if (!concatenatedPosterURL.contains("|||IMAGE_DATA|||")) {
            return concatenatedPosterURL;
        }

        String[] parts = concatenatedPosterURL.split("\\|\\|\\|IMAGE_DATA\\|\\|\\|");
        if (parts.length != 2) {
            throw new IOException("Invalid image data format");
        }

        String targetPath = parts[0];
        String base64Data = parts[1];

        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        // ✅ Save to BOTH locations to ensure compatibility

        // Location 1: Project root (for current loading mechanism)
        java.nio.file.Path projectRootDir = java.nio.file.Paths.get("Images/MovieImage");
        java.nio.file.Files.createDirectories(projectRootDir);
        java.nio.file.Path projectRootFile = java.nio.file.Paths.get(targetPath);
        java.nio.file.Files.write(projectRootFile, imageBytes);
        System.out.println("✅ Image saved to PROJECT ROOT: " + projectRootFile.toAbsolutePath());

        // Location 2: Resources folder (for production deployment)
        java.nio.file.Path resourcesDir = java.nio.file.Paths.get("src/main/resources/Images/MovieImage");
        java.nio.file.Files.createDirectories(resourcesDir);
        java.nio.file.Path resourcesFile = java.nio.file.Paths.get("src/main/resources/" + targetPath);
        java.nio.file.Files.write(resourcesFile, imageBytes);
        System.out.println("✅ Image saved to RESOURCES: " + resourcesFile.toAbsolutePath());

        return targetPath;
    }

    // Add this helper to MBAdminServant if it doesn't exist
    private String safeString(String input) {
        return (input == null) ? "" : input;
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
}