package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;

import java.io.IOException;

public class AdminMMNavManager {

    private final AdminService adminService;
    private final int currentAdminId;
    private Pane movieMgmtPane; // The root pane of the loaded FXML

    private MovieController movieController;
    private MovieModel movieModel;
    private MovieView movieView;

    public AdminMMNavManager(AdminService adminService, Admin admin) {
        this.adminService = adminService;
        this.currentAdminId = admin.adminId;
    }

    private void initializeMovieMgmtView() throws IOException {
        movieModel = new MovieModel(adminService, currentAdminId);
        movieView = new MovieView();
        movieController = new MovieController(movieModel, movieView);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminMovieMgmt.fxml")
        );

        loader.setController(movieView);
        movieMgmtPane = loader.load();

        // Initialize the controller after the FXML is loaded
        movieController.initialize(null, null);
    }

    public Pane getMovieMgmtView() {
        if (movieMgmtPane == null) {
            try {
                initializeMovieMgmtView();
            } catch (IOException e) {
                e.printStackTrace();
                movieMgmtPane = new Pane(new javafx.scene.control.Label("Error loading Movie Management View."));
            }
        }
        return movieMgmtPane;
    }
}