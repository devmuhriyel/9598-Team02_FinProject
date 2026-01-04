package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;

public class ProfileModel extends CustomerParentModel {

    public ProfileModel() {
    }

    // ==== UPDATE METHODS ====
    public void updateCustomerProfile(String phoneNumber, String fullName) throws AuthenticationException {
        clientServices.updateCustomerProfile(customerId, sessionToken, phoneNumber, fullName);

        // ✅ Update local session data after successful update
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
    }

    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        clientServices.changePassword(customerId, sessionToken, oldPassword, newPassword);
    }
}