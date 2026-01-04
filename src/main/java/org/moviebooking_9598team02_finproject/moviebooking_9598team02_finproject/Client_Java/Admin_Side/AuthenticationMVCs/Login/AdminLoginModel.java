package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AdminLoginResponse;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class AdminLoginModel extends AdminParentModel {


    public AdminLoginResponse logInAdmin(String username, String password) throws AuthenticationException {
        AdminLoginResponse response = adminServices.loginAdmin(username, password);

        // ✅ Store session in parent model for child models to use
        setSession(response.admin.adminId, response.sessionToken, response.admin.username);

        return response;
    }
}