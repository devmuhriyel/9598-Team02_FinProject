package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class CustomerMgmtModel extends AdminParentModel {
    private int adminId;

    public CustomerMgmtModel(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    // ===== DATA OPERATIONS =====

    public Customer[] loadAllCustomers() throws AuthenticationException {
        return adminServices.getAllCustomers(adminId);
    }

    public Customer[] searchCustomers(String searchString) throws AuthenticationException {
        return adminServices.searchCustomers(adminId, searchString.trim());
    }

    public Customer getCustomerDetails(String requestedCustomerId) throws AuthenticationException {
        return adminServices.getCustomerDetails(adminId, Integer.parseInt(requestedCustomerId));
    }

    public void updateCustomerInfo(int targetCustomerId, String email, String phoneNumber, String fullName)
            throws AuthenticationException {

        // Validate inputs
        String error = validateCustomerUpdate(email, fullName, phoneNumber);
        if (error != null) {
            throw new AuthenticationException(error);
        }

        adminServices.updateCustomerInfo(adminId, targetCustomerId, email.trim(), phoneNumber.trim(), fullName.trim());
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
     * Validate customer update
     * @return error message or null if all valid
     */
    public String validateCustomerUpdate(String email, String fullName, String phoneNumber) {
        String error;

        error = validateEmail(email);
        if (error != null) return error;

        error = validateFullName(fullName);
        if (error != null) return error;

        error = validatePhoneNumber(phoneNumber);
        if (error != null) return error;

        return null;
    }
}
