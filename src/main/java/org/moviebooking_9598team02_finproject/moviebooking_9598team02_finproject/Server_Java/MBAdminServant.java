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
    // ===== MOVIE METHODS - CORBA EXCEPTIONS (NO PARAMETERS) =====

    @Override
    public int addMovie(int adminId, String movieTitle, int[] genreIds, String movieDescription,
                        int movieRating, String moviePosterURL)
            throws AuthenticationException, InvalidInputException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate input
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
            // Get next movie ID
            int nextMovieId = getLatestMovieId() + 1;
            LocalDateTime timeOfCreation = LocalDateTime.now();

            // ✅ PROCESS IMAGE - extract and save
            String dbPosterPath = processAndSaveImage(moviePosterURL);

            // Insert new movie with ONLY the path
            String query = "INSERT INTO movies (movie_id, movie_title, movie_description, movie_rating, movie_poster_url, created_by_admin, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, nextMovieId);
            statement.setString(2, movieTitle.trim());
            statement.setString(3, movieDescription != null ? movieDescription.trim() : "");
            statement.setInt(4, movieRating);
            statement.setString(5, dbPosterPath); // ✅ Store ONLY path in DB
            statement.setInt(6, adminId);
            statement.setString(7, timeOfCreation.toString());
            statement.setString(8, timeOfCreation.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Insert movie_genres relationships
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
    public void updateMovieTitle(int adminId, int movieId, String newTitle)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate input
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new AuthenticationException("Movie title cannot be empty");
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

            // Update movie title
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
    public void updateMovieGenres(int adminId, int movieId, int[] newGenreIds)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate input
        if (newGenreIds == null || newGenreIds.length == 0) {
            throw new AuthenticationException("At least one genre is required");
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

            // Delete existing genres
            String deleteQuery = "DELETE FROM movie_genres WHERE movie_id = ?";
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setInt(1, movieId);
            deleteStatement.executeUpdate();

            // Insert new genres
            String insertQuery = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);

            for (int genreId : newGenreIds) {
                insertStatement.setInt(1, movieId);
                insertStatement.setInt(2, genreId);
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();

            // Update movie timestamp
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
    public void addGenreToMovie(int adminId, int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public void removeGenreFromMovie(int adminId, int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public Genre[] getMovieGenres(int adminId, int movieId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public void updateMovieDescription(int adminId, int movieId, String newDescription)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public void updateMovieRating(int adminId, int movieId, int newRating)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public void updateMoviePoster(int adminId, int movieId, String newPosterURL)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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

            // Update poster URL
            LocalDateTime timeOfUpdate = LocalDateTime.now();
            String query = "UPDATE movies SET movie_poster_url = ?, updated_at = ? WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPosterURL != null ? newPosterURL.trim() : "");
            statement.setString(2, timeOfUpdate.toString());
            statement.setInt(3, movieId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated movie poster for ID: " + movieId);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteMovie(int adminId, int movieId)
            throws AuthenticationException, MovieNotFoundException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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

            // Delete movie_genres relationships first (foreign key constraint)
            String deleteGenresQuery = "DELETE FROM movie_genres WHERE movie_id = ?";
            PreparedStatement deleteGenresStatement = connection.prepareStatement(deleteGenresQuery);
            deleteGenresStatement.setInt(1, movieId);
            deleteGenresStatement.executeUpdate();

            // Delete movie
            String query = "DELETE FROM movies WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully deleted movie ID: " + movieId);
            } else {
                throw new MovieNotFoundException();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Movie[] getAllMovies(int adminId) throws AuthenticationException {
        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
                movie.moviePosterURL = rs.getString("movie_poster_url"); // Path from DB
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");

                // ✅ ATTACH IMAGE DATA before sending to client
                movie = attachImageDataToMovie(movie);

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
    public int createMovieSchedule(int adminId, String screeningDate, String screeningTime)
            throws AuthenticationException, InvalidInputException {

        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        // Validate inputs
        if (screeningDate == null || screeningDate.trim().isEmpty()) {
            throw new InvalidInputException("Screening date cannot be empty", "date");
        }
        if (screeningTime == null || screeningTime.trim().isEmpty()) {
            throw new InvalidInputException("Screening time cannot be empty", "time");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Get next movie_schedule_id
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
            String query = "INSERT INTO movie_schedules (movie_schedule_id, screening_date, screening_time, created_by_admin, created_at, updated_at) " +
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
                System.out.println("✅ Created movie schedule ID: " + nextScheduleId + " for " + screeningDate + " at " + screeningTime);
                return nextScheduleId;
            } else {
                throw new InvalidInputException("Failed to create movie schedule", "database");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new InvalidInputException("Database error: " + e.getMessage(), "database");
        }
    }


    @Override
    public int assignMovieToSchedule(int adminId, int movieId, int movieScheduleId)
            throws AuthenticationException, MovieNotFoundException, ScheduleNotFoundException {

        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if movie exists
            String checkMovieQuery = "SELECT movie_id FROM movies WHERE movie_id = ?";
            PreparedStatement checkMovieStmt = connection.prepareStatement(checkMovieQuery);
            checkMovieStmt.setInt(1, movieId);
            ResultSet movieRs = checkMovieStmt.executeQuery();
            if (!movieRs.next()) {
                throw new MovieNotFoundException();
            }

            // Check if movie_schedule exists
            String checkScheduleQuery = "SELECT movie_schedule_id FROM movie_schedules WHERE movie_schedule_id = ?";
            PreparedStatement checkScheduleStmt = connection.prepareStatement(checkScheduleQuery);
            checkScheduleStmt.setInt(1, movieScheduleId);
            ResultSet scheduleRs = checkScheduleStmt.executeQuery();
            if (!scheduleRs.next()) {
                throw new ScheduleNotFoundException();
            }

            // Get next schedule_id
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
                initializeScheduleSeatsInternal(connection, nextScheduleId);

                System.out.println("✅ Assigned movie " + movieId + " to schedule " + nextScheduleId);
                return nextScheduleId;
            } else {
                throw new AuthenticationException("Failed to assign movie to schedule");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
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
        String insertQuery = "INSERT INTO schedule_list_seats (schedule_list_seat_id, schedule_id, seat_id, status, created_at, updated_at) " +
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
        System.out.println("✅ Initialized " + count + " seats for schedule " + scheduleId);
    }

    @Override
    public void updateScheduleDateTime(int adminId, int scheduleId, String newDate, String newTime)
            throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
                System.out.println("✅ Updated schedule " + scheduleId + " to " + newDate + " at " + newTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void deleteSchedule(int adminId, int scheduleId)
            throws AuthenticationException, ScheduleNotFoundException {

        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if schedule exists
            String checkQuery = "SELECT schedule_id FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, scheduleId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new ScheduleNotFoundException();
            }

            // Delete schedule (cascade will handle schedule_list_seats)
            String deleteQuery = "DELETE FROM schedules WHERE schedule_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, scheduleId);

            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Deleted schedule " + scheduleId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Schedule[] getAllSchedules(int adminId) throws AuthenticationException {
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public Schedule[] getSchedulesByMovie(int adminId, int movieId)
            throws AuthenticationException, MovieNotFoundException {

        // Check authentication
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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
    public Booking[] getBookingsByMovie(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
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
    public Booking[] getBookingsBySchedule(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
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
    public Booking[] getBookingsByStatus(int adminId, String status) throws AuthenticationException {
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
    public void cancelBooking(int adminId, int bookingId, String reason) throws AuthenticationException, BookingException {}

    @Override
    public void updateBookingAdmin(int adminId, int bookingId, int[] newSeatIds, String status) throws AuthenticationException, BookingException {}

    @Override
    public BookingDetails getBookingDetailsView(int adminId, int bookingId) throws AuthenticationException, BookingException {
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
    public BookingDetails[] getAllBookingsWithDetails(int adminId) throws AuthenticationException {
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

    // In MBAdminServant.java
    @Override
    public double getMovieRevenue(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        if (!SessionManager.isAdminLoggedIn(adminId)) {
            throw new AuthenticationException("Admin not logged in or session expired");
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

        // Check if it contains image data
        if (!concatenatedPosterURL.contains("|||IMAGE_DATA|||")) {
            // Already a path (for updates without new image)
            return concatenatedPosterURL;
        }

        // Split the concatenated string
        String[] parts = concatenatedPosterURL.split("\\|\\|\\|IMAGE_DATA\\|\\|\\|");
        if (parts.length != 2) {
            throw new IOException("Invalid image data format");
        }

        String targetPath = parts[0]; // e.g., "Images/MovieImage/1234567890_poster.jpg"
        String base64Data = parts[1];

        // Decode Base64
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        // Create directory if it doesn't exist
        java.nio.file.Path dirPath = java.nio.file.Paths.get("Images/MovieImage");
        java.nio.file.Files.createDirectories(dirPath);

        // Save file
        java.nio.file.Path filePath = java.nio.file.Paths.get(targetPath);
        java.nio.file.Files.write(filePath, imageBytes);

        System.out.println("✅ Image saved to: " + targetPath);
        return targetPath; // Return ONLY the path for DB storage
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