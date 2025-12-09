package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServiceHelper;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.Properties;


public abstract class AdminParentModel {
    public static AdminService adminServices;
    protected static CommonService commonServices;
    private static String hostIP;
    private static int hostPort;

    public static void initializeCORBAConnection(String[] args){
        try {
            Properties props = new java.util.Properties();
            props.put("org.omg.CORBA.ORBInitialHost", hostIP);
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(hostPort)); // CONVERT TO STRING!

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
}
