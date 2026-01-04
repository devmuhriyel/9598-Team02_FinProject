package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentException;

public class PaymentModel extends CustomerParentModel {

    // ✅ ADD: Constructor
    public PaymentModel(int customerId, String sessionToken) {
        this.customerId = customerId;
        this.sessionToken = sessionToken;
    }

    // Methods remain the same - they already use this.customerId and this.sessionToken
    public int processPayment(int customerId, String sessionToken, int bookingId, double amount, String method, String txnId)
            throws AuthenticationException, BookingException, PaymentException {
        return clientServices.processPayment(customerId, sessionToken, bookingId, amount, method, txnId);
    }

    public void cancelBooking(int customerId, int bookingId)
            throws AuthenticationException, BookingException {
        clientServices.cancelBooking(customerId, sessionToken, bookingId);
    }
}