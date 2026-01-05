package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.PaymentNotFoundException;

public class AdminPaymentModel extends AdminParentModel {

    public AdminPaymentModel(int adminId, String sessionToken) {
        this.adminId = adminId;
        this.sessionToken = sessionToken;
        System.out.println("✅ AdminPaymentModel initialized with adminId: " + adminId);
    }

    // Fetch all payments with details (includes customer name)
    public PaymentDetails[] getAllPayments(int adminId) throws AuthenticationException {
        return adminServices.getAllPaymentsWithDetails(adminId, sessionToken);
    }

    // Update status like updated or failed
    public void updatePaymentStatus(int adminId, int paymentId, String status)
            throws AuthenticationException, PaymentNotFoundException {
        adminServices.updatePaymentStatus(adminId, sessionToken, paymentId, status);
    }
}