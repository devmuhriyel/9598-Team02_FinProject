package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

/**
 * AdminMgmtModel - Business Logic (following Genre pattern)
 */
public class AdminMgmtModel extends AdminParentModel {
    private int adminId;

    public AdminMgmtModel(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Admin[] loadAllAdmins() throws AuthenticationException {
        return adminServices.getAllAdmins(adminId);
    }

    public Admin[] searchAdmins(String searchString) throws AuthenticationException {
        return adminServices.searchAdmins(adminId, searchString);
    }

    public Admin getAdminById(String requestedAdminId) throws AuthenticationException {
        return adminServices.getAdminById(Integer.parseInt(requestedAdminId), adminId);
    }

    public int createAdmin(String username, String email, String password, String fullName, String phoneNumber) throws AuthenticationException, InvalidInputException {
        return adminServices.createAdmin(adminId, username, email, password, fullName, phoneNumber);
    }

    public void updateAdminProfile(int targetAdminId, String fullName, String phoneNumber) throws AuthenticationException {
        adminServices.updateAdminProfile(targetAdminId, fullName, phoneNumber);
    }

    public void updateAdminEmail(int targetAdminId, String newEmail, String password) throws AuthenticationException, InvalidInputException {
        adminServices.updateAdminEmail(targetAdminId, newEmail, password);
    }

    public void changeAdminPassword(int targetAdminId, String oldPassword, String newPassword) throws AuthenticationException {
        adminServices.changeAdminPassword(targetAdminId, oldPassword, newPassword);
    }

    public void deleteAdmin(int targetAdminId) throws AuthenticationException {
        adminServices.deleteAdmin(adminId, targetAdminId);
    }
}