package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CustomerLoginResponse;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;

/**
 * ✅ FIXED: Now returns CustomerLoginResponse to get sessionToken
 */
public class CustomerLoginModel extends CustomerParentModel {

    /**
     * Login customer and store session credentials
     * @return CustomerLoginResponse containing Customer object and sessionToken
     */
    public CustomerLoginResponse logInCustomer(String username, String password) throws AuthenticationException {
        CustomerLoginResponse response = clientServices.loginCustomer(username, password);

        // ✅ Store full session including profile data
        setSession(
                response.customer.customerId,
                response.sessionToken,
                response.customer.username,
                response.customer.fullName,
                response.customer.email,
                response.customer.phoneNumber
        );

        return response;
    }
}