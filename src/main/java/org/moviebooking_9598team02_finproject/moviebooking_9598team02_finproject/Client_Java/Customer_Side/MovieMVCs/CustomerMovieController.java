package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerMovieController {

    private CustomerMovieView view;
    private CustomerMovieModel model;
    private MovieNavigationManager movieNavigationManager;

    private Map<Integer, Movie> movieDataMap = new HashMap<>();
    private Map<Integer, Genre> genreDataMap = new HashMap<>();
    private Movie currentlySelectedMovie = null;

    public CustomerMovieController(CustomerMovieView view, CustomerMovieModel model, MovieNavigationManager movieNavigationManager) {
        this.view = view;
        this.model = model;
        this.movieNavigationManager = movieNavigationManager;
    }

    /**
     * Manual initializer invoked by MovieNavigationManager after construction.
     */
    public void init() {
        wireButtons();
        initializeFilters();
        loadAllGenres();
        loadAllMovies();
    }

    private void wireButtons() {
        if (view == null) return;

        if (view.getBackButton() != null) {
            view.getBackButton().setOnAction(this::handleBack);
        }
        if (view.getHomeButton() != null) {
            view.getHomeButton().setOnAction(this::handleHome);
        }
        if (view.getViewSchedulesButton() != null) {
            view.getViewSchedulesButton().setOnAction(this::handleViewSchedules);
        }
    }

    private void initializeFilters() {
        if (view == null) return;

        // Initialize rating filter (1-10)
        if (view.getRatingFilterComboBox() != null) {
            javafx.collections.ObservableList<Integer> ratings =
                    javafx.collections.FXCollections.observableArrayList();
            ratings.add(0); // "All Ratings"
            for (int i = 1; i <= 10; i++) {
                ratings.add(i);
            }
            view.getRatingFilterComboBox().setItems(ratings);
            view.getRatingFilterComboBox().setValue(0);
        }

        // Add listeners for filters
        if (view.getGenreFilterComboBox() != null) {
            view.getGenreFilterComboBox().setOnAction(e -> applyFilters());
        }
        if (view.getRatingFilterComboBox() != null) {
            view.getRatingFilterComboBox().setOnAction(e -> applyFilters());
        }
        if (view.getDateFilterPicker() != null) {
            view.getDateFilterPicker().setOnAction(e -> applyFilters());
        }
    }

    // ===== DATA LOADING =====

    private void loadAllGenres() {
        try {
            Genre[] allGenres = model.browseGenres();
            if (allGenres == null || allGenres.length == 0) {
                showStatus("No genres available.");
                return;
            }

            genreDataMap.clear();
            javafx.collections.ObservableList<String> genreNames =
                    javafx.collections.FXCollections.observableArrayList();
            genreNames.add("All Genres");

            for (Genre genre : allGenres) {
                genreDataMap.put(genre.genreId, genre);
                genreNames.add(genre.genreName);
            }

            if (view.getGenreFilterComboBox() != null) {
                view.getGenreFilterComboBox().setItems(genreNames);
                view.getGenreFilterComboBox().setValue("All Genres");
            }

        } catch (Exception e) {
            showStatus("Error loading genres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllMovies() {
        try {
            Movie[] allMovies = model.browseMovies();
            if (allMovies == null || allMovies.length == 0) {
                showStatus("No movies found.");
                return;
            }

            movieDataMap.clear();
            for (Movie movie : allMovies) {
                movieDataMap.put(movie.movieId, movie);
            }

            displayMovieCards(allMovies);
            showStatus("Movies loaded successfully. Total: " + allMovies.length);

        } catch (Exception e) {
            showStatus("Error loading movies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayMovieCards(Movie[] movies) {
        if (view == null || view.getMovieCardsContainer() == null) return;

        view.clearMovieCards();

        for (Movie movie : movies) {
            VBox movieCard = createMovieCard(movie);
            view.getMovieCardsContainer().getChildren().add(movieCard);
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(300);
        card.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 10; " +
                "-fx-border-color: #444444; -fx-border-radius: 10; -fx-padding: 15;");

        // Movie poster placeholder
        Label posterLabel = new Label("🎬");
        posterLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: #FFD700;");

        // Movie title
        Label titleLabel = new Label(movie.movieTitle);
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);
        titleLabel.setAlignment(Pos.CENTER);

        // Movie rating
        Label ratingLabel = new Label("⭐ " + movie.movieRating + "/10");
        ratingLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px;");

        // Get genres
        String genresText = getGenreNamesForMovie(movie.movieId);
        Label genresLabel = new Label(genresText);
        genresLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 11px; " +
                "-fx-wrap-text: true;");
        genresLabel.setMaxWidth(180);
        genresLabel.setAlignment(Pos.CENTER);

        // View Details button
        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: #007ACC; -fx-text-fill: WHITE; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> handleMovieCardClick(movie));

        card.getChildren().addAll(posterLabel, titleLabel, ratingLabel, genresLabel, detailsBtn);
        VBox.setMargin(detailsBtn, new Insets(10, 0, 0, 0));

        return card;
    }

    private String getGenreNamesForMovie(int movieId) {
        try {
            Genre[] genres = model.getMovieGenres(movieId);
            if (genres == null || genres.length == 0) {
                return "No genres";
            }
            return Arrays.stream(genres)
                    .map(g -> g.genreName)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ===== EVENT HANDLERS =====

    public void handleSearchClick(ActionEvent event) {
        String searchText = view.getSearchField().getText().trim();

        if (searchText.isEmpty()) {
            loadAllMovies();
            return;
        }

        try {
            Movie[] searchResults = model.searchMovies(searchText);
            if (searchResults == null || searchResults.length == 0) {
                showStatus("No movies found matching '" + searchText + "'");
                view.clearMovieCards();
            } else {
                displayMovieCards(searchResults);
                showStatus("Found " + searchResults.length + " movie(s)");
            }
        } catch (Exception e) {
            showStatus("Error searching movies: " + e.getMessage());
        }
    }

    public void handleClearFiltersClick(ActionEvent event) {
        view.clearFilters();
        loadAllMovies();
        view.hideMovieDetails();
        showStatus("Filters cleared");
    }

    private void applyFilters() {
        try {
            Movie[] filteredMovies = null;

            // Apply genre filter
            String selectedGenre = view.getGenreFilterComboBox().getValue();
            if (selectedGenre != null && !selectedGenre.equals("All Genres")) {
                int genreId = findGenreIdByName(selectedGenre);
                if (genreId != -1) {
                    filteredMovies = model.filterByGenreIds(new int[]{genreId});
                }
            }

            // Apply rating filter
            Integer selectedRating = view.getRatingFilterComboBox().getValue();
            if (selectedRating != null && selectedRating > 0) {
                Movie[] ratingFiltered = model.filterByRating(selectedRating);
                filteredMovies = intersectMovies(filteredMovies, ratingFiltered);
            }

            // Apply date filter
            LocalDate selectedDate = view.getDateFilterPicker().getValue();
            if (selectedDate != null) {
                Movie[] dateFiltered = model.getMoviesByDate(
                        selectedDate.getDayOfMonth(),
                        selectedDate.getMonthValue(),
                        selectedDate.getYear()
                );
                filteredMovies = intersectMovies(filteredMovies, dateFiltered);
            }

            // Display results
            if (filteredMovies == null) {
                loadAllMovies();
            } else {
                displayMovieCards(filteredMovies);
                showStatus("Filtered results: " + filteredMovies.length + " movie(s)");
            }

        } catch (Exception e) {
            showStatus("Error applying filters: " + e.getMessage());
        }
    }

    private Movie[] intersectMovies(Movie[] list1, Movie[] list2) {
        if (list1 == null) return list2;
        if (list2 == null) return list1;

        Set<Integer> ids1 = Arrays.stream(list1)
                .map(m -> m.movieId)
                .collect(Collectors.toSet());

        return Arrays.stream(list2)
                .filter(m -> ids1.contains(m.movieId))
                .toArray(Movie[]::new);
    }

    private void handleMovieCardClick(Movie movie) {
        currentlySelectedMovie = movie;

        // Update details panel
        String genresText = getGenreNamesForMovie(movie.movieId);
        view.updateMovieDetails(
                movie.movieTitle,
                "⭐ " + movie.movieRating + "/10",
                "Genres: " + genresText,
                movie.movieDescription
        );

        // Load poster image if available
        if (view.getMoviePosterImageView() != null &&
                movie.moviePosterURL != null &&
                !movie.moviePosterURL.isEmpty()) {
            try {
                Image posterImage = new Image(movie.moviePosterURL, true);
                view.getMoviePosterImageView().setImage(posterImage);
            } catch (Exception e) {
                // Handle image load error silently
            }
        }

        view.showMovieDetails();
        showStatus("Selected: " + movie.movieTitle);
    }

    public void handleViewSchedules(ActionEvent event) {
        if (currentlySelectedMovie == null) {
            showStatus("Please select a movie first");
            return;
        }

        try {
            Schedule[] schedules = model.getMovieSchedules(currentlySelectedMovie.movieId);
            if (schedules == null || schedules.length == 0) {
                showStatus("No schedules available for this movie");
                return;
            }

            // Navigate to schedule selection
            if (movieNavigationManager != null) {
                movieNavigationManager.showScheduleSelection(currentlySelectedMovie.movieId);
            }

        } catch (MovieNotFoundException e) {
            showStatus("Error: Movie not found - " + e.message);
        } catch (Exception e) {
            showStatus("Error loading schedules: " + e.getMessage());
        }
    }

    public void handleBack(ActionEvent event) {
        if (view.getMovieDetailsPanel() != null &&
                view.getMovieDetailsPanel().isVisible()) {
            view.hideMovieDetails();
            currentlySelectedMovie = null;
        } else if (movieNavigationManager != null) {
            movieNavigationManager.goBack();
        }
    }

    public void handleHome(ActionEvent event) {
        if (movieNavigationManager != null) {
            movieNavigationManager.goHome();
        }
    }

    // ===== HELPER METHODS =====

    private int findGenreIdByName(String genreName) {
        for (Genre genre : genreDataMap.values()) {
            if (genre.genreName.equals(genreName)) {
                return genre.genreId;
            }
        }
        return -1;
    }

    private void showStatus(String message) {
        if (view != null) {
            view.showStatusMessage(message, false);
        }
    }

    // ===== GETTERS =====

    public CustomerMovieView getView() {
        return view;
    }

    public void setView(CustomerMovieView view) {
        this.view = view;
    }
}