package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MovieController - Controller for Movie Management
 * Handles all business logic and file uploads
 */
public class MovieController {

    private final MovieModel model;
    private final MovieView view;
    private final int currentAdminId;

    // Data caches
    private final Map<Integer, Movie> movieDataMap = new HashMap<>();
    private final Map<Integer, Genre> genreDataMap = new HashMap<>();
    private List<Movie> allMoviesList = new ArrayList<>();
    private List<Movie> filteredMoviesList = new ArrayList<>();

    // Pagination
    private static final int MOVIES_PER_PAGE = 8; // 2 rows x 4 columns
    private int currentPage = 1;
    private int totalPages = 1;

    // Selection tracking
    private Movie currentlySelectedMovie = null;
    private int selectedMovieId = -1;

    // Image upload tracking
    private String selectedImagePath = null;
    private String uploadedImageURL = null;

    // View states
    private enum ViewState { GRID, ADD, EDIT }
    private ViewState currentViewState = ViewState.GRID;

    // Image storage directory
    private static final String IMAGE_DIRECTORY = "resources/Images/MovieImage/";

    // ===== CONSTRUCTOR =====

    public MovieController(MovieModel model, MovieView view, int adminId) {
        this.model = model;
        this.view = view;
        this.currentAdminId = adminId;

        System.out.println("🎬 MovieController created (admin ID: " + adminId + ")");
    }

    // ===== PUBLIC INITIALIZATION METHOD =====

    /**
     * Called by AdminMMNavManager AFTER FXML is loaded
     * This is when we wire up all the buttons
     */
    public void initialize() {
        System.out.println("🔧 MovieController.initialize() called - wiring buttons now");

        // Verify FXML elements are loaded
        if (view.getBtnAddNewMovie() == null) {
            System.err.println("❌ CRITICAL ERROR: FXML elements not loaded!");
            return;
        }

        initializeComboBoxes();
        initializeButtons();
        showGridViewInternal();
        loadInitialData();

        System.out.println("✅ MovieController fully initialized");
    }

    // ===== INITIALIZATION =====

    private void initializeButtons() {
        System.out.println("🔘 Wiring buttons...");

        // Navigation
        view.getBtnAddNewMovie().setOnAction(event -> handleAddNewMovieClick());
        view.getBtnEdit().setOnAction(event -> handleEditClick());
        view.getBtnBackToGrid().setOnAction(event -> handleBackToGridClick());
        view.getBtnBackToGridFromEdit().setOnAction(event -> handleBackToGridFromEditClick());

        // Search
        view.getBtnSearch().setOnAction(event -> handleSearchClick());
        view.getBtnFilter().setOnAction(event -> handleFilterClick());

        // Pagination
        view.getBtnPreviousPage().setOnAction(event -> handlePreviousPageClick());
        view.getBtnNextPage().setOnAction(event -> handleNextPageClick());

        // CRUD
        view.getBtnSaveMovie().setOnAction(event -> handleSaveMovieClick());
        view.getBtnUpdateMovie().setOnAction(event -> handleUpdateMovieClick());
        view.getBtnDeleteMovie().setOnAction(event -> handleDeleteMovieClick());
        view.getBtnClearFields().setOnAction(event -> handleClearFieldsClick());

        // 🆕 Image Upload Button
        if (view.getBtnBrowsePoster() != null) {
            view.getBtnBrowsePoster().setOnAction(event -> handleBrowsePosterClick());
            view.getBtnBrowsePoster().setVisible(true);
            view.getBtnBrowsePoster().setManaged(true);
        }

        System.out.println("✅ All buttons wired successfully");
    }

    private void initializeComboBoxes() {
        if (view.getRatingComboBox() == null || view.getStatusComboBox() == null) {
            System.err.println("❌ ERROR: ComboBoxes not loaded yet!");
            return;
        }

        // Rating 1-10
        List<Integer> ratings = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            ratings.add(i);
        }
        view.getRatingComboBox().getItems().addAll(ratings);

        // Status
        List<String> statuses = Arrays.asList("Now Showing", "Coming Soon", "Ended");
        view.getStatusComboBox().getItems().addAll(statuses);

        System.out.println("✅ ComboBoxes initialized");
    }

    private void loadInitialData() {
        System.out.println("📊 Loading initial data...");
        loadAllGenres();
        loadAllMovies();
    }

    // ===== VIEW SWITCHING =====

    private void showGridViewInternal() {
        currentViewState = ViewState.GRID;
        view.showGridView();
        view.clearAllFields();
        currentlySelectedMovie = null;
        selectedMovieId = -1;
        selectedImagePath = null;
        uploadedImageURL = null;
        System.out.println("📊 Switched to Grid View");
    }

    private void showAddViewInternal() {
        currentViewState = ViewState.ADD;
        view.showAddView();
        view.clearAllFields();
        currentlySelectedMovie = null;
        selectedMovieId = -1;
        selectedImagePath = null;
        uploadedImageURL = null;

        // Show browse button
        if (view.getBtnBrowsePoster() != null) {
            view.getBtnBrowsePoster().setVisible(true);
            view.getBtnBrowsePoster().setManaged(true);
        }

        System.out.println("➕ Switched to Add View");
    }

    private void showEditViewInternal(Movie movie) {
        currentViewState = ViewState.EDIT;
        view.showEditView();
        populateFieldsWithMovie(movie);

        // Show browse button
        if (view.getBtnBrowsePoster() != null) {
            view.getBtnBrowsePoster().setVisible(true);
            view.getBtnBrowsePoster().setManaged(true);
        }

        System.out.println("✏️ Switched to Edit View - Movie ID: " + movie.movieId);
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
            List<String> genreNames = new ArrayList<>();

            for (Genre genre : allGenres) {
                genreDataMap.put(genre.genreId, genre);
                genreNames.add(genre.genreName);
            }

            view.getGenreComboBox().getItems().clear();
            view.getGenreComboBox().getItems().addAll(genreNames);
            System.out.println("✅ Loaded " + allGenres.length + " genres");

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
            System.err.println("❌ Authentication error loading genres");
        } catch (Exception e) {
            view.showStatusMessage("Error loading genres: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void loadAllMovies() {
        try {
            Movie[] allMovies = model.getAllMovies();
            if (allMovies == null || allMovies.length == 0) {
                allMoviesList.clear();
                filteredMoviesList.clear();
                updateMovieGrid();
                view.showStatusMessage("No movies found in the system.", false);
                return;
            }

            movieDataMap.clear();
            allMoviesList.clear();

            for (Movie movie : allMovies) {
                movieDataMap.put(movie.movieId, movie);
                allMoviesList.add(movie);
            }

            filteredMoviesList = new ArrayList<>(allMoviesList);
            currentPage = 1;
            updateMovieGrid();

            System.out.println("✅ Loaded " + allMovies.length + " movies");

        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed. Please log in again.", true);
            System.err.println("❌ Authentication error loading movies");
        } catch (Exception e) {
            view.showStatusMessage("Error loading movies: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // ===== MOVIE GRID RENDERING =====

    private void updateMovieGrid() {
        view.clearMovieGrid();

        // Calculate pagination
        totalPages = (int) Math.ceil((double) filteredMoviesList.size() / MOVIES_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        // Update page label
        view.updatePageLabel(currentPage);

        // Enable/disable pagination buttons
        view.setPaginationButtons(currentPage > 1, currentPage < totalPages);

        // Get movies for current page
        int startIndex = (currentPage - 1) * MOVIES_PER_PAGE;
        int endIndex = Math.min(startIndex + MOVIES_PER_PAGE, filteredMoviesList.size());

        List<Movie> moviesOnPage = filteredMoviesList.subList(startIndex, endIndex);

        // Create cards (2 rows x 4 columns)
        int row = 0;
        int col = 0;

        for (Movie movie : moviesOnPage) {
            String genreNames = getGenreNamesForMovie(movie.movieId);

            final int movieId = movie.movieId;
            final Movie currentMovie = movie;

            Runnable clickAction = () -> {
                currentlySelectedMovie = currentMovie;
                selectedMovieId = movieId;
                showEditViewInternal(currentMovie);
            };

            // View creates and adds the card with poster image
            view.createAndAddMovieCard(
                    movie.movieId,
                    movie.movieTitle,
                    genreNames,
                    movie.movieRating,
                    movie.moviePosterURL, // Pass poster URL
                    clickAction,
                    col,
                    row
            );

            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }

        System.out.println("📊 Grid updated: Page " + currentPage + "/" + totalPages +
                " (" + moviesOnPage.size() + " movies)");
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

    // ===== IMAGE UPLOAD HANDLING =====

    private void handleBrowsePosterClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Get current stage
        Stage stage = (Stage) view.getPosterUrlInput().getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            view.getPosterUrlInput().setText(selectedFile.getName());
            view.showStatusMessage("Image selected: " + selectedFile.getName(), false);
            System.out.println("🖼️ Selected image: " + selectedImagePath);
        }
    }

    private String uploadImage() throws IOException {
        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            return null;
        }

        // Create directory if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename
        String extension = selectedImagePath.substring(selectedImagePath.lastIndexOf("."));
        String newFileName = "movie_" + System.currentTimeMillis() + extension;
        String destinationPath = IMAGE_DIRECTORY + newFileName;

        // Copy file
        Path source = Paths.get(selectedImagePath);
        Path destination = Paths.get(destinationPath);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✅ Image uploaded: " + destinationPath);
        return destinationPath;
    }

    // ===== EVENT HANDLERS =====

    private void handleAddNewMovieClick() {
        showAddViewInternal();
    }

    private void handleBackToGridClick() {
        showGridViewInternal();
    }

    private void handleBackToGridFromEditClick() {
        showGridViewInternal();
    }

    private void handlePreviousPageClick() {
        if (currentPage > 1) {
            currentPage--;
            updateMovieGrid();
            System.out.println("⬅️ Page " + currentPage);
        }
    }

    private void handleNextPageClick() {
        if (currentPage < totalPages) {
            currentPage++;
            updateMovieGrid();
            System.out.println("➡️ Page " + currentPage);
        }
    }

    private void handleSearchClick() {
        String searchText = view.getSearchField().getText().trim();

        if (searchText.isEmpty()) {
            filteredMoviesList = new ArrayList<>(allMoviesList);
        } else {
            filteredMoviesList = allMoviesList.stream()
                    .filter(movie ->
                            movie.movieTitle.toLowerCase().contains(searchText.toLowerCase()) ||
                                    String.valueOf(movie.movieId).equals(searchText))
                    .collect(Collectors.toList());
        }

        currentPage = 1;
        updateMovieGrid();

        System.out.println("🔍 Search: '" + searchText + "' - Found " + filteredMoviesList.size() + " results");
    }

    private void handleFilterClick() {
        view.showStatusMessage("Filter feature coming soon!", false);
        System.out.println("🔧 Filter button clicked (not implemented yet)");
    }

    private void handleEditClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showStatusMessage("Please select a movie card first.", true);
            return;
        }
        showEditViewInternal(currentlySelectedMovie);
    }

    private void handleSaveMovieClick() {
        view.clearStatusMessage();

        // Validate
        String title = view.getTitleInput().getText().trim();
        String description = view.getDescriptionInput().getText().trim();
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
            // Upload image if selected
            String posterURL = "";
            if (selectedImagePath != null) {
                posterURL = uploadImage();
                System.out.println("✅ Image uploaded to: " + posterURL);
            }

            int genreId = findGenreIdByName(selectedGenreName);
            if (genreId == -1) {
                view.showStatusMessage("Error: Selected genre not found.", true);
                return;
            }

            int[] genreIds = new int[]{genreId};
            int newMovieId = model.addMovie(title, genreIds, description, rating, posterURL);

            view.showStatusMessage("Movie added successfully! ID: " + newMovieId, false);
            loadAllMovies();
            showGridViewInternal();

            System.out.println("✅ Movie added: " + title + " (ID: " + newMovieId + ")");

        } catch (IOException e) {
            view.showStatusMessage("Error uploading image: " + e.getMessage(), true);
            e.printStackTrace();
        } catch (AuthenticationException e) {
            view.showStatusMessage("Error: Authentication failed.", true);
            System.err.println("❌ Authentication error");
        } catch (InvalidInputException e) {
            view.showStatusMessage("Error: Invalid input - " + e.message, true);
        } catch (Exception e) {
            view.showStatusMessage("Error adding movie: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void handleUpdateMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showStatusMessage("Please select a movie to update.", true);
            return;
        }

        try {
            String newTitle = view.getTitleInput().getText().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(currentlySelectedMovie.movieTitle)) {
                model.updateMovieTitle(selectedMovieId, newTitle);
            }

            String newDescription = view.getDescriptionInput().getText().trim();
            if (!newDescription.equals(currentlySelectedMovie.movieDescription)) {
                model.updateMovieDescription(selectedMovieId, newDescription);
            }

            Integer newRating = view.getRatingComboBox().getValue();
            if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                model.updateMovieRating(selectedMovieId, newRating);
            }

            // Upload new image if selected
            if (selectedImagePath != null) {
                String newPosterURL = uploadImage();
                model.updateMoviePoster(selectedMovieId, newPosterURL);
                System.out.println("✅ Poster updated: " + newPosterURL);
            }

            String selectedGenreName = view.getGenreComboBox().getValue();
            if (selectedGenreName != null) {
                int genreId = findGenreIdByName(selectedGenreName);
                if (genreId != -1) {
                    model.updateMovieGenres(selectedMovieId, new int[]{genreId});
                }
            }

            view.showStatusMessage("Movie updated successfully!", false);
            loadAllMovies();
            showGridViewInternal();

            System.out.println("✅ Movie updated: ID " + selectedMovieId);

        } catch (IOException e) {
            view.showStatusMessage("Error uploading image: " + e.getMessage(), true);
            e.printStackTrace();
        } catch (Exception e) {
            view.showStatusMessage("Error updating movie: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void handleDeleteMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showStatusMessage("Please select a movie to delete.", true);
            return;
        }

        boolean confirmed = view.showConfirmationDialog(
                "Delete Movie",
                "Are you sure you want to delete '" + currentlySelectedMovie.movieTitle + "'?",
                "This action cannot be undone."
        );

        if (!confirmed) {
            System.out.println("🚫 Delete cancelled by user");
            return;
        }

        try {
            model.deleteMovie(selectedMovieId);
            view.showStatusMessage("Movie deleted successfully!", false);
            loadAllMovies();
            showGridViewInternal();

            System.out.println("✅ Movie deleted: ID " + selectedMovieId);

        } catch (Exception e) {
            view.showStatusMessage("Error deleting movie: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void handleClearFieldsClick() {
        view.clearAllFields();
        selectedImagePath = null;
        uploadedImageURL = null;
        System.out.println("🧹 Fields cleared");
    }

    // ===== HELPER METHODS =====

    private void populateFieldsWithMovie(Movie movie) {
        view.getTitleInput().setText(movie.movieTitle);
        view.getDescriptionInput().setText(movie.movieDescription);
        view.getRatingComboBox().setValue(movie.movieRating);

        // Show current poster URL
        if (movie.moviePosterURL != null && !movie.moviePosterURL.isEmpty()) {
            view.getPosterUrlInput().setText(movie.moviePosterURL);
        }

        try {
            Genre[] genres = model.getMovieGenres(movie.movieId);
            if (genres != null && genres.length > 0) {
                view.getGenreComboBox().setValue(genres[0].genreName);
            }
        } catch (Exception e) {
            // Ignore
        }

        String details = String.format("Movie ID: %d | Rating: %d/10",
                movie.movieId, movie.movieRating);
        view.updateMovieDetailsLabel(details);
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