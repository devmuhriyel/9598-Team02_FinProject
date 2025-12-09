package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

/**
 * CustomerMgmtModel - Business Logic for Customer Management
 */
public class CustomerMgmtModel extends AdminParentModel {
    private int adminId;

    public CustomerMgmtModel(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Customer[] loadAllCustomers() throws AuthenticationException {
        return adminServices.getAllCustomers(adminId);
    }

    public Customer[] searchCustomers(String searchString) throws AuthenticationException {
        return adminServices.searchCustomers(adminId, searchString);
    }

    public Customer getCustomerDetails(String requestedCustomerId) throws AuthenticationException {
        return adminServices.getCustomerDetails(adminId, Integer.parseInt(requestedCustomerId));
    }

    public void updateCustomerInfo(int targetCustomerId, String email, String phoneNumber, String fullName) throws AuthenticationException {
        adminServices.updateCustomerInfo(adminId, targetCustomerId, email, phoneNumber, fullName);
    }
}