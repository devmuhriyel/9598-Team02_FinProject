package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Signup;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class AdminSignupModel extends AdminParentModel {
    public int signUpAdmin(String userName, String fullName, String phoneNumber, String emailAddress, String password, String confirmPassword) throws InvalidInputException, AuthenticationException {

        if (!password.equals(confirmPassword)){
            throw new IllegalArgumentException("Passwords do not match");
        } else if (phoneNumber.length() > 12 || phoneNumber.length() < 10){
            throw new IllegalArgumentException("Phone number is invalid");
        } else if (!emailAddress.endsWith(".com")){
            if(emailAddress.split("@").length != 2){
                throw new IllegalArgumentException("Email address is invalid");
            }
        }

        // ✅ FIX: Pass empty string instead of null sessionToken for signup
        // Admin ID 0 and empty token indicate this is a signup operation
        return adminServices.createAdmin(0, "", userName, emailAddress, password, fullName, phoneNumber);
    }
}