package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminServiceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;


public abstract class AdminParentModel {
    protected static AdminService adminServices;

    public static void initializeCORBAConnection(String[] args){
        try {
            ORB orb = ORB.init(args, null);

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt nameContextExt = NamingContextExtHelper.narrow(objectRef);
            String name = "AdminService";
            adminServices = AdminServiceHelper.narrow(nameContextExt.resolve_str(name));
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {

        } catch (InvalidName e) {

        } catch (CannotProceed e) {

        } catch (NotFound e) {

        }

    }

    public boolean isConnected(){
        return adminServices != null;
    }
}
