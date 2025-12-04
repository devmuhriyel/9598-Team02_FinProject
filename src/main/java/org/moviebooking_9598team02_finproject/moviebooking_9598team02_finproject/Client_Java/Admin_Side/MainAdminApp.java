package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginView;

public class MainAdminApp {
    public static void main(String[] args) {
        AdminLoginView adminLoginView = new AdminLoginView();
        AdminLoginModel adminLoginModel = new AdminLoginModel();
        AdminLoginController adminLoginController = new AdminLoginController(adminLoginView, adminLoginModel);
    }
}
