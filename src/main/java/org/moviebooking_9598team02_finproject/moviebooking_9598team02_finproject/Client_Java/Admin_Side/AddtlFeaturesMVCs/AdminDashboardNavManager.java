package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import java.io.IOException;

public class AdminDashboardNavManager {
    private final int currentAdminId;
    private Pane dashboardPane;

    private DashboardController dashboardController;
    private DashboardModel dashboardModel;
    private DashboardView dashboardView;

    public AdminDashboardNavManager(Admin admin) {
        this.currentAdminId = admin.adminId;
        try {
            initializeDashboardView(); // Initialize immediately
        } catch (IOException e) {
            e.printStackTrace();
            dashboardPane = new Pane(new Label("Error loading Dashboard View: " + e.getMessage()));
        }
    }

    private void initializeDashboardView() throws IOException {
        // Initialize model
        dashboardModel = new DashboardModel(currentAdminId);
        // Load FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminDashboard.fxml")
        );
        dashboardPane = loader.load();
        dashboardView = loader.getController();
        // Initialize controller
        dashboardController = new DashboardController(
                currentAdminId,
                dashboardModel,
                dashboardView
        );
        // Initialize the dashboard
        dashboardController.initializeDashboard();
    }

    public Pane getDashboardView() {
        return dashboardPane;
    }
}