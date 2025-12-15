package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;

public class ProfileModel extends CustomerParentModel {
    private Customer customer;
    public ProfileModel(Customer customer) {
        this.customer = customer;
    }
    public String getUsername() { return customer.username;    }
    public String getFullName() {return customer.fullName;    }
    public String getEmail() {return customer.email;    }
    public String getPhoneNumber() {return customer.phoneNumber;    }

    // ==== UPDATE METHODS ====
    public void updateCustomerProfile(String phoneNumber, String fullName) throws AuthenticationException {
        clientServices.updateCustomerProfile(customer.customerId, phoneNumber, fullName);
        this.customer.phoneNumber = phoneNumber;
        this.customer.fullName = fullName;
    }

    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        clientServices.changePassword(customer.customerId, oldPassword, newPassword);
    }

    // ==== CUSTOMER ID ====
    public int getCurrentCustomerId() {
        return customer.customerId;
    }
}