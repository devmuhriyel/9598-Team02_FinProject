package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

import java.io.IOException;

public class AdminPaymentNavManager {

    private Admin admin;
    private Pane paymentViewPane;

    public AdminPaymentNavManager(Admin admin) {
        this.admin = admin;
    }

    public Pane getPaymentMgmtView() {
        if (paymentViewPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminPaymentMgmt.fxml"));
                paymentViewPane = loader.load();
                AdminPaymentView view = loader.getController();
                AdminPaymentModel model = new AdminPaymentModel();
                new AdminPaymentController(view, model, admin.adminId);
            } catch (IOException e) {
                e.printStackTrace();
                paymentViewPane = new Pane();
            }
        }
        return paymentViewPane;
    }
}