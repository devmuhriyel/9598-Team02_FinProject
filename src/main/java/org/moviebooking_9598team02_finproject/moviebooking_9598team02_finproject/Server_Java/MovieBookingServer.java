package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class MovieBookingServer {
    public static void main(String[] args) {
        try{
            ORB orb = ORB.init(args, null);

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            MBAdminServant adminServant = new MBAdminServant();
            MBClientServant clientServant = new MBClientServant();
            MBCommonServant commonServant = new MBCommonServant();

            adminServant.setOrb(orb);

            org.omg.CORBA.Object adminRef = rootPOA.servant_to_reference(adminServant);
            org.omg.CORBA.Object clientRef = rootPOA.servant_to_reference(clientServant);
            org.omg.CORBA.Object commonRef = rootPOA.servant_to_reference(commonServant);

            org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
            NamingContextExt namingContextExt = NamingContextExtHelper.narrow(namingServiceObj);

            NameComponent[] adminPath = namingContextExt.to_name("AdminService");
            namingContextExt.rebind(adminPath, adminRef);
            NameComponent[] clientPath = namingContextExt.to_name("ClientService");
            namingContextExt.rebind(clientPath, clientRef);
            NameComponent[] commonPath = namingContextExt.to_name("CommonService");
            namingContextExt.rebind(commonPath, commonRef);

            orb.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
