package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientServiceHelper;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

/**
 * CustomerParentModel - Base class for all customer models
 * ✅ FIXED: Now stores sessionToken AND customer profile data
 */
public abstract class CustomerParentModel {

    protected static ClientService clientServices;
    protected static CommonService commonServices;
    private static String hostIP;
    private static int hostPort;

    // ✅ Session storage - STATIC so accessible by all child models
    protected static int customerId;
    protected static String sessionToken;
    protected static String username;

    // ✅ NEW: Customer profile data - STATIC
    protected static String fullName;
    protected static String email;
    protected static String phoneNumber;

    public static void initializeCORBAConnection(String[] args) {
        try {
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialHost", hostIP);
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(hostPort));

            ORB orb = ORB.init(args, props);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            clientServices = ClientServiceHelper.narrow(ncRef.resolve_str("ClientService"));

            String commonName = "CommonService";
            commonServices = CommonServiceHelper.narrow(ncRef.resolve_str(commonName));

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize ClientService: " + e.getMessage());
            e.printStackTrace();
            clientServices = null;
        }
    }

    public boolean isConnected() {
        return clientServices != null;
    }

    public void setHostIP(String ip) {
        hostIP = ip;
    }

    public void setHostPort(int port) {
        hostPort = port;
    }

    // ✅ UPDATED: Session management methods

    /**
     * Set session credentials after login
     * Called by login model after successful authentication
     *
     * @param customerId Customer ID
     * @param sessionToken Session token
     * @param username Username
     * @param fullName Full name
     * @param email Email address
     * @param phoneNumber Phone number
     */
    public void setSession(int customerId, String sessionToken, String username, String fullName, String email, String phoneNumber) {
        this.customerId = customerId;
        this.sessionToken = sessionToken;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        System.out.println("✅ Customer session set: " + username + " (ID: " + customerId + ")");
    }

    /**
     * Clear session on logout
     */
    public void clearSession() {
        this.customerId = 0;
        this.sessionToken = null;
        this.username = null;
        this.fullName = null;
        this.email = null;
        this.phoneNumber = null;
        System.out.println("🔓 Customer session cleared");
    }

    public void logout() throws AuthenticationException {
        if (hasSession() && clientServices != null) {
            try {
                clientServices.logoutCustomer(customerId, sessionToken);
                clearSession();
                System.out.println("✅ Logged out successfully from server");
            } catch (AuthenticationException e) {
                System.out.println("⚠️ Logout warning: " + e.message);
                clearSession(); // Clear local session anyway
            }
        }
    }

    /**
     * Check if session is active
     */
    public boolean hasSession() {
        return sessionToken != null && !sessionToken.isEmpty() && customerId > 0;
    }

    /**
     * Get current customer ID
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Get current session token
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Get current username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get current full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Get current email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get current phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Validate if the customer session is still active
     * This is called by SessionValidator every 5 seconds
     *
     * @param customerId The customer ID to validate
     * @return true if session is valid, false if invalidated (concurrent login)
     */
    public boolean validateSession(int customerId, String sessionToken) {
        try {
            if (clientServices == null) {
                System.err.println("⚠️ ClientServices not initialized");
                return false;
            }

            // 🔥 FIX: Use the passed sessionToken instead of instance variable
            clientServices.getCustomerBookings(customerId, sessionToken);
            return true;

        } catch (AuthenticationException e) {
            System.out.println("⚠️ Session validation failed for customer " + customerId + ": " + e.message);
            return false;

        } catch (Exception e) {
            System.err.println("⚠️ Session validation error (network?): " + e.getMessage());
            return true; // Don't kick out user for network errors
        }
    }

}