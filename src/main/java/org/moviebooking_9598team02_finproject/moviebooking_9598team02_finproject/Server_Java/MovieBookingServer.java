package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

public class MovieBookingServer {
    public static void main(String[] args) {
        try{

            Properties props = new java.util.Properties();
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");
            props.put("org.omg.CORBA.ORBInitialPort", "1050");
            // Initialize ORB
            ORB orb = ORB.init(args, props);

            // Initialize POA
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Initialize SessionManager for tracking logged-in users
            SessionManager.initialize();
            System.out.println("SessionManager initialized");

            // Create servants
            MBAdminServant adminServant = new MBAdminServant();
            MBClientServant clientServant = new MBClientServant();
            MBCommonServant commonServant = new MBCommonServant();

            // Set ORB reference for shutdown capability
            adminServant.setOrb(orb);

            // Register servants with POA
            org.omg.CORBA.Object adminRef = rootPOA.servant_to_reference(adminServant);
            org.omg.CORBA.Object clientRef = rootPOA.servant_to_reference(clientServant);
            org.omg.CORBA.Object commonRef = rootPOA.servant_to_reference(commonServant);

            // Get naming service reference
            org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
            NamingContextExt namingContextExt = NamingContextExtHelper.narrow(namingServiceObj);

            // Bind servants to naming service
            NameComponent[] adminPath = namingContextExt.to_name("AdminService");
            namingContextExt.rebind(adminPath, adminRef);
            System.out.println("AdminService bound to naming service");

            NameComponent[] clientPath = namingContextExt.to_name("ClientService");
            namingContextExt.rebind(clientPath, clientRef);
            System.out.println("ClientService bound to naming service");

            NameComponent[] commonPath = namingContextExt.to_name("CommonService");
            namingContextExt.rebind(commonPath, commonRef);
            System.out.println("CommonService bound to naming service");

            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                try {
                    // Print final session stats
                    System.out.println(SessionManager.getSessionStats());

                    // Cleanup sessions
                    SessionManager.shutdown();

                    // Close database connections
                    DatabaseAccess.closeConnection();

                    // Shutdown ORB
                    orb.shutdown(false);

                    System.out.println("Server shutdown complete");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            System.out.println("Movie Booking Server is ready and waiting for requests...");
            System.out.println("Press Ctrl+C to shutdown");

            // Start ORB
            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}