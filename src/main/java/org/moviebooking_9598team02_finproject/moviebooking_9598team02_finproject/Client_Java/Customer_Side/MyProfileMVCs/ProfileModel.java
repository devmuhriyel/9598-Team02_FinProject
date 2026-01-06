package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;

public class ProfileModel extends CustomerParentModel {

    // ✅ FIX: Add constructor that initializes session data
    public ProfileModel(int customerId, String sessionToken, String username,
                        String fullName, String email, String phoneNumber) {
        this.customerId = customerId;
        this.sessionToken = sessionToken;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;

        System.out.println("✅ ProfileModel initialized with customerId: " + customerId);
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