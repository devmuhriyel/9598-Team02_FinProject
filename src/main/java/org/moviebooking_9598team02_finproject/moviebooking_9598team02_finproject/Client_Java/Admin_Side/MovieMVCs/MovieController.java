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

        loadInitialData();
        initializeButtons();
        setupLiveSearch();
        showGridViewInternal();

        System.out.println("✅ MovieController fully initialized");
    }

    private void initializeButtons() {
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
        view.showGridView();
        view.clearAllFields();
        currentlySelectedMovie = null;
        selectedMovieId = -1;
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

    private void showEditViewInternal(Movie movie) {
        currentViewState = ViewState.EDIT;
        view.showEditView();
        populateFieldsWithMovie(movie);

        // ✅ Load schedules for this movie
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
            Schedule[] schedules = model.getSchedulesByMovie(movieId);

            view.clearScheduleContainer();

            if (schedules == null || schedules.length == 0) {
                System.out.println("No schedules found for movie " + movieId);
                return;
            }

            for (Schedule schedule : schedules) {
                loadScheduleCard(schedule);
            }

            System.out.println("✅ Loaded " + schedules.length + " schedules");

        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load a single schedule card with date/time info - FIXED VERSION (Pure Java)
     */
    private void loadScheduleCard(Schedule schedule) {
        try {
            // ✅ FIX: Get MovieSchedule details from the model
            MovieSchedule movieSchedule = model.getMovieScheduleDetails(schedule.movieScheduleId);

            String screeningDate = movieSchedule.screeningDate;
            String screeningTime = movieSchedule.screeningTime;

            // ✅ FIX: Create card and store reference for lambdas
            // Using Object[] to hold mutable reference (workaround for lambda effectively final requirement)
            final Object[] cardHolder = new Object[1];

            Object card = view.createScheduleCard(
                    schedule.scheduleId,
                    schedule.movieScheduleId,
                    screeningDate,
                    screeningTime,
                    () -> handleScheduleSave(cardHolder[0]),  // Pass the card object
                    () -> handleScheduleDelete(cardHolder[0]) // Pass the card object
            );

            cardHolder[0] = card; // Store the card reference

            view.addScheduleCardToContainer(card);

        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to load schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle adding a new schedule
     */
    private void handleAddNewScheduleClick() {
        if (selectedMovieId == -1) {
            view.showWarningAlert("No Movie Selected", "Please select a movie first.");
            return;
        }

        // Get date and time from input fields
        LocalDate scheduleDate = view.getScheduleDatePicker().getValue();
        String scheduleTime = view.getScheduleTimeField().getText().trim();

        if (scheduleDate == null) {
            view.showWarningAlert("Missing Date", "Please select a screening date.");
            return;
        }

        if (scheduleTime.isEmpty()) {
            view.showWarningAlert("Missing Time", "Please enter a screening time (HH:mm).");
            return;
        }

        if (!isValidTimeFormat(scheduleTime)) {
            view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30).");
            return;
        }

        try {
            String formattedDate = scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Create movie schedule
            int movieScheduleId = model.createMovieSchedule(formattedDate, scheduleTime);

            // Assign movie to schedule
            int scheduleId = model.assignMovieToSchedule(selectedMovieId, movieScheduleId);

            view.showInfoAlert("Success!",
                    "✅ Schedule added successfully!\n" +
                            "Schedule ID: " + scheduleId + "\n" +
                            "Screening: " + formattedDate + " at " + scheduleTime);

            // Reload schedules
            loadSchedulesForMovie(selectedMovieId);

            // Clear fields
            view.getScheduleDatePicker().setValue(LocalDate.now());
            view.getScheduleTimeField().clear();

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
            // Get data from the card using View's helper method
            Map<String, Object> data = view.getScheduleCardData(scheduleCard);

            if (data == null) {
                view.showWarningAlert("Error", "No schedule data found");
                return;
            }

            int scheduleId = (int) data.get("scheduleId");
            String newDate = (String) data.get("newDate");
            String newTime = (String) data.get("newTime");

            if (!isValidTimeFormat(newTime)) {
                view.showWarningAlert("Invalid Time", "Please use HH:mm format (e.g., 14:30)");
                return;
            }

            model.updateScheduleDateTime(scheduleId, newDate, newTime);

            view.showInfoAlert("Success!", "✅ Schedule updated successfully!");

            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Not Found", "Schedule not found");
        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", e.getMessage());
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to update schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Handle schedule delete - FIXED VERSION (Pure Java)
     */
    private void handleScheduleDelete(Object scheduleCard) {
        // Get data from the card using View's helper method
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

            if (selectedMovieId != -1) {
                loadSchedulesForMovie(selectedMovieId);
            }

        } catch (ScheduleNotFoundException e) {
            view.showErrorAlert("Not Found", "Schedule not found");
        } catch (AuthenticationException e) {
            view.showErrorAlert("Authentication Error", e.getMessage());
        } catch (Exception e) {
            view.showErrorAlert("Error", "Failed to delete schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== BUTTON HANDLERS =====

    private void handleAddNewMovieClick() {
        showAddViewInternal();
    }

    private void handleMovieCardClick(Movie movie) {
        currentlySelectedMovie = movie;
        selectedMovieId = movie.movieId;
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
        LocalDate scheduleDate = view.getScheduleDatePicker().getValue();
        String scheduleTime = view.getScheduleTimeField().getText().trim();

        try {
            String posterURL = "";
            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                posterURL = uploadImage();
            }

            int[] genreIds = selectedGenreNames.stream()
                    .mapToInt(this::findGenreIdByName)
                    .filter(id -> id != -1)
                    .toArray();

            if (genreIds.length == 0) {
                view.showErrorAlert("Error", "Selected genres not found in database.");
                return;
            }

            int newMovieId = model.addMovie(title, genreIds, description, rating, posterURL);

            if (scheduleDate != null && scheduleTime != null && !scheduleTime.isEmpty()) {
                if (isValidTimeFormat(scheduleTime)) {
                    String formattedDate = scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    try {
                        int movieScheduleId = model.createMovieSchedule(formattedDate, scheduleTime);
                        int scheduleId = model.assignMovieToSchedule(newMovieId, movieScheduleId);

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

    private void handleUpdateMovieClick() {
        if (selectedMovieId == -1 || currentlySelectedMovie == null) {
            view.showWarningAlert("No Movie Selected", "Please select a movie to update.");
            return;
        }

        System.out.println("✏️ === UPDATE MOVIE PROCESS STARTED ===");
        boolean anyChanges = false;

        try {
            String newTitle = view.getTitleInputEdit().getText().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(currentlySelectedMovie.movieTitle)) {
                model.updateMovieTitle(selectedMovieId, newTitle);
                anyChanges = true;
            }

            String newDescription = view.getDescriptionInputEdit().getText().trim();
            if (!newDescription.equals(currentlySelectedMovie.movieDescription)) {
                model.updateMovieDescription(selectedMovieId, newDescription);
                anyChanges = true;
            }

            Integer newRating = view.getRatingComboBoxEdit().getValue();
            if (newRating != null && newRating != currentlySelectedMovie.movieRating) {
                model.updateMovieRating(selectedMovieId, newRating);
                anyChanges = true;
            }

            String imagePath = view.getSelectedImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                String newPosterURL = uploadImage();
                model.updateMoviePoster(selectedMovieId, newPosterURL);
                anyChanges = true;
            }

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
    }

    // ===== HELPER METHODS =====

    private void populateFieldsWithMovie(Movie movie) {
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
}