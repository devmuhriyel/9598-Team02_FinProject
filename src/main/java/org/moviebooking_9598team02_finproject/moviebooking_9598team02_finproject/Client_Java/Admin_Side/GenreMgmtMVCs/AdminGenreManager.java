package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class AdminGenreManager {
    private Pane genrePane;
    private GenreMgmtView genreView;
    private GenreMgmtModel genreModel;
    private GenreMgmtController genreController;
    private int adminId;

    public AdminGenreManager(int adminId) {
        this.adminId = adminId;
        initializeGenreMVC();
    }

    private void initializeGenreMVC() {
        try {
            // Load Genre Management FXML
            FXMLLoader genreLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/GenreManagement.fxml"));
            genrePane = genreLoader.load();
            genreView = genreLoader.getController();

            // Create Model with admin ID
            genreModel = new GenreMgmtModel(adminId);

            // Create Controller with Model
            genreController = new GenreMgmtController(genreModel);

            // Set Controller in View
            genreView.setController(genreController);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Genre Management FXML: " + e.getMessage());
        }
    }

    public Pane getGenrePane() {
        return genrePane;
    }

    public void showGenreView(Pane containerPane) {
        if (containerPane != null && genrePane != null) {
            containerPane.getChildren().clear();
            containerPane.getChildren().add(genrePane);
        }
    }

    public void updateAdminId(int newAdminId) {
        this.adminId = newAdminId;
        if (genreModel != null) {
            genreModel.setAdminId(newAdminId);
        }
        if (genreController != null) {
            genreController.setAdminId(newAdminId);
        }
    }

    public int getAdminId() {
        return adminId;
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
}

