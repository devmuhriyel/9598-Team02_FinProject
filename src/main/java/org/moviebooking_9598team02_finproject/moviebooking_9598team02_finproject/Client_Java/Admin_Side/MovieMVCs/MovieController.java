package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;


import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

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
 * MovieController - Complete with Schedule Management
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

        loadInitialData();
        initializeButtons();
        setupLiveSearch();
        showGridViewInternal();
        // timer
        view.startAutoRefresh(() -> {
            // This code runs every 10 seconds
            if (currentViewState == ViewState.GRID) {
                System.out.println("🔄 Auto-refreshing genres...");
                loadAllGenres();
            }
        });

    }
    // Add this Enum inside the MovieController class (or at the bottom of the file)
    private enum ViewState {
        GRID,
        ADD,
        EDIT
    }

    private void initializeButtons() {

        view.getBtnAddMovie().setOnAction(e ->handleAddMovieClick());
        // Navigation
        view.getBtnAddNewSchedule().setOnAction(e -> handleAddNewScheduleClick());
        view.getBtnAddNewMovie().setOnAction(e -> handleAddNewMovieClick());

        view.getBtnBackToGridFromEdit().setOnAction(event -> handleBackToGridFromEditClick());
        view.getBtnBackToGrid().setOnAction(e -> handleBackToGridClick());
        // Pagination
        view.getBtnPreviousPage().setOnAction(event -> handlePreviousPageClick());
        view.getBtnNextPage().setOnAction(event -> handleNextPageClick());

        // CRUD
        // Add Movie Button (on Grid)
        if (view.getBtnAddNewMovie() != null) {
            view.getBtnAddNewMovie().setOnAction(e -> handleAddNewMovieClick());
        }

        // Back Buttons
        if (view.getBtnBackToGrid() != null) {
            view.getBtnBackToGrid().setOnAction(e -> handleBackToGridClick());
        }
        if (view.getBtnBackToGridFromEdit() != null) {
            view.getBtnBackToGridFromEdit().setOnAction(e -> handleBackToGridFromEditClick()); // or handleBackToGridClick
        }

        view.getBtnUpdateMovie().setOnAction(event -> handleUpdateMovieClick());
        view.getBtnDeleteMovie().setOnAction(event -> handleDeleteMovieClick());
        view.getBtnClearFields().setOnAction(event -> handleClearFieldsClick());

        // Schedule management
        if (view.getBtnAddNewSchedule() != null) {
            view.getBtnAddNewSchedule().setOnAction(event -> handleAddNewScheduleClick());
        }

        System.out.println("✅ All buttons wired successfully");
    }

    private void setupLiveSearch() {
        if (view.getSearchField() != null) {
            view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
                handleLiveSearch(newValue.trim());
            });
        }
    }

    private void handleLiveSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
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
        currentViewState = ViewState.GRID; // <--- Update State
        currentlySelectedMovie = null;
        selectedMovieId = -1;
        view.showGridView();
        view.clearAllFields();
    }

    private void showAddViewInternal() {
        currentViewState = ViewState.ADD;
        view.showAddView();
        view.clearAllFields();
        view.clearGenreSelection();

        if (view.getScheduleDatePicker() != null) {
            view.getScheduleDatePicker().setValue(LocalDate.now());
        }

        currentlySelectedMovie = null;
        selectedMovieId = -1;
    }

    // Update showEditViewInternal
    private void showEditViewInternal(Movie movie) {
        if (movie == null) return;

        currentViewState = ViewState.EDIT;
        loadAllGenres();
        selectedMovieId = movie.movieId;
        currentlySelectedMovie = movie;

        populateFieldsWithMovie(movie);

        view.showEditView();

        // ✅ ADD: Force schedule container to be visible
        view.ensureScheduleContainerVisible();

        // ✅ Load schedules AFTER view is shown
        loadSchedulesForMovie(movie.movieId);
    }

    // ===== DATA LOADING =====

    private void loadAllGenres() {
        try {
            // 1. Fetch from Database
            Genre[] genres = model.getAllGenres();

            // 2. Clear Lists
            List<String> genreNames = new ArrayList<>();
            genreDataMap.clear();

            // 3. Populate
            if (genres != null) {
                for (Genre g : genres) {
                    genreDataMap.put(g.genreId, g);
                    genreNames.add(g.genreName);
                }
            }

            // 4. Update View
            Collections.sort(genreNames);
            view.updateGenreOptions(genreNames);

        } catch (Exception e) {
            view.showErrorAlert("Data Error", "Failed to load genres: " + e.getMessage());
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

        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", "Session expired. Please log in again.");
            e.printStackTrace();
        } catch (Exception e) {
            view.showErrorAlert("Error Loading Movies", "Failed to load movies: " + e.getMessage());
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

    // ===== SCHEDULE MANAGEMENT =====

    /**
     * Load schedules for the currently edited movie
     */
    private void loadSchedulesForMovie(int movieId) {
        try {
            System.out.println("🔍 Loading schedules for movie: " + movieId);
            Schedule[] schedules = model.getSchedulesByMovie(movieId);

            System.out.println("📊 Retrieved " + (schedules != null ? schedules.length : 0) + " schedules");

            view.clearScheduleContainer();

            if (schedules == null || schedules.length == 0) {
                System.out.println("⚠️ No schedules found for movie " + movieId);

                // ✅ ADD: Show a placeholder message
                view.showNoSchedulesMessage();
                return;
            }

            for (int i = 0; i < schedules.length; i++) {
                Schedule schedule = schedules[i];
                System.out.println("📅 Loading schedule " + (i+1) + ": ID=" + schedule.scheduleId);
                loadScheduleCard(schedule);
            }

            System.out.println("✅ Loaded " + schedules.length + " schedule cards");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading schedules: " + e.getMessage());
            view.showErrorAlert("Error", "Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Load a single schedule card with date/time info - FIXED VERSION (Pure Java)
     */
    private void loadScheduleCard(Schedule schedule) {
        try {
            // ✅ Get MovieSchedule details
            MovieSchedule movieSchedule = model.getMovieScheduleDetails(schedule.movieScheduleId);

            String screeningDate = movieSchedule.screeningDate;
            String screeningTime = movieSchedule.screeningTime;

            // ✅ FIX: Use array to hold mutable reference (Java workaround)
            final Object[] cardHolder = new Object[1];

            cardHolder[0] = view.createScheduleCard(
                    schedule.scheduleId,
                    schedule.movieScheduleId,
                    screeningDate,
                    screeningTime,
                    () -> handleScheduleSave(cardHolder[0]),
                    () -> handleScheduleDelete(cardHolder[0])
            );

            view.addScheduleCardToContainer(cardHolder[0]);

        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to load schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle adding a new schedule (inside the Edit Movie screen)
     */
    private void handleAddNewScheduleClick() {
        try {
            // 1. Validate Movie Selection
            if (selectedMovieId == -1) {
                view.showWarningAlert("Error", "No movie selected.");
                return;
            }

            // 2. Get Data (Date + Dropdown Time)
            LocalDate date = view.getScheduleDatePicker().getValue();
            String time = view.getScheduleTimeComboBox().getValue(); // <--- Get from ComboBox

            if (date == null || time == null || time.isEmpty()) {
                view.showWarningAlert("Validation Error", "Please select both a date and a time.");
                return;
            }

            // 3. Format
            String scheduleDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 4. Call Model
            System.out.println("Adding Schedule: " + scheduleDate + " @ " + time);
            int movieScheduleId = model.createMovieSchedule(scheduleDate, time);
            model.assignMovieToSchedule(selectedMovieId, movieScheduleId);

            // 5. Refresh
            view.showInfoAlert("Success", "✅ Schedule added!");
            loadSchedulesForMovie(selectedMovieId);

            // 6. Reset Fields
            view.getScheduleDatePicker().setValue(LocalDate.now());
            view.getScheduleTimeComboBox().getSelectionModel().clearSelection();

        } catch (InvalidInputException e) {
            view.showErrorAlert("Invalid Input", e.message);
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to add schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle schedule save - FIXED VERSION (Pure Java)
     */
    private void handleScheduleSave(Object scheduleCard) {
        try {
            // ✅ Get data from the card (now gets live values from controls)
            Map<String, Object> data = view.getScheduleCardData(scheduleCard);

            if (data == null) {
                view.showWarningAlert("Error", "No schedule data found");
                return;
            }

            int scheduleId = (int) data.get("scheduleId");
            String newDate = (String) data.get("newDate");
            String newTime = (String) data.get("newTime");

            // Validate time format
            if (!isValidTimeFormat(newTime)) {
                view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30)");
                return;
            }

            // ✅ Call model to update
            model.updateScheduleDateTime(scheduleId, newDate, newTime);

            view.showInfoAlert("Success!", "✅ Schedule updated successfully!");

            // ✅ Refresh schedules
            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Not Found", "Schedule not found");
        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", e.message);
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to update schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Handle schedule delete - FIXED VERSION (Pure Java)
     */
    private void handleScheduleDelete(Object scheduleCard) {
        // Get data from the card
        Map<String, Object> data = view.getScheduleCardData(scheduleCard);

        if (data == null) {
            view.showWarningAlert("Error", "No schedule data found");
            return;
        }

        int scheduleId = (int) data.get("scheduleId");

        boolean confirmed = view.showConfirmationDialog(
                "Delete Schedule",
                "Are you sure you want to delete this schedule?",
                "This action cannot be undone. Any bookings for this schedule will be affected."
        );

        if (!confirmed) {
            return;
        }

        try {
            model.deleteSchedule(scheduleId);
            view.showInfoAlert("Success!", "✅ Schedule deleted successfully!");

            // ✅ Refresh schedules
            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Not Found", "Schedule not found");
        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", e.message);
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to delete schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== BUTTON HANDLERS =====

    // Update handleAddNewMovieClick
    private void handleAddNewMovieClick() {
        currentViewState = ViewState.ADD;
        loadAllGenres();
        view.clearAllFields();
        view.showAddView();
    }

    // Inside MovieController.java
    private void handleMovieCardClick(Movie movie) {
        System.out.println("Clicked movie: " + movie.movieTitle);
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

    private void handleAddMovieClick() {
        System.out.println("🎬 === ADD MOVIE PROCESS STARTED ===");

        try {
            // 1. Gather Inputs
            String title = view.getTitleInput().getText().trim();
            String description = view.getDescriptionInput().getText().trim();
            Integer rating = view.getRatingComboBox().getValue();

            // GENRES
            List<String> selectedGenres = view.getSelectedGenresAdd();
            int[] genreIds = getGenreIdsFromNames(selectedGenres);

            // IMAGE
            String posterUrl = "";
            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Path sourceImagePath = Paths.get(imagePath);
                byte[] imageBytes = Files.readAllBytes(sourceImagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String filename = System.currentTimeMillis() + "_" + sourceImagePath.getFileName().toString();

                String targetPath = IMAGE_DIRECTORY + filename;
                posterUrl = targetPath + "|||IMAGE_DATA|||" + base64Image;
            }

            // ✅ NEW: Get Schedule Data
            LocalDate scheduleDate = view.getScheduleDatePicker().getValue();
            String scheduleTime = view.getScheduleTimeComboBox().getValue();

            System.out.println("📅 Schedule Date: " + scheduleDate);
            System.out.println("🕐 Schedule Time: " + scheduleTime);

            // 2. Validate
            if (title.isEmpty()) {
                view.showWarningAlert("Validation Error", "Please enter a movie title.");
                return;
            }
            if (description.isEmpty()) {
                view.showWarningAlert("Validation Error", "Please enter a description.");
                return;
            }
            if (rating == null) {
                view.showWarningAlert("Validation Error", "Please select a rating.");
                return;
            }
            if (genreIds.length == 0) {
                view.showWarningAlert("Validation Error", "Please select at least one genre.");
                return;
            }

            // 3. Add Movie First
            System.out.println("🔄 Creating movie...");
            int newMovieId = model.addMovie(title, genreIds, description, rating, posterUrl);
            System.out.println("✅ Movie created with ID: " + newMovieId);

            // ✅ 4. NEW: Add Schedule if provided (OPTIONAL)
            if (scheduleDate != null && scheduleTime != null && !scheduleTime.isEmpty()) {
                try {
                    System.out.println("🔄 Creating schedule for new movie...");

                    String formattedDate = scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    // Create movie schedule (time slot)
                    int movieScheduleId = model.createMovieSchedule(formattedDate, scheduleTime);
                    System.out.println("✅ Created movieScheduleId: " + movieScheduleId);

                    // Assign movie to schedule
                    int scheduleId = model.assignMovieToSchedule(newMovieId, movieScheduleId);
                    System.out.println("✅ Assigned to scheduleId: " + scheduleId);

                } catch (Exception scheduleError) {
                    System.err.println("⚠️ Failed to create schedule: " + scheduleError.getMessage());
                    // Don't fail the entire operation - movie was created successfully
                    view.showWarningAlert("Partial Success",
                            "Movie created successfully, but schedule creation failed: " + scheduleError.getMessage());
                }
            } else {
                System.out.println("ℹ️ No schedule provided - skipping schedule creation");
            }

            // 5. Success
            view.showInfoAlert("Success", "✅ Movie added successfully! ID: " + newMovieId);

            loadAllMovies(); // Refresh data
            showGridViewInternal(); // Go back to grid

        } catch (Exception e) {
            System.err.println("❌ Error adding movie: " + e.getMessage());
            view.showErrorAlert("Error", "Failed to add movie: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Inside MovieController.java
            private void handleUpdateMovieClick() {
                // 1. Safety Checks
                if (selectedMovieId == -1 || currentlySelectedMovie == null) {
                    view.showWarningAlert("Selection Error", "No movie selected.");
                    return;
                }

                System.out.println("✏️ === UPDATE MOVIE PROCESS STARTED ===");
                boolean anyChanges = false;

                try {
                    // --- TITLE UPDATE ---
                    String newTitle = view.getTitleInputEdit().getText().trim();
                    if (!newTitle.isEmpty() && !newTitle.equals(currentlySelectedMovie.movieTitle)) {
                        model.updateMovieTitle(selectedMovieId, newTitle); //
                        anyChanges = true;
                    }

                    // --- DESCRIPTION UPDATE ---
                    String newDesc = view.getDescriptionInputEdit().getText().trim();
                    if (!newDesc.equals(currentlySelectedMovie.movieDescription)) {
                        model.updateMovieDescription(selectedMovieId, newDesc); //
                        anyChanges = true;
                    }

                    // --- RATING UPDATE ---
                    Integer newRating = view.getRatingComboBoxEdit().getValue();
                    if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                        model.updateMovieRating(selectedMovieId, newRating); //
                        anyChanges = true;
                    }

                    // --- GENRE UPDATE ---
                    // Get genres from the EDIT checkboxes
                    List<String> newSelectedGenres = view.getSelectedGenresEdit();
                    int[] newGenreIds = getGenreIdsFromNames(newSelectedGenres);

                    // Simple check: if list size is different or content differs, update.
                    // (For a perfect check, we'd compare the sets of IDs, but strictly updating is safer here)
                    if (newGenreIds.length > 0) {
                        // We just update them to be safe. The backend usually handles if they are identical.
                        model.updateMovieGenres(selectedMovieId, newGenreIds); //
                        anyChanges = true;
                    } else {
                        view.showWarningAlert("Warning", "A movie must have at least one genre.");
                        return;
                    }

                    // --- IMAGE UPDATE ---
                    String imagePath = view.getSelectedImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        String newPosterUrl = uploadImage(); // Helper method
                        model.updateMoviePoster(selectedMovieId, newPosterUrl); //
                        anyChanges = true;
                    }

                    // --- FINISH ---
                    if (anyChanges) {
                        view.showInfoAlert("Success", "✅ Movie updated successfully!");
                        loadAllMovies(); // Reload from DB to see changes
                        showGridViewInternal();
                    } else {
                        view.showInfoAlert("No Changes", "No fields were modified.");
                    }

                } catch (Exception e) {
                    view.showErrorAlert("Update Error", "Failed to update movie: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            private void handleDeleteMovieClick () {
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

            private void handleClearFieldsClick () {
                view.clearAllFields();

                if (view.getScheduleDatePicker() != null) {
                    view.getScheduleDatePicker().setValue(LocalDate.now());
                }

                view.showStatusMessage("Fields cleared", false);
            }

            // ===== HELPER METHODS =====

            private void populateFieldsWithMovie (Movie movie){
                view.getTitleInputEdit().setText(movie.movieTitle);
                view.getDescriptionInputEdit().setText(movie.movieDescription);
                view.getRatingComboBoxEdit().setValue(movie.movieRating);

                if (movie.moviePosterURL != null && !movie.moviePosterURL.isEmpty()) {
                    view.updateSelectedImageLabelEdit("Current: " + movie.moviePosterURL);
                }

                // Inside populateFieldsWithMovie(...)
                try {
                    Genre[] genres = model.getMovieGenres(movie.movieId);
                    if (genres != null) {
                        List<String> genreNames = Arrays.stream(genres)
                                .map(g -> g.genreName)
                                .collect(Collectors.toList());

                        // NEW: Use the method we just created
                        view.selectGenresForEdit(genreNames);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String details = String.format("Movie ID: %d | Rating: %d/10",
                        movie.movieId, movie.movieRating);
                view.updateMovieDetailsLabel(details);
            }

            private int findGenreIdByName (String genreName){
                for (Genre genre : genreDataMap.values()) {
                    if (genre.genreName.equals(genreName)) {
                        return genre.genreId;
                    }
                }
                return -1;
            }

            private boolean isValidTimeFormat (String time){
                try {
                    LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            // Helper method to get IDs from names
            private int[] getGenreIdsFromNames (List < String > names) {
                return names.stream()
                        .mapToInt(this::findGenreIdByName) // Uses your existing findGenreIdByName
                        .filter(id -> id != -1)
                        .toArray();
    }



    private void extractAndSaveImage(Movie movie) {
        if (movie.moviePosterURL == null || movie.moviePosterURL.isEmpty()) {
            return;
        }

        // Check if it contains image data
        if (!movie.moviePosterURL.contains("|||IMAGE_DATA|||")) {
            return; // Already just a path
        }

        try {
            // Split the concatenated string
            String[] parts = movie.moviePosterURL.split("\\|\\|\\|IMAGE_DATA\\|\\|\\|");
            if (parts.length != 2) {
                System.err.println("❌ Invalid image data format for movie " + movie.movieId);
                movie.moviePosterURL = ""; // Clear invalid data
                return;
            }

            String targetPath = parts[0];
            String base64Data = parts[1];

            // Decode Base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Create directory structure
            Path targetFilePath = Paths.get("src/main/resources/" + targetPath);
            Files.createDirectories(targetFilePath.getParent());

            // Save file locally
            Files.write(targetFilePath, imageBytes);

            // Update movie object to contain ONLY the path
            movie.moviePosterURL = targetPath;

            System.out.println("✅ Image extracted and saved locally: " + targetPath);

        } catch (Exception e) {
            System.err.println("❌ Error extracting image: " + e.getMessage());
            movie.moviePosterURL = ""; // Clear on error
        }
    }
}