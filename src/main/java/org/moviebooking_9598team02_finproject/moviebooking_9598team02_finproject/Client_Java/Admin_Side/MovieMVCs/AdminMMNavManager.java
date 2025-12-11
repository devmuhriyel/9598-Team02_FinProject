package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;

import java.io.IOException;

public class AdminMMNavManager {

    private final AdminService adminService;
    private final int currentAdminId;
    private Pane movieMgmtPane;

    private MovieController movieController;
    private MovieModel movieModel;
    private MovieView movieView;

    public AdminMMNavManager(AdminService adminService, Admin admin) {
        this.adminService = adminService;
        this.currentAdminId = admin.adminId;
    }

    private void initializeMovieMgmtView() throws IOException {
        System.out.println("🎬 Initializing Movie Management MVC...");

        // 1. Load FXML FIRST (this creates the View)
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminMovieMgmt.fxml")
        );

        movieMgmtPane = loader.load();
        System.out.println("✅ FXML loaded");

        // 2. Get the View from FXML loader
        movieView = loader.getController();
        System.out.println("✅ View obtained from FXML");

        // 3. Create Model
        movieModel = new MovieModel(adminService, currentAdminId);
        System.out.println("✅ Model created");

        // 4. Create Controller and pass View + Model
        movieController = new MovieController(movieModel, movieView, currentAdminId);
        System.out.println("✅ Controller created");

        // 5. NOW initialize the controller (wires buttons + loads data)
        movieController.initialize();
        System.out.println("✅ Controller initialized - all buttons wired!");
    }

    public Pane getMovieMgmtView() {
        if (movieMgmtPane == null) {
            try {
                initializeMovieMgmtView();
            } catch (IOException e) {
                System.err.println("❌ ERROR loading Movie Management:");
                e.printStackTrace();
                movieMgmtPane = new Pane(new javafx.scene.control.Label("Error loading Movie Management View."));
            }
        }
        return movieMgmtPane;
    }
}