package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Payment;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentNotFoundException;

public class AdminPaymentModel extends AdminParentModel {

    // Fetch all payments sa Server
    public Payment[] getAllPayments(int adminId) throws AuthenticationException {
        return adminServices.getAllPayments(adminId, sessionToken);
    }

    // Update status like updated or failed
    public void updatePaymentStatus(int adminId, int paymentId, String status)
            throws AuthenticationException, PaymentNotFoundException {
        adminServices.updatePaymentStatus(adminId, sessionToken, paymentId, status);
    }
}