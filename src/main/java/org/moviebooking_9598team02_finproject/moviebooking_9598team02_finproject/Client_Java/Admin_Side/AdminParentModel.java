package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServiceHelper;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

/**
 * AdminParentModel - Base class for all admin models
 * ✅ FIXED: Now stores sessionToken for automatic authentication
 */
public abstract class AdminParentModel {
    public static AdminService adminServices;
    protected static CommonService commonServices;
    private static String hostIP;
    private static int hostPort;

    // ✅ NEW: Session storage - accessible by all child models
    protected int adminId;
    protected String sessionToken;
    protected String username;

    public static void initializeCORBAConnection(String[] args){
        try {
            Properties props = new java.util.Properties();
            props.put("org.omg.CORBA.ORBInitialHost", hostIP);
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(hostPort));

            ORB orb = ORB.init(args, props);

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt nameContextExt = NamingContextExtHelper.narrow(objectRef);

            String adminName = "AdminService";
            adminServices = AdminServiceHelper.narrow(nameContextExt.resolve_str(adminName));

            String commonName = "CommonService";
            commonServices = CommonServiceHelper.narrow(nameContextExt.resolve_str(commonName));
        } catch (Exception e) {
            e.printStackTrace();
            adminServices = null;
            commonServices = null;
        }
    }

    public boolean isConnected(){
        return adminServices != null && commonServices != null;
    }

    public void setHostIP(String ip){
        hostIP = ip;
    }

    public void setHostPort(int port){
        hostPort = port;
    }

    // ✅ NEW: Session management methods

    /**
     * Set session credentials after login
     * Called by login model after successful authentication
     */
    public void setSession(int adminId, String sessionToken, String username) {
        this.adminId = adminId;
        this.sessionToken = sessionToken;
        this.username = username;
        System.out.println("✅ Admin session set: " + username + " (ID: " + adminId + ")");
    }

    /**
     * Clear session on logout
     */
    public void clearSession() {
        this.adminId = 0;
        this.sessionToken = null;
        this.username = null;
        System.out.println("🔓 Admin session cleared");
    }
    public void logout() throws AuthenticationException {
        if (hasSession() && adminServices != null) {
            try {
                adminServices.logoutAdmin(adminId, sessionToken);
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
        return sessionToken != null && !sessionToken.isEmpty() && adminId > 0;
    }

    /**
     * Get current admin ID
     */
    public int getAdminId() {
        return adminId;
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
     * Validate if the admin session is still active
     * This is called by SessionValidator every 5 seconds
     *
     * @param adminId The admin ID to validate
     * @return true if session is valid, false if invalidated (concurrent login)
     */
    public boolean validateSession(int adminId) {
        try {
            if (adminServices == null) {
                System.err.println("⚠️ AdminServices not initialized");
                return false;
            }

            // Try to get admin info - if session is invalid, this will throw exception
            adminServices.getAdminById(adminId, sessionToken, adminId);
            return true;

        } catch (AuthenticationException e) {
            System.out.println("⚠️ Session validation failed for admin " + adminId + ": " + e.message);
            return false;

        } catch (Exception e) {
            System.err.println("⚠️ Session validation error (network?): " + e.getMessage());
            return true;
        }
    }
}