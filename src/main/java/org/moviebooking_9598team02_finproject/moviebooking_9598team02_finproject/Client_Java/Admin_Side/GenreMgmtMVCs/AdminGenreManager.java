package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

import java.io.IOException;

public class AdminGenreManager {
    private Pane genrePane;
    private GenreMgmtView genreView;
    private GenreMgmtModel genreModel;
    private GenreMgmtController genreController;
    private int adminId;
    private String sessionToken; // ✅ ADD

    // ✅ FIX: Accept sessionToken in constructor
    public AdminGenreManager(int adminId, String sessionToken) {
        this.adminId = adminId;
        this.sessionToken = sessionToken; // ✅ STORE
        initializeGenreMVC();
    }

    private void initializeGenreMVC() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/GenreManagement.fxml")
            );
            genrePane = loader.load();
            genreView = loader.getController();

            // ✅ FIX: Use stored sessionToken
            genreModel = new GenreMgmtModel(adminId, sessionToken);
            genreController = new GenreMgmtController(genreModel, genreView);

            System.out.println("✅ Genre Management MVC initialized");

        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing Genre Management: " + e.getMessage());
        }
    }

    // ===== GETTERS =====
    public Pane getGenrePane() {
        return genrePane;
    }

    public GenreMgmtView getGenreView() {
        return genreView;
    }

    public GenreMgmtModel getGenreModel() {
        return genreModel;
    }

    public GenreMgmtController getGenreController() {
        return genreController;
    }

    public int getAdminId() {
        return adminId;
    }

    public void updateAdminId(int newAdminId) {
        this.adminId = newAdminId;
        if (genreModel != null) {
            genreModel.setAdminId(newAdminId);
        }
    }
}