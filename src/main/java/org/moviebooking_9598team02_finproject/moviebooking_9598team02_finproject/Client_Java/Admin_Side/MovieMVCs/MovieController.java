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

        // ✅ Ensure image folder exists on startup
        ensureImageFolderExists();

        loadInitialData();
        initializeButtons();
        setupLiveSearch();
        showGridViewInternal();

        // Timer
        view.startAutoRefresh(() -> {
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

        view.getBtnAddMovie().setOnAction(e -> handleAddMovieClick());
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
        currentViewState = ViewState.GRID;
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
        if (movie == null) {
            System.err.println("❌ showEditViewInternal called with NULL movie");
            return;
        }

        System.out.println("✏️ === SHOWING EDIT VIEW FOR MOVIE ===");
        System.out.println("Movie ID: " + movie.movieId);
        System.out.println("Movie Title: " + movie.movieTitle);

        currentViewState = ViewState.EDIT;
        loadAllGenres();
        selectedMovieId = movie.movieId;
        currentlySelectedMovie = movie;

        populateFieldsWithMovie(movie);

        // ✅ Show view first
        view.showEditView();

        // ✅ CRITICAL: Ensure schedule components are ready AFTER view is shown
        view.ensureScheduleComponentsReadyEdit();  // ✅ Use Edit version

        // ✅ Force schedule container to be visible
        view.ensureScheduleContainerVisible();

        // ✅ Load schedules
        System.out.println("📊 Loading schedules for movie " + movie.movieId + "...");
        loadSchedulesForMovie(movie.movieId);

        System.out.println("✅ === EDIT VIEW INITIALIZATION COMPLETE ===");
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

            // ✅ ADD DEBUGGING HERE
            System.out.println("📸 === MOVIE POSTER DEBUG ===");
            for (Movie m : allMovies) {
                System.out.println("Movie ID: " + m.movieId);
                System.out.println("  Title: " + m.movieTitle);
                System.out.println("  Poster URL: " + m.moviePosterURL);

                // Check if file exists
                if (m.moviePosterURL != null && !m.moviePosterURL.isEmpty()) {
                    Path imagePath = Paths.get("src/main/resources/" + m.moviePosterURL);
                    boolean exists = Files.exists(imagePath);
                    System.out.println("  File exists? " + exists);
                    if (exists) {
                        try {
                            long fileSize = Files.size(imagePath);
                            System.out.println("  File size: " + fileSize + " bytes");
                        } catch (IOException e) {
                            System.out.println("  Error reading file size");
                        }
                    }
                } else {
                    System.out.println("  ⚠️ No poster URL in database");
                }
                System.out.println("---");
            }
            System.out.println("=========================");

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
                System.out.println("📅 Loading schedule " + (i + 1) + ": ID=" + schedule.scheduleId);
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
     * Handle adding a new schedule (inside the Edit Movie screen) - IMPROVED VALIDATION
     */
    private void handleAddNewScheduleClick() {
        try {
            System.out.println("🎬 === ADD SCHEDULE PROCESS STARTED ===");

            // ✅ DIAGNOSTIC: Check UI state before proceeding
            System.out.println("🔍 Pre-flight check:");
            System.out.println("   → Movie ID: " + selectedMovieId);
            System.out.println("   → Movie Title: " + (currentlySelectedMovie != null ?
                    currentlySelectedMovie.movieTitle : "NULL"));

            // ✅ Force component verification
            view.ensureScheduleComponentsReady();

            // 1. Validate Movie Selection
            if (selectedMovieId == -1) {
                System.err.println("❌ No movie selected");
                view.showWarningAlert("Error", "No movie selected. Please select a movie first.");
                return;
            }

            // 2. Get Date and Time through VIEW methods (EDIT versions)
            String date = view.getScheduleDateValueEdit();
            String time = view.getScheduleTimeValueEdit();

            System.out.println("📅 Date value: " + date);
            System.out.println("🕐 Time value: " + time);

            // 3. Validate Date
            if (date == null || date.isEmpty()) {
                System.err.println("❌ Date is NULL or empty");
                view.showWarningAlert("Validation Error",
                        "Please select a screening date.\n\n" +
                                "Tip: Click on the date picker calendar icon to choose a date.");

                view.setScheduleDateToToday();
                return;
            }

            // 4. ✅ NEW: Validate that date is not in the past
            if (isDateInPast(date)) {
                System.err.println("❌ Cannot schedule for past date: " + date);
                view.showWarningAlert("Invalid Date",
                        "⚠️ Cannot schedule a movie for a past date.\n\n" +
                                "Please select today's date or a future date.\n\n" +
                                "Selected date: " + date + "\n" +
                                "Today's date: " + LocalDate.now());

                // Reset to today's date
                view.setScheduleDateToToday();
                return;
            }

            // 5. Validate Time
            if (time == null || time.isEmpty()) {
                System.err.println("❌ Time is NULL or empty");
                view.showWarningAlert("Validation Error",
                        "Please select a screening time.\n\n" +
                                "Tip: Click on the time dropdown to choose a time slot.\n\n" +
                                "If the dropdown is empty, please report this as a bug.");
                return;
            }

            // 6. ✅ NEW: Additional validation - if date is today, check if time is not in the past
            if (date.equals(LocalDate.now().toString())) {
                LocalTime currentTime = LocalTime.now();
                LocalTime selectedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));

                if (selectedTime.isBefore(currentTime)) {
                    System.err.println("❌ Cannot schedule for past time today: " + time);
                    view.showWarningAlert("Invalid Time",
                            "⚠️ Cannot schedule a movie for a time that has already passed today.\n\n" +
                                    "Current time: " + currentTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
                                    "Selected time: " + time + "\n\n" +
                                    "Please select a future time slot.");
                    return;
                }
            }

            // 7. Validate time format
            if (!isValidTimeFormat(time)) {
                view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30)");
                return;
            }

            System.out.println("✅ All validations passed. Creating schedule...");

            // 8. Create movie schedule (time slot)
            int movieScheduleId = model.createMovieSchedule(date, time);
            System.out.println("✅ Created movieScheduleId: " + movieScheduleId);

            // 9. Assign movie to schedule
            int scheduleId = model.assignMovieToSchedule(selectedMovieId, movieScheduleId);
            System.out.println("✅ Assigned to scheduleId: " + scheduleId);

            // 10. Show success message
            view.showInfoAlert("Success!",
                    "✅ Schedule created successfully!\n\n" +
                            "Date: " + date + "\n" +
                            "Time: " + time + "\n" +
                            "Schedule ID: " + scheduleId);

            // 11. Refresh schedules display
            loadSchedulesForMovie(selectedMovieId);

            // 12. Reset fields for next entry
            view.resetScheduleFields();

            System.out.println("✅ === ADD SCHEDULE PROCESS COMPLETED ===");

        } catch (InvalidInputException e) {
            System.err.println("❌ Validation Error: " + e.message);
            view.showWarningAlert("Validation Error", e.message);
        } catch (AuthenticationException e) {
            System.err.println("❌ Authentication Error: " + e.message);
            view.showErrorAlert("Authentication Error",
                    "Session may have expired. Please try logging in again.\n\n" + e.message);
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            view.showErrorAlert("Unexpected Error",
                    "An unexpected error occurred:\n\n" + e.getMessage() +
                            "\n\nPlease check the console for details.");
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
     * Handle schedule delete - IMPROVED with better error handling
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
                "⚠️ WARNING: This action cannot be undone.\n\n" +
                        "• All seat assignments for this schedule will be removed\n" +
                        "• Any PENDING bookings will be cancelled\n" +
                        "• CONFIRMED bookings will prevent deletion\n\n" +
                        "Make sure there are no confirmed bookings before deleting."
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
            view.showErrorAlert("Not Found", "Schedule not found. It may have already been deleted.");

            // Refresh to show current state
            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

        } catch (AuthenticationException e) {
            // This exception is also used for business logic errors
            String errorMsg = e.message;

            if (errorMsg.contains("existing bookings")) {
                view.showErrorAlert("Cannot Delete",
                        "❌ This schedule cannot be deleted because it has confirmed bookings.\n\n" +
                                "Please cancel all bookings for this schedule first.");
            } else if (errorMsg.contains("session expired")) {
                view.showErrorAlert("Session Expired",
                        "Your session has expired. Please log in again.");
            } else {
                view.showErrorAlert("Error", "Failed to delete schedule: " + errorMsg);
            }

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
            // ... all your existing code for gathering inputs and validation ...

            String title = view.getTitleInput().getText().trim();
            String description = view.getDescriptionInput().getText().trim();
            Integer rating = view.getRatingComboBox().getValue();
            List<String> selectedGenres = view.getSelectedGenresAdd();
            int[] genreIds = getGenreIdsFromNames(selectedGenres);

            String posterUrl = "";
            String imagePath = view.getSelectedImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                Path sourceImagePath = Paths.get(imagePath);
                byte[] imageBytes = Files.readAllBytes(sourceImagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String filename = System.currentTimeMillis() + "_" + sourceImagePath.getFileName().toString();

                String targetPath = IMAGE_DIRECTORY + filename;

                // Save to client's local resources first
                saveImageToClientResources(imageBytes, targetPath);

                posterUrl = targetPath + "|||IMAGE_DATA|||" + base64Image;
            }

            LocalDate scheduleDate = view.getScheduleDatePicker().getValue();
            String scheduleTime = view.getScheduleTimeComboBox().getValue();

            // Validations...
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

            // Create movie
            System.out.println("📤 Creating movie...");
            int newMovieId = model.addMovie(title, genreIds, description, rating, posterUrl);
            System.out.println("✅ Movie created with ID: " + newMovieId);

            // Add schedule if provided
            if (scheduleDate != null && scheduleTime != null && !scheduleTime.isEmpty()) {
                try {
                    System.out.println("📤 Creating schedule for new movie...");
                    String formattedDate = scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    int movieScheduleId = model.createMovieSchedule(formattedDate, scheduleTime);
                    System.out.println("✅ Created movieScheduleId: " + movieScheduleId);

                    int scheduleId = model.assignMovieToSchedule(newMovieId, movieScheduleId);
                    System.out.println("✅ Assigned to scheduleId: " + scheduleId);

                } catch (Exception scheduleError) {
                    System.err.println("⚠️ Failed to create schedule: " + scheduleError.getMessage());
                    view.showWarningAlert("Partial Success",
                            "Movie created successfully, but schedule creation failed: " + scheduleError.getMessage());
                }
            } else {
                System.out.println("ℹ️ No schedule provided - skipping schedule creation");
            }

            // Success
            view.showInfoAlert("Success", "✅ Movie added successfully! ID: " + newMovieId);

            // ✅ CHANGED: Use reloadMoviesWithImages instead of loadAllMovies
            reloadMoviesWithImages(); // Clear cache and reload
            showGridViewInternal();

        } catch (Exception e) {
            System.err.println("❌ Error adding movie: " + e.getMessage());
            view.showErrorAlert("Error", "Failed to add movie: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleUpdateMovieClick() {
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
                model.updateMovieTitle(selectedMovieId, newTitle);
                anyChanges = true;
            }

            // --- DESCRIPTION UPDATE ---
            String newDesc = view.getDescriptionInputEdit().getText().trim();
            if (!newDesc.equals(currentlySelectedMovie.movieDescription)) {
                model.updateMovieDescription(selectedMovieId, newDesc);
                anyChanges = true;
            }

            // --- RATING UPDATE ---
            Integer newRating = view.getRatingComboBoxEdit().getValue();
            if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                model.updateMovieRating(selectedMovieId, newRating);
                anyChanges = true;
            }

            // --- GENRE UPDATE ---
            List<String> newSelectedGenres = view.getSelectedGenresEdit();
            int[] newGenreIds = getGenreIdsFromNames(newSelectedGenres);

            if (newGenreIds.length > 0) {
                model.updateMovieGenres(selectedMovieId, newGenreIds);
                anyChanges = true;
            } else {
                view.showWarningAlert("Warning", "A movie must have at least one genre.");
                return;
            }

            // --- IMAGE UPDATE ---
            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Path sourceImagePath = Paths.get(imagePath);
                    byte[] imageBytes = Files.readAllBytes(sourceImagePath);
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String filename = System.currentTimeMillis() + "_" + sourceImagePath.getFileName().toString();

                    String targetPath = IMAGE_DIRECTORY + filename;

                    // ✅ Save to client's local resources first
                    saveImageToClientResources(imageBytes, targetPath);

                    // ✅ Prepare data for server (with embedded image)
                    String newPosterUrl = targetPath + "|||IMAGE_DATA|||" + base64Image;

                    System.out.println("📤 Sending updated image to server...");

                    // Send to server
                    model.updateMoviePoster(selectedMovieId, newPosterUrl);
                    anyChanges = true;

                    System.out.println("✅ Image update sent to server successfully");

                } catch (IOException e) {
                    view.showErrorAlert("Image Error", "Failed to process image: " + e.getMessage());
                    e.printStackTrace();
                    return; // Don't continue if image failed
                }
            }

            // --- FINISH ---
            if (anyChanges) {
                view.showInfoAlert("Success", "✅ Movie updated successfully!");

                // ✅ CHANGED: Use reloadMoviesWithImages instead of loadAllMovies
                reloadMoviesWithImages(); // Clear cache and reload
                showGridViewInternal();
            } else {
                view.showInfoAlert("No Changes", "No fields were modified.");
            }

        } catch (Exception e) {
            view.showErrorAlert("Update Error", "Failed to update movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle movie deletion - IMPROVED with better error handling
     */
    private void handleDeleteMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showWarningAlert("No Movie Selected", "Please select a movie to delete.");
            return;
        }

        boolean confirmed = view.showConfirmationDialog(
                "Delete Movie",
                "⚠️ Are you sure you want to delete '" + currentlySelectedMovie.movieTitle + "'?",
                "WARNING: This is a permanent action!\n\n" +
                        "This will delete:\n" +
                        "• The movie and all its information\n" +
                        "• All schedules for this movie\n" +
                        "• All seat assignments for these schedules\n" +
                        "• All PENDING bookings\n\n" +
                        "❌ CONFIRMED bookings will PREVENT deletion.\n" +
                        "You must cancel confirmed bookings first.\n\n" +
                        "Are you absolutely sure?"
        );

        if (!confirmed) {
            return;
        }

        try {
            model.deleteMovie(selectedMovieId);
            view.showInfoAlert("Success!",
                    "✅ Movie '" + currentlySelectedMovie.movieTitle + "' deleted successfully!\n\n" +
                            "All related schedules and data have been removed.");

            loadAllMovies();
            showGridViewInternal();

        } catch (MovieNotFoundException e) {
            view.showErrorAlert("Movie Not Found",
                    "The movie could not be found. It may have already been deleted.");

            loadAllMovies();
            showGridViewInternal();

        } catch (AuthenticationException e) {
            String errorMsg = e.message;

            if (errorMsg.contains("confirmed bookings")) {
                view.showErrorAlert("Cannot Delete Movie",
                        "❌ This movie cannot be deleted because it has confirmed bookings.\n\n" +
                                "Steps to delete this movie:\n" +
                                "1. Go to Booking Management\n" +
                                "2. Find and cancel all confirmed bookings for this movie\n" +
                                "3. Return here and try deleting again\n\n" +
                                "Note: Pending bookings will be automatically cancelled.");
            } else if (errorMsg.contains("session expired")) {
                view.showErrorAlert("Session Expired",
                        "Your session has expired. Please log in again.");
            } else {
                view.showErrorAlert("Authentication Error",
                        "Failed to delete movie: " + errorMsg);
            }

        } catch (Exception e) {
            view.showErrorAlert("Delete Error",
                    "An unexpected error occurred while deleting the movie:\n\n" +
                            e.getMessage() + "\n\n" +
                            "Please check the logs for more details.");
            e.printStackTrace();
        }
    }

    private void handleClearFieldsClick() {
        view.clearAllFields();

        if (view.getScheduleDatePicker() != null) {
            view.getScheduleDatePicker().setValue(LocalDate.now());
        }

        view.showStatusMessage("Fields cleared", false);
    }

    // ===== HELPER METHODS =====
    private boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        // Simple regex check for YYYY-MM-DD format
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean isDateInPast(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate today = LocalDate.now();
            return date.isBefore(today);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return false;
        }
    }

    private void populateFieldsWithMovie(Movie movie) {
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

    private int findGenreIdByName(String genreName) {
        for (Genre genre : genreDataMap.values()) {
            if (genre.genreName.equals(genreName)) {
                return genre.genreId;
            }
        }
        return -1;
    }

    private boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // Helper method to get IDs from names
    private int[] getGenreIdsFromNames(List<String> names) {
        return names.stream()
                .mapToInt(this::findGenreIdByName) // Uses your existing findGenreIdByName
                .filter(id -> id != -1)
                .toArray();
    }

    private boolean ensureImageFolderExists() {
        try {
            Path imageFolder = Paths.get("src/main/resources/" + IMAGE_DIRECTORY);

            if (!Files.exists(imageFolder)) {
                Files.createDirectories(imageFolder);
                System.out.println("✅ Created image folder: " + imageFolder.toAbsolutePath());
            } else {
                System.out.println("✅ Image folder exists: " + imageFolder.toAbsolutePath());
            }

            return true;

        } catch (IOException e) {
            System.err.println("❌ Failed to create image folder: " + e.getMessage());
            return false;
        }
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

    // ===== IMAGE METHODS =====
    private void saveImageToClientResources(byte[] imageBytes, String targetPath) {
        try {
            // Build the full path to resources folder
            Path resourcesPath = Paths.get("src/main/resources/" + targetPath);

            // Create parent directories if they don't exist
            Files.createDirectories(resourcesPath.getParent());

            // ✅ CORRECT: Write the image file (NO StandardCopyOption here!)
            Files.write(resourcesPath, imageBytes);

            System.out.println("✅ Image saved to client resources: " + resourcesPath.toAbsolutePath());
            System.out.println("   📊 Size: " + imageBytes.length + " bytes");

        } catch (IOException e) {
            System.err.println("⚠️ WARNING: Could not save image to client resources");
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            // Show warning to user but don't fail the whole operation
            view.showWarningAlert("Image Save Warning",
                    "Movie will be created, but image could not be saved to local folder.\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void clearImageCache() {
        System.out.println("🗑️ Clearing image cache...");

        // Clear the grid completely
        view.clearMovieGrid();

        // Force garbage collection (optional but helps)
        System.gc();

        System.out.println("✅ Image cache cleared");
    }

    private void reloadMoviesWithImages() {
        System.out.println("🔄 Reloading movies with fresh images...");

        // Clear cache first
        clearImageCache();

        // ✅ INCREASED DELAY to ensure file system has written the file
        try {
            Thread.sleep(300); // 300ms delay (increased from 100ms)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Reload from database
        loadAllMovies();

        System.out.println("✅ Movies reloaded");
    }
}