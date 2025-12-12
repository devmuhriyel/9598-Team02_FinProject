package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientServiceHelper;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

/**
 * CustomerParentModel - Base class for all customer models
 * Provides CORBA connection and session validation
 */
public abstract class CustomerParentModel {

    protected static ClientService clientServices;
    private static String hostIP;
    private static int hostPort;

    public static void initializeCORBAConnection(String[] args) {
        try {
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialHost", hostIP);
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(hostPort));

            ORB orb = ORB.init(args, props);

            // Get naming service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Resolve ClientService
            clientServices = ClientServiceHelper.narrow(ncRef.resolve_str("ClientService"));

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

    /**
     * 🆕 Validate if the customer session is still active
     * This is called by SessionValidator every 5 seconds
     *
     * @param customerId The customer ID to validate
     * @return true if session is valid, false if invalidated (concurrent login)
     */
    public boolean validateSession(int customerId) {
        try {
            // Check if we have a connection first
            if (clientServices == null) {
                System.err.println("⚠️ ClientServices not initialized");
                return false;
            }

            // Try to get customer bookings - if session is invalid, this will throw exception
            // This is a lightweight call to check session validity
            clientServices.getCustomerBookings(customerId);
            return true;

        } catch (AuthenticationException e) {
            // Session is no longer valid (concurrent login detected)
            System.out.println("⚠️ Session validation failed for customer " + customerId + ": " + e.message);
            return false;

        } catch (Exception e) {
            // Network error or other issue - treat as session still valid
            // Don't logout on network glitches
            System.err.println("⚠️ Session validation error (network?): " + e.getMessage());
            return true;
        }
    }
}