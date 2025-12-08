package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MovieController implements Initializable {

    private final MovieModel model;
    private final MovieView view;

    private final Map<Integer, Movie> movieDataMap = new HashMap<>();
    private final Map<Integer, Genre> genreDataMap = new HashMap<>();

    private Movie currentlySelectedMovie = null;
    private int selectedMovieId = -1;

    // ===== CONSTRUCTOR (Dependency Injection) =====

    public MovieController(MovieModel model, MovieView view) {
        this.model = model;
        this.view = view;
    }

    // ===== INITIALIZATION =====

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        view.initializeTableColumns();
        setupTableSelectionListener();
        initializeComboBoxes();
        loadAllGenres();
        loadAllMovies();
        view.setEditMode(false);
    }

    private void setupTableSelectionListener() {
        view.getMovieTableView().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        handleMovieSelection(newSelection.getMovieId());
                    }
                }
        );
    }

    private void initializeComboBoxes() {
        // Initialize rating combo box (1-10)
        ObservableList<Integer> ratings = FXCollections.observableArrayList();
        for (int i = 1; i <= 10; i++) {
            ratings.add(i);
        }
        view.getRatingComboBox().setItems(ratings);

        // Initialize status combo box
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "Now Showing", "Coming Soon", "Ended"
        );
        view.getStatusComboBox().setItems(statuses);
    }

    // ===== DATA LOADING =====

    private void loadAllGenres() {
        try {
            Genre[] allGenres = model.getAllGenres();
            if (allGenres == null || allGenres.length == 0) {
                view.showStatusMessage("No genres found. Please add genres first.", true);
                return;
            }

            genreDataMap.clear();
            ObservableList<String> genreNames = FXCollections.observableArrayList();

            for (Genre genre : allGenres) {
                genreDataMap.put(genre.genreId, genre);
                genreNames.add(genre.genreName);
            }

            view.getGenreComboBox().setItems(genreNames);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
        } catch (Exception e) {
            view.showStatusMessage("Error loading genres: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void loadAllMovies() {
        try {
            Movie[] allMovies = model.getAllMovies();
            if (allMovies == null || allMovies.length == 0) {
                view.showStatusMessage("No movies found in the system.", false);
                return;
            }

            movieDataMap.clear();
            ObservableList<MovieView.MovieTableData> tableData = FXCollections.observableArrayList();

            for (Movie movie : allMovies) {
                movieDataMap.put(movie.movieId, movie);

                // Get genres for this movie
                String genreNames = getGenreNamesForMovie(movie.movieId);

                // Note: Duration, releaseDate, and status are not in the Movie struct
                // You may need to store these separately or modify the IDL
                MovieView.MovieTableData rowData = new MovieView.MovieTableData(
                        movie.movieId,
                        movie.movieTitle,
                        genreNames,
                        null, // duration not available in Movie struct
                        movie.movieRating,
                        null, // releaseDate not available in Movie struct
                        null  // status not available in Movie struct
                );
                tableData.add(rowData);
            }

            view.getMovieTableView().setItems(tableData);
            view.showStatusMessage("Movies loaded successfully. Total: " + allMovies.length, false);

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
        } catch (Exception e) {
            view.showStatusMessage("Error loading movies: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private String getGenreNamesForMovie(int movieId) {
        try {
            Genre[] genres = model.getMovieGenres(movieId);
            if (genres == null || genres.length == 0) {
                return "N/A";
            }
            return Arrays.stream(genres)
                    .map(g -> g.genreName)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Error";
        }
    }

    // ===== EVENT HANDLERS =====

    public void handleSearchClick(ActionEvent event) {
        String searchText = view.getSearchField().getText().trim();

        if (searchText.isEmpty()) {
            loadAllMovies();
            return;
        }

        ObservableList<MovieView.MovieTableData> filteredData = FXCollections.observableArrayList();

        for (Movie movie : movieDataMap.values()) {
            boolean matches = movie.movieTitle.toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(movie.movieId).equals(searchText);

            if (matches) {
                String genreNames = getGenreNamesForMovie(movie.movieId);
                MovieView.MovieTableData rowData = new MovieView.MovieTableData(
                        movie.movieId,
                        movie.movieTitle,
                        genreNames,
                        null,
                        movie.movieRating,
                        null,
                        null
                );
                filteredData.add(rowData);
            }
        }

        view.getMovieTableView().setItems(filteredData);
        view.showStatusMessage("Search results: " + filteredData.size() + " movie(s) found.", false);
    }

    public void handleClearSearchClick(ActionEvent event) {
        view.getSearchField().clear();
        loadAllMovies();
    }

    public void handleAddNewMovieClick(ActionEvent event) {
        view.clearAllFields();
        view.setEditMode(false);
        currentlySelectedMovie = null;
        selectedMovieId = -1;
        view.updateMovieDetailsLabel("Fill in the details below to add a new movie.");
        view.clearStatusMessage();
    }

    public void handleSaveMovieClick(ActionEvent event) {
        view.clearStatusMessage();

        // Validate inputs
        String title = view.getTitleInput().getText().trim();
        String description = view.getDescriptionInput().getText().trim();
        String posterUrl = view.getPosterUrlInput().getText().trim();
        Integer rating = view.getRatingComboBox().getValue();
        String selectedGenreName = view.getGenreComboBox().getValue();

        if (title.isEmpty()) {
            view.showStatusMessage("Error: Movie title is required.", true);
            return;
        }

        if (rating == null) {
            view.showStatusMessage("Error: Please select a rating.", true);
            return;
        }

        if (selectedGenreName == null) {
            view.showStatusMessage("Error: Please select at least one genre.", true);
            return;
        }

        try {
            // Find genre ID by name
            int genreId = findGenreIdByName(selectedGenreName);
            if (genreId == -1) {
                view.showStatusMessage("Error: Selected genre not found.", true);
                return;
            }

            int[] genreIds = new int[]{genreId};

            // Add movie
            int newMovieId = model.addMovie(title, genreIds, description, rating, posterUrl);

            view.showStatusMessage("Movie added successfully! ID: " + newMovieId, false);
            view.clearAllFields();
            loadAllMovies();

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
        } catch (InvalidInputException e) {
            view.showStatusMessage("Error: Invalid input - " + e.message, true);
        } catch (Exception e) {
            view.showStatusMessage("Error adding movie: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public void handleUpdateMovieClick(ActionEvent event) {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showStatusMessage("Please select a movie from the table to update.", true);
            return;
        }

        view.clearStatusMessage();

        try {
            // Update title if changed
            String newTitle = view.getTitleInput().getText().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(currentlySelectedMovie.movieTitle)) {
                model.updateMovieTitle(selectedMovieId, newTitle);
            }

            // Update description if changed
            String newDescription = view.getDescriptionInput().getText().trim();
            if (!newDescription.equals(currentlySelectedMovie.movieDescription)) {
                model.updateMovieDescription(selectedMovieId, newDescription);
            }

            // Update rating if changed
            Integer newRating = view.getRatingComboBox().getValue();
            if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                model.updateMovieRating(selectedMovieId, newRating);
            }

            // Update poster URL if changed
            String newPosterUrl = view.getPosterUrlInput().getText().trim();
            if (!newPosterUrl.equals(currentlySelectedMovie.moviePosterURL)) {
                model.updateMoviePoster(selectedMovieId, newPosterUrl);
            }

            // Update genre if changed
            String selectedGenreName = view.getGenreComboBox().getValue();
            if (selectedGenreName != null) {
                int genreId = findGenreIdByName(selectedGenreName);
                if (genreId != -1) {
                    model.updateMovieGenres(selectedMovieId, new int[]{genreId});
                }
            }

            view.showStatusMessage("Movie updated successfully!", false);
            loadAllMovies();

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
        } catch (MovieNotFoundException e) {
            view.showStatusMessage("Error: Movie not found - " + e.message, true);
        } catch (Exception e) {
            view.showStatusMessage("Error updating movie: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public void handleDeleteMovieClick(ActionEvent event) {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showStatusMessage("Please select a movie from the table to delete.", true);
            return;
        }

        // Show confirmation dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Movie: " + currentlySelectedMovie.movieTitle);
        alert.setContentText("Are you sure you want to delete this movie? This action cannot be undone.");

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                model.deleteMovie(selectedMovieId);
                view.showStatusMessage("Movie deleted successfully!", false);
                view.clearAllFields();
                currentlySelectedMovie = null;
                selectedMovieId = -1;
                view.setEditMode(false);
                loadAllMovies();

            } catch (AuthenticationException e) {
                view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
            } catch (MovieNotFoundException e) {
                view.showStatusMessage("Error: Movie not found - " + e.message, true);
            } catch (Exception e) {
                view.showStatusMessage("Error deleting movie: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }

    public void handleClearFieldsClick(ActionEvent event) {
        view.clearAllFields();
        currentlySelectedMovie = null;
        selectedMovieId = -1;
        view.setEditMode(false);
        view.updateMovieDetailsLabel("Select a movie from the table to view details.");
        view.clearStatusMessage();
    }

    // ===== HELPER METHODS =====

    private void handleMovieSelection(int movieId) {
        Movie movie = movieDataMap.get(movieId);
        if (movie == null) {
            view.showStatusMessage("Error: Movie data not found.", true);
            return;
        }

        currentlySelectedMovie = movie;
        selectedMovieId = movieId;

        // Populate fields with movie data
        view.getTitleInput().setText(movie.movieTitle);
        view.getDescriptionInput().setText(movie.movieDescription);
        view.getRatingComboBox().setValue(movie.movieRating);
        view.getPosterUrlInput().setText(movie.moviePosterURL);

        // Set genre
        try {
            Genre[] genres = model.getMovieGenres(movieId);
            if (genres != null && genres.length > 0) {
                view.getGenreComboBox().setValue(genres[0].genreName);
            }
        } catch (Exception e) {
            // Ignore genre loading errors
        }

        // Update details label
        String details = String.format(
                "Selected: %s (ID: %d) | Rating: %d/10 | Created: %s",
                movie.movieTitle, movie.movieId, movie.movieRating, movie.createdAt
        );
        view.updateMovieDetailsLabel(details);

        view.setEditMode(true);
        view.clearStatusMessage();
    }

    private int findGenreIdByName(String genreName) {
        for (Genre genre : genreDataMap.values()) {
            if (genre.genreName.equals(genreName)) {
                return genre.genreId;
            }
        }
        return -1;
    }
}