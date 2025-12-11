package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MovieController - NO JavaFX IMPORTS
 * All JavaFX handling is done in View
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
    private static final int MOVIES_PER_PAGE = 8;
    private int currentPage = 1;
    private int totalPages = 1;

    // Selection tracking
    private Movie currentlySelectedMovie = null;
    private int selectedMovieId = -1;

    // View states
    private enum ViewState { GRID, ADD, EDIT }
    private ViewState currentViewState = ViewState.GRID;

    // Image storage directory
    private static final String IMAGE_DIRECTORY = "Images/MovieImage/";

    public MovieController(MovieModel model, MovieView view, int adminId) {
        this.model = model;
        this.view = view;
        this.currentAdminId = adminId;
        System.out.println("🎬 MovieController created (admin ID: " + adminId + ")");
    }

    public void initialize() {
        System.out.println("🔧 MovieController.initialize() called");

        if (view.getBtnAddNewMovie() == null) {
            System.err.println("❌ CRITICAL ERROR: FXML elements not loaded!");
            return;
        }

        // IMPORTANT: Load data FIRST before showing views
        loadInitialData();

        // Then setup UI
        initializeButtons();
        setupLiveSearch();
        showGridViewInternal();

        System.out.println("✅ MovieController fully initialized");
    }

    private void initializeButtons() {
        System.out.println("🔘 Wiring buttons...");

        // Navigation
        view.getBtnAddNewMovie().setOnAction(event -> handleAddNewMovieClick());
        view.getBtnBackToGrid().setOnAction(event -> handleBackToGridClick());
        view.getBtnBackToGridFromEdit().setOnAction(event -> handleBackToGridFromEditClick());

        // Pagination
        view.getBtnPreviousPage().setOnAction(event -> handlePreviousPageClick());
        view.getBtnNextPage().setOnAction(event -> handleNextPageClick());

        // CRUD
        if (view.getBtnAddMovie() != null) {
            view.getBtnAddMovie().setOnAction(event -> handleAddMovieClick());
        }

        view.getBtnUpdateMovie().setOnAction(event -> handleUpdateMovieClick());
        view.getBtnDeleteMovie().setOnAction(event -> handleDeleteMovieClick());
        view.getBtnClearFields().setOnAction(event -> handleClearFieldsClick());

        // Schedule management
        view.setAddScheduleButtonAction(this::handleAddNewScheduleClick);

        System.out.println("✅ All buttons wired successfully");
    }

    /**
     * Setup live search - filter while typing
     */
    private void setupLiveSearch() {
        if (view.getSearchField() != null) {
            view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
                handleLiveSearch(newValue.trim());
            });
            System.out.println("✅ Live search configured");
        }
    }

    private void handleLiveSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            System.out.println("🔄 Search blank - refreshing from database");
            loadAllMovies();
            return;
        }

        searchTerm = searchTerm.toLowerCase();
        String finalSearchTerm = searchTerm;

        filteredMoviesList = allMoviesList.stream()
                .filter(movie ->
                        movie.movieTitle.toLowerCase().contains(finalSearchTerm) ||
                                String.valueOf(movie.movieId).contains(finalSearchTerm))
                .collect(Collectors.toList());

        currentPage = 1;
        updateMovieGrid();

        if (filteredMoviesList.isEmpty()) {
            view.showStatusMessage("No movies found matching '" + finalSearchTerm + "'", false);
        } else {
            view.showStatusMessage("Found " + filteredMoviesList.size() + " movie(s)", false);
        }
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
        System.out.println("📊 Switched to Grid View");
    }

    private void showAddViewInternal() {
        currentViewState = ViewState.ADD;
        view.showAddView();
        view.clearAllFields();

        // IMPORTANT: Clear genre selections for ADD mode
        view.clearGenreSelection();

        // Reset schedule date to today
        if (view.getScheduleDatePicker() != null) {
            view.getScheduleDatePicker().setValue(LocalDate.now());
        }

        currentlySelectedMovie = null;
        selectedMovieId = -1;
        System.out.println("➕ Switched to Add View - all genres unchecked");
    }

    private void showEditViewInternal(Movie movie) {
        currentViewState = ViewState.EDIT;
        view.showEditView();
        populateFieldsWithMovie(movie);

        // Load schedules for this movie
        loadSchedulesForMovie(movie.movieId);

        System.out.println("✏️ Switched to Edit View - Movie ID: " + movie.movieId);
    }

    // ===== DATA LOADING =====

    private void loadAllGenres() {
        try {
            Genre[] allGenres = model.getAllGenres();
            if (allGenres == null || allGenres.length == 0) {
                view.showWarningAlert("No Genres Found", "Please add genres first before creating movies.");
                return;
            }

            genreDataMap.clear();
            List<String> genreNames = new ArrayList<>();

            for (Genre genre : allGenres) {
                genreDataMap.put(genre.genreId, genre);
                genreNames.add(genre.genreName);
            }

            view.populateGenreList(genreNames);

            System.out.println("✅ Loaded " + allGenres.length + " genres: " + genreNames);

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error Loading Genres", "Failed to load genres: " + e.getMessage());
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
                view.showStatusMessage("No movies found. Click '+ Add Movie' to create one.", false);
                return;
            }

            allMoviesList = new ArrayList<>(Arrays.asList(allMovies));
            filteredMoviesList = new ArrayList<>(allMoviesList);

            allMoviesList.forEach(movie -> movieDataMap.put(movie.movieId, movie));

            currentPage = 1;
            updateMovieGrid();

            view.showStatusMessage("✅ Loaded " + allMovies.length + " movies", false);
            System.out.println("✅ Loaded " + allMovies.length + " movies");

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error Loading Movies", "Failed to load movies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display schedules for currently selected movie in edit view
     * Uses getSchedulesByMovie and commonServices to get full details
     */
    private void loadSchedulesForMovie(int movieId) {
        try {
            System.out.println("📅 Loading schedules for movie ID: " + movieId);

            // Get schedules using model
            Schedule[] schedules = model.getSchedulesByMovie(movieId);

            if (schedules == null || schedules.length == 0) {
                view.populateScheduleList(new ArrayList<>(), this::handleScheduleSave, this::handleScheduleDelete);
                System.out.println("📅 No schedules found for movie ID: " + movieId);
                return;
            }

            List<Map<String, Object>> scheduleDataList = new ArrayList<>();

            // For each schedule, get the MovieSchedule details using commonServices
            for (Schedule schedule : schedules) {
                try {
                    // Use commonServices to get MovieSchedule details
                    MovieSchedule movieSchedule = AdminParentModel.commonServices.getMovieScheduleById(schedule.movieScheduleId);

                    if (movieSchedule != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("scheduleId", schedule.scheduleId);
                        data.put("movieScheduleId", schedule.movieScheduleId);
                        data.put("screeningDate", movieSchedule.screeningDate);
                        data.put("screeningTime", movieSchedule.screeningTime);
                        scheduleDataList.add(data);

                        System.out.println("📅 Loaded schedule " + schedule.scheduleId +
                                " - Date: " + movieSchedule.screeningDate +
                                " Time: " + movieSchedule.screeningTime);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error loading movie schedule " + schedule.movieScheduleId + ": " + e.getMessage());
                }
            }

            // Pass data to view with action handlers
            view.populateScheduleList(scheduleDataList, this::handleScheduleSave, this::handleScheduleDelete);
            System.out.println("✅ Loaded " + scheduleDataList.size() + " schedule(s) for movie " + movieId);

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (MovieNotFoundException e) {
            view.showErrorAlert("Movie Not Found", "Could not load schedules for this movie.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle schedule save button click
     */
    private void handleScheduleSave(Integer scheduleId, String[] dateTime) {
        String newDate = dateTime[0];
        String newTime = dateTime[1];

        if (newDate == null || newDate.isEmpty() || newTime == null || newTime.isEmpty()) {
            view.showWarningAlert("Validation Error", "Please provide both date and time.");
            return;
        }

        if (!isValidTimeFormat(newTime)) {
            view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30, 19:00)");
            return;
        }

        try {
            model.updateScheduleDateTime(scheduleId, newDate, newTime);

            view.showInfoAlert("Success!", "✅ Schedule updated successfully!");

            // Reload schedules to reflect changes
            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

            System.out.println("✅ Updated schedule " + scheduleId + " to " + newDate + " at " + newTime);

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Schedule Not Found", "This schedule no longer exists.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Update Error", "Failed to update schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle schedule delete button click
     */
    private void handleScheduleDelete(Integer scheduleId) {
        boolean confirmed = view.showConfirmationDialog(
                "Delete Schedule",
                "Are you sure you want to delete this schedule?",
                "Schedule ID: " + scheduleId + "\nThis action cannot be undone."
        );

        if (!confirmed) {
            System.out.println("🚫 Schedule delete cancelled");
            return;
        }

        try {
            model.deleteSchedule(scheduleId);
            view.showInfoAlert("Success!", "✅ Schedule deleted successfully!");

            // Reload schedules to reflect changes
            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

            System.out.println("✅ Deleted schedule " + scheduleId);

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Schedule Not Found", "This schedule no longer exists.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Delete Error", "Failed to delete schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add new schedule button click
     */
    private void handleAddNewScheduleClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showWarningAlert("No Movie Selected", "Please select a movie first.");
            return;
        }

        // Show input dialog for new schedule
        String dateInput = view.showInputDialog("Add New Schedule", "Enter screening date (yyyy-MM-dd):", LocalDate.now().toString());
        if (dateInput == null || dateInput.trim().isEmpty()) {
            return;
        }

        String timeInput = view.showInputDialog("Add New Schedule", "Enter screening time (HH:mm):", "14:00");
        if (timeInput == null || timeInput.trim().isEmpty()) {
            return;
        }

        if (!isValidTimeFormat(timeInput)) {
            view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30, 19:00)");
            return;
        }

        try {
            // Use combined method from model
            int scheduleId = model.createAndAssignSchedule(selectedMovieId, dateInput.trim(), timeInput.trim());

            view.showInfoAlert("Success!", "✅ New schedule added!\nSchedule ID: " + scheduleId);

            // Reload schedules
            loadSchedulesForMovie(selectedMovieId);

            System.out.println("✅ Created new schedule " + scheduleId + " for movie " + selectedMovieId);

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (InvalidInputException e) {
            view.showErrorAlert("Invalid Input", e.message);
            e.printStackTrace();
        } catch (MovieNotFoundException e) {
            view.showErrorAlert("Movie Not Found", "The selected movie no longer exists.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to create schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMovieGrid() {
        view.clearMovieGrid();

        if (filteredMoviesList.isEmpty()) {
            view.showStatusMessage("No movies to display.", false);
            view.updatePageLabel(0);
            view.setPaginationButtons(false, false);
            return;
        }

        totalPages = (int) Math.ceil((double) filteredMoviesList.size() / MOVIES_PER_PAGE);
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startIdx = (currentPage - 1) * MOVIES_PER_PAGE;
        int endIdx = Math.min(startIdx + MOVIES_PER_PAGE, filteredMoviesList.size());

        int col = 0, row = 0;
        for (int i = startIdx; i < endIdx; i++) {
            Movie movie = filteredMoviesList.get(i);
            String genres = getGenreNamesForMovie(movie.movieId);

            // Create card and add to grid (card is Object type to avoid JavaFX imports)
            Object card = view.createMovieCard(
                    movie.movieTitle,
                    genres,
                    movie.movieRating,
                    movie.moviePosterURL,
                    () -> handleMovieCardClick(movie)
            );

            view.addCardToGrid(card, col, row);

            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }

        view.updatePageLabel(currentPage);
        view.setPaginationButtons(currentPage > 1, currentPage < totalPages);

        System.out.println("📊 Grid updated: Page " + currentPage + "/" + totalPages);
    }

    private String getGenreNamesForMovie(int movieId) {
        try {
            Genre[] genres = model.getMovieGenres(movieId);
            if (genres == null || genres.length == 0) return "No genres";

            return Arrays.stream(genres)
                    .map(g -> g.genreName)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ===== BUTTON HANDLERS =====

    private void handleAddNewMovieClick() {
        showAddViewInternal();
    }

    private void handleMovieCardClick(Movie movie) {
        currentlySelectedMovie = movie;
        selectedMovieId = movie.movieId;
        System.out.println("🎬 Movie card clicked: " + movie.movieTitle + " (ID: " + movie.movieId + ")");
        showEditViewInternal(movie);
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
        }
    }

    private void handleNextPageClick() {
        if (currentPage < totalPages) {
            currentPage++;
            updateMovieGrid();
        }
    }

    private String uploadImage() throws IOException {
        String selectedImagePath = view.getSelectedImagePath();

        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            return "";
        }

        Path sourcePath = Paths.get(selectedImagePath);
        String fileName = System.currentTimeMillis() + "_" + sourcePath.getFileName().toString();

        Path targetDir = Paths.get("src/main/resources/" + IMAGE_DIRECTORY);
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(fileName);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return IMAGE_DIRECTORY + fileName;
    }

    // ===== CRUD OPERATIONS =====

    private void handleAddMovieClick() {
        System.out.println("🎬 === ADD MOVIE PROCESS STARTED ===");

        // Validate inputs
        String title = view.getTitleInput().getText().trim();
        if (title.isEmpty()) {
            view.showWarningAlert("Validation Error", "Please enter a movie title.");
            return;
        }

        Integer rating = view.getRatingComboBox().getValue();
        if (rating == null) {
            view.showWarningAlert("Validation Error", "Please select a rating.");
            return;
        }

        List<String> selectedGenreNames = view.getSelectedGenres();
        if (selectedGenreNames.isEmpty()) {
            view.showWarningAlert("Validation Error", "Please select at least one genre.");
            return;
        }

        String description = view.getDescriptionInput().getText().trim();

        // Get schedule information
        LocalDate scheduleDate = view.getScheduleDatePicker().getValue();
        String scheduleTime = view.getScheduleTimeField().getText().trim();

        try {
            // Upload image if selected
            String posterURL = "";
            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                posterURL = uploadImage();
                System.out.println("✅ Image uploaded to: " + posterURL);
            }

            // Convert genre names to IDs
            int[] genreIds = selectedGenreNames.stream()
                    .mapToInt(this::findGenreIdByName)
                    .filter(id -> id != -1)
                    .toArray();

            if (genreIds.length == 0) {
                view.showErrorAlert("Error", "Selected genres not found in database.");
                return;
            }

            // Add movie
            int newMovieId = model.addMovie(title, genreIds, description, rating, posterURL);
            System.out.println("✅ Movie added successfully! New Movie ID: " + newMovieId);

            // Create schedule if provided
            if (scheduleDate != null && scheduleTime != null && !scheduleTime.isEmpty()) {
                if (isValidTimeFormat(scheduleTime)) {
                    String formattedDate = scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    try {
                        int scheduleId = model.createAndAssignSchedule(newMovieId, formattedDate, scheduleTime);

                        view.showInfoAlert("Success!",
                                "✅ Movie added successfully!\n" +
                                        "Movie ID: " + newMovieId + "\n" +
                                        "Schedule ID: " + scheduleId + "\n" +
                                        "Screening: " + formattedDate + " at " + scheduleTime);
                    } catch (Exception e) {
                        view.showWarningAlert("Partial Success",
                                "Movie added (ID: " + newMovieId + ") but schedule creation failed: " + e.getMessage());
                    }
                } else {
                    view.showInfoAlert("Success!",
                            "Movie added (ID: " + newMovieId + ") but invalid time format. Use HH:mm format.");
                }
            } else {
                view.showInfoAlert("Success!", "✅ Movie added successfully! ID: " + newMovieId);
            }

            loadAllMovies();
            showGridViewInternal();

        } catch (IOException e) {
            view.showErrorAlert("Image Upload Error", "Failed to upload image: " + e.getMessage());
            e.printStackTrace();
        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidInputException e) {
            view.showErrorAlert("Invalid Input", e.message);
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to add movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleUpdateMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showWarningAlert("No Movie Selected", "Please select a movie to update.");
            return;
        }

        System.out.println("✏️ === UPDATE MOVIE PROCESS STARTED ===");
        boolean anyChanges = false;

        try {
            // Update title (use EDIT view field)
            String newTitle = view.getTitleInputEdit().getText().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(currentlySelectedMovie.movieTitle)) {
                model.updateMovieTitle(selectedMovieId, newTitle);
                System.out.println("✅ Title updated to: " + newTitle);
                anyChanges = true;
            }

            // Update description (use EDIT view field)
            String newDescription = view.getDescriptionInputEdit().getText().trim();
            if (!newDescription.equals(currentlySelectedMovie.movieDescription)) {
                model.updateMovieDescription(selectedMovieId, newDescription);
                System.out.println("✅ Description updated");
                anyChanges = true;
            }

            // Update rating (use EDIT view field)
            Integer newRating = view.getRatingComboBoxEdit().getValue();
            if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                model.updateMovieRating(selectedMovieId, newRating);
                System.out.println("✅ Rating updated to: " + newRating);
                anyChanges = true;
            }

            // Update poster
            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                String newPosterURL = uploadImage();
                model.updateMoviePoster(selectedMovieId, newPosterURL);
                System.out.println("✅ Poster updated: " + newPosterURL);
                anyChanges = true;
            }

            // Update genres (getSelectedGenres automatically checks which view is visible)
            List<String> newSelectedGenres = view.getSelectedGenres();
            if (!newSelectedGenres.isEmpty()) {
                Genre[] currentGenres = model.getMovieGenres(selectedMovieId);
                Set<String> currentGenreNames = Arrays.stream(currentGenres)
                        .map(g -> g.genreName)
                        .collect(Collectors.toSet());

                if (!currentGenreNames.equals(new HashSet<>(newSelectedGenres))) {
                    int[] genreIds = newSelectedGenres.stream()
                            .mapToInt(this::findGenreIdByName)
                            .filter(id -> id != -1)
                            .toArray();
                    if (genreIds.length > 0) {
                        model.updateMovieGenres(selectedMovieId, genreIds);
                        System.out.println("✅ Genres updated");
                        anyChanges = true;
                    }
                }
            }

            if (anyChanges) {
                view.showInfoAlert("Success!", "✅ Movie updated successfully!");
                loadAllMovies();
                showGridViewInternal();
            } else {
                view.showInfoAlert("No Changes", "No changes were made to the movie.");
            }

        } catch (Exception e) {
            view.showErrorAlert("Update Error", "Failed to update movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showWarningAlert("No Movie Selected", "Please select a movie to delete.");
            return;
        }

        boolean confirmed = view.showConfirmationDialog(
                "Delete Movie",
                "Are you sure you want to delete '" + currentlySelectedMovie.movieTitle + "'?",
                "This action cannot be undone. All schedules and bookings associated with this movie will be affected."
        );

        if (!confirmed) {
            System.out.println("🚫 Delete cancelled");
            return;
        }

        try {
            model.deleteMovie(selectedMovieId);
            view.showInfoAlert("Success!", "✅ Movie deleted successfully!");
            loadAllMovies();
            showGridViewInternal();

        } catch (Exception e) {
            view.showErrorAlert("Delete Error", "Failed to delete movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClearFieldsClick() {
        view.clearAllFields();

        if (view.getScheduleDatePicker() != null) {
            view.getScheduleDatePicker().setValue(LocalDate.now());
        }

        view.showStatusMessage("Fields cleared", false);
        System.out.println("🧹 Fields cleared");
    }

    // ===== HELPER METHODS =====

    private void populateFieldsWithMovie(Movie movie) {
        // Use EDIT view fields
        view.getTitleInputEdit().setText(movie.movieTitle);
        view.getDescriptionInputEdit().setText(movie.movieDescription);
        view.getRatingComboBoxEdit().setValue(movie.movieRating);

        if (movie.moviePosterURL != null && !movie.moviePosterURL.isEmpty()) {
            view.updateSelectedImageLabelEdit("Current: " + movie.moviePosterURL);
        }

        try {
            Genre[] genres = model.getMovieGenres(movie.movieId);
            if (genres != null && genres.length > 0) {
                List<String> genreNames = Arrays.stream(genres)
                        .map(g -> g.genreName)
                        .collect(Collectors.toList());
                view.selectGenres(genreNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        System.err.println("⚠️ Genre not found: " + genreName);
        return -1;
    }
}