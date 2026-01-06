package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class AdminMgmtModel extends AdminParentModel {

    public AdminMgmtModel(int adminId, String sessionToken) {
        this.adminId = adminId;
        this.sessionToken = sessionToken;
    }


    // ===== DATA OPERATIONS =====

    public Admin[] loadAllAdmins() throws AuthenticationException {
        return adminServices.getAllAdmins(adminId,  sessionToken);
    }

    public Admin[] searchAdmins(String searchString) throws AuthenticationException {
        return adminServices.searchAdmins(adminId, sessionToken, searchString.trim());
    }

    public Admin getAdminById(String requestedAdminId) throws AuthenticationException {
        return adminServices.getAdminById(Integer.parseInt(requestedAdminId),  sessionToken, adminId);
    }

    public int createAdmin(String username, String email, String password, String fullName, String phoneNumber)
            throws AuthenticationException, InvalidInputException {

        // Validate all inputs
        String error = validateAdminCreation(username, email, password, fullName, phoneNumber);
        if (error != null) {
            throw new InvalidInputException(error, "validation");
        }

        return adminServices.createAdmin(adminId, sessionToken, username.trim(), email.trim(), password,
                fullName.trim(), phoneNumber.trim());
    }

    public void updateAdminProfile(int targetAdminId, String fullName, String phoneNumber)
            throws AuthenticationException {

        // Validate inputs
        String error = validateProfileUpdate(fullName, phoneNumber);
        if (error != null) {
            throw new AuthenticationException(error);
        }

        adminServices.updateAdminProfile(targetAdminId, sessionToken, fullName.trim(), phoneNumber.trim());
    }

    public void updateAdminEmail(int targetAdminId, String newEmail, String password)
            throws AuthenticationException, InvalidInputException {

        // Validate inputs
        String error = validateEmailChange(newEmail, password);
        if (error != null) {
            throw new AuthenticationException(error);
        }

        adminServices.updateAdminEmail(targetAdminId, sessionToken, newEmail.trim(), password);
    }

    public void changeAdminPassword(int targetAdminId, String oldPassword, String newPassword)
            throws AuthenticationException {

        // Validate inputs
        String error = validatePasswordChange(oldPassword, newPassword);
        if (error != null) {
            throw new AuthenticationException(error);
        }

        adminServices.changeAdminPassword(targetAdminId, sessionToken, oldPassword, newPassword);
    }

    public void deleteAdmin(int targetAdminId) throws AuthenticationException {
        adminServices.deleteAdmin(adminId, sessionToken, targetAdminId);
    }

    // ===== VALIDATION METHODS =====

    /**
     * Validate search query
     * @return error message or null if valid
     */
    public String validateSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Search query cannot be empty";
        }
        if (query.trim().length() > 100) {
            return "Search query is too long (max 100 characters)";
        }
        return null;
    }

    /**
     * Validate username
     * @return error message or null if valid
     */
    public String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty";
        }

        String trimmed = username.trim();

        if (trimmed.length() < 3) {
            return "Username must be at least 3 characters";
        }
        if (trimmed.length() > 20) {
            return "Username cannot exceed 20 characters";
        }
        if (!trimmed.matches("^[A-Za-z0-9_-]+$")) {
            return "Username can only contain letters, numbers, underscore, and hyphen";
        }
        return null;
    }

    /**
     * Validate email
     * @return error message or null if valid
     */
    public String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty";
        }

        String trimmed = email.trim();

        if (!trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Invalid email format (example: user@example.com)";
        }
        if (trimmed.length() > 100) {
            return "Email is too long (max 100 characters)";
        }
        return null;
    }

    /**
     * Validate password
     * @return error message or null if valid
     */
    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (password.length() > 50) {
            return "Password is too long (max 50 characters)";
        }
        if (!password.matches(".*[A-Za-z].*")) {
            return "Password must contain at least one letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }
        return null;
    }

    /**
     * Validate full name
     * @return error message or null if valid
     */
    public String validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name cannot be empty";
        }

        String trimmed = fullName.trim();

        if (trimmed.length() < 2) {
            return "Full name must be at least 2 characters";
        }
        if (trimmed.length() > 100) {
            return "Full name is too long (max 100 characters)";
        }
        if (!trimmed.matches("^[A-Za-z\\s'-]+$")) {
            return "Full name can only contain letters, spaces, hyphens, and apostrophes";
        }
        return null;
    }

    /**
     * Validate phone number (optional field)
     * @return error message or null if valid
     */
    public String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null; // Phone is optional
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-()]", "");

        if (!cleaned.matches("^[+]?[0-9]{10,15}$")) {
            return "Invalid phone number (use 10-15 digits, optional + prefix)";
        }
        return null;
    }

    /**
     * Validate admin creation - checks all fields
     * @return error message or null if all valid
     */
    public String validateAdminCreation(String username, String email, String password,
                                        String fullName, String phoneNumber) {
        String error;

        error = validateUsername(username);
        if (error != null) return error;

        error = validateEmail(email);
        if (error != null) return error;

        error = validatePassword(password);
        if (error != null) return error;

        error = validateFullName(fullName);
        if (error != null) return error;

        error = validatePhoneNumber(phoneNumber);
        if (error != null) return error;

        return null;
    }

    /**
     * Validate profile update (no password)
     * @return error message or null if valid
     */
    public String validateProfileUpdate(String fullName, String phoneNumber) {
        String error;

        error = validateFullName(fullName);
        if (error != null) return error;

        error = validatePhoneNumber(phoneNumber);
        if (error != null) return error;

        return null;
    }

    /**
     * Validate email change
     * @return error message or null if valid
     */
    public String validateEmailChange(String newEmail, String currentPassword) {
        String error;

        error = validateEmail(newEmail);
        if (error != null) return error;

        if (currentPassword == null || currentPassword.isEmpty()) {
            return "Current password is required to change email";
        }

        return null;
    }

    /**
     * Validate password change
     * @return error message or null if valid
     */
    public String validatePasswordChange(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            return "Current password is required";
        }

        String error = validatePassword(newPassword);
        if (error != null) return error;

        if (oldPassword.equals(newPassword)) {
            return "New password must be different from current password";
        }

        return null;
    }
}
