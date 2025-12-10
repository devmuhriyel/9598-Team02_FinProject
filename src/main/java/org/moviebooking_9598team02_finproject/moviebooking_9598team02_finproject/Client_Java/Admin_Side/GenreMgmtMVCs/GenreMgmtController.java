package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;

/**
 * GenreMgmtController - Pure business logic, no JavaFX imports
 */
public class GenreMgmtController {
    private GenreMgmtModel model;
    private GenreMgmtView view;
    private Genre[] genreArrays;

    public GenreMgmtController(GenreMgmtModel model, GenreMgmtView view) throws AuthenticationException {
        this.model = model;
        this.view = view;
        initializeButtons();
        loadAllGenres();
    }

    // ===== BUTTON INITIALIZATION =====
    private void initializeButtons() {
        // Form Buttons
        view.getAddButton().setOnAction(event -> handleAddButtonClick());
        view.getUpdateButton().setOnAction(event -> handleUpdateButtonClick());
        view.getDeleteButton().setOnAction(event -> handleDeleteButtonClick());
        view.getClearButton().setOnAction(event -> handleClearButtonClick());
        view.getRefreshButton().setOnAction(event -> handleRefreshButtonClick());
    }

    // ===== BUTTON HANDLERS =====
    private void handleAddButtonClick() {
        try {
            String genreName = view.getGenreNameField().getText();
            String genreDescription = view.getGenreDescriptionField().getText();

            // Validate inputs
            if (genreName == null || genreName.trim().isEmpty()) {
                view.showMessage("❌ Genre name cannot be empty");
                return;
            }

            view.clearMessage();
            int newGenreId = model.addGenre(genreName.trim(), genreDescription != null ? genreDescription.trim() : "");
            view.showMessage("✅ Genre added successfully with ID: " + newGenreId);
            view.clearFields();
            loadAllGenres();

        } catch (InvalidInputException e) {
            view.showMessage("❌ Error: " + e.message);
        } catch (AuthenticationException e) {
            view.showMessage("❌ Error: " + e.getMessage());
        }
    }

    private void handleUpdateButtonClick() {
        Genre selected = view.getGenreTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            view.showMessage("❌ Please select a genre to update");
            return;
        }

        try {
            String genreName = view.getGenreNameField().getText();
            String genreDescription = view.getGenreDescriptionField().getText();

            // Validate inputs
            if (genreName == null || genreName.trim().isEmpty()) {
                view.showMessage("❌ Genre name cannot be empty");
                return;
            }

            view.clearMessage();
            model.updateGenre(selected.genreId, genreName.trim(), genreDescription != null ? genreDescription.trim() : "");
            view.showMessage("✅ Genre updated successfully");
            view.clearFields();
            view.getGenreTable().getSelectionModel().clearSelection();
            loadAllGenres();

        } catch (AuthenticationException e) {
            view.showMessage("❌ Error: " + e.getMessage());
        }
    }

    private void handleDeleteButtonClick() {
        Genre selected = view.getGenreTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            view.showMessage("❌ Please select a genre to delete");
            return;
        }

        try {
            view.clearMessage();
            model.deleteGenre(selected.genreId);
            view.showMessage("✅ Genre deleted successfully");
            view.clearFields();
            view.getGenreTable().getSelectionModel().clearSelection();
            loadAllGenres();

        } catch (AuthenticationException e) {
            view.showMessage("❌ Error: " + e.getMessage());
        }
    }

    private void handleClearButtonClick() {
        view.clearFields();
        view.getGenreTable().getSelectionModel().clearSelection();
        view.clearMessage();
    }

    private void handleRefreshButtonClick() {
        try {
            view.clearMessage();
            loadAllGenres();
            view.showMessage("✅ Genre list refreshed");
        } catch (AuthenticationException e) {
            view.showMessage("❌ Error: " + e.getMessage());
        }
    }

    // ===== DATA LOADING =====
    public void loadAllGenres() throws AuthenticationException {
        System.out.println("=== Loading Genres ===");
        genreArrays = model.loadAllGenres();
        System.out.println("Got " + genreArrays.length + " genres from model");

        for (Genre genre : genreArrays) {
            System.out.println("Genre: " + genre.genreId + " - " + genre.genreName);
        }

        view.displayGenreList(genreArrays);
    }
}
