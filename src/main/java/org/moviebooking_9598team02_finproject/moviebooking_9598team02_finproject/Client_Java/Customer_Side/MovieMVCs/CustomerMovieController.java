package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerMovieController {

    private CustomerMovieView view;
    private CustomerMovieModel model;
    private MovieNavigationManager movieNavigationManager;

    private Map<Integer, Movie> movieDataMap = new HashMap<>();
    private Map<Integer, Genre> genreDataMap = new HashMap<>();
    private Movie currentlySelectedMovie = null;

    // Pagination for schedules
    private List<Schedule> currentScheduleList = new ArrayList<>();
    private static final int SCHEDULES_PER_PAGE = 6;
    private int currentSchedulePage = 1;
    private int totalSchedulePages = 1;

    // Auto-refresh timer
    private Timer autoRefreshTimer;

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
        wireSearchField();
        loadAllGenres();
        loadAllMovies();
        startAutoRefresh();
    }

    private void wireButtons() {
        view.setBackButtonAction(this::handleBackToMovieList);
        view.setPreviousSchedulePageAction(this::handlePreviousSchedulePage);
        view.setNextSchedulePageAction(this::handleNextSchedulePage);
        view.setRefreshButtonAction(this::handleRefresh);
    }

    private void wireSearchField() {
        if (view == null || view.getSearchField() == null) return;

        // Add text change listener for real-time search
        view.addSearchFieldListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });
    }

    // ===== DATA LOADING =====

    private void handleRefresh() {
        view.showStatusMessage("Refreshing content...", false);

        // 1. Clear existing data to ensure a full reload
        movieDataMap.clear();
        genreDataMap.clear();
        view.clearMovieCards(); // Clear UI immediately

        // 2. Reload data from Database
        loadAllGenres();
        loadAllMovies();
    }

    private void loadAllGenres() {
        try {
            Genre[] allGenres = model.browseGenres();
            if (allGenres == null || allGenres.length == 0) {
                showStatus("No genres available.");
                return;
            }
            genreDataMap.clear();
            for (Genre genre : allGenres) {
                genreDataMap.put(genre.genreId, genre);
            }
        } catch (Exception e) {
            showStatus("Error loading genres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadAllMovies() {
        try {
            Movie[] allMovies = model.browseMovies();
            if (allMovies == null || allMovies.length == 0) {
                showStatus("No movies found.");
                return;
            }
            movieDataMap.clear();
            // ✅ EXTRACT AND SAVE IMAGES for each movie
            for (Movie movie : allMovies) {
                extractAndSaveImage(movie);
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
        if (view == null) return;

        view.clearMovieCards();

        for (Movie movie : movies) {
            String genreNames = getGenreNamesForMovie(movie.movieId);

            // Create click action for this movie
            Runnable clickAction = () -> handleMovieCardClick(movie);

            // Ask view to create and add the card
            view.createAndAddMovieCard(
                    movie.movieId,
                    movie.movieTitle,
                    genreNames,
                    movie.movieRating,
                    movie.moviePosterURL,
                    clickAction
            );
        }
    }

    private String getGenreNamesForMovie(int movieId) {
        try {
            Genre[] genres = model.getMovieGenres(movieId);
            if (genres == null || genres.length == 0) {
                return "No genres";
            }
            return Arrays.stream(genres)
                    .map(g -> g.genreName)
                    .limit(2) // Show max 2 genres
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ===== EVENT HANDLERS =====

    private void handleSearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadAllMovies();
            return;
        }
        try {
            Movie[] searchResults = model.searchMovies(searchText.trim());
            if (searchResults == null || searchResults.length == 0) {
                showStatus("No movies found matching '" + searchText + "'");
                view.clearMovieCards();
            } else {
                // ✅ EXTRACT AND SAVE IMAGES for search results
                for (Movie movie : searchResults) {
                    extractAndSaveImage(movie);
                }

                displayMovieCards(searchResults);
                showStatus("Found " + searchResults.length + " movie(s)");
            }
        } catch (Exception e) {
            showStatus("Error searching movies: " + e.getMessage());
        }
    }

    private void handleMovieCardClick(Movie movie) {
        currentlySelectedMovie = movie;
        // Load schedules for this movie
        loadMovieSchedules(movie.movieId);
        // Show movie details view
        showMovieDetailsView(movie);
    }

    private void loadMovieSchedules(int movieId) {
        try {
            Schedule[] schedules = model.getMovieSchedules(movieId);

            currentScheduleList.clear();
            if (schedules != null && schedules.length > 0) {
                currentScheduleList.addAll(Arrays.asList(schedules));
            }

            currentSchedulePage = 1;
            updateScheduleDisplay();

        } catch (MovieNotFoundException e) {
            showStatus("Movie not found: " + e.message);
        } catch (Exception e) {
            showStatus("Error loading schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateScheduleDisplay() {
        totalSchedulePages = (int) Math.ceil((double) currentScheduleList.size() / SCHEDULES_PER_PAGE);
        if (totalSchedulePages == 0) totalSchedulePages = 1;
        if (currentSchedulePage > totalSchedulePages) currentSchedulePage = totalSchedulePages;

        view.updateSchedulePageLabel(currentSchedulePage);
        view.setSchedulePaginationButtons(currentSchedulePage > 1, currentSchedulePage < totalSchedulePages);

        int startIndex = (currentSchedulePage - 1) * SCHEDULES_PER_PAGE;
        int endIndex = Math.min(startIndex + SCHEDULES_PER_PAGE, currentScheduleList.size());
        List<Schedule> schedulesOnPage = currentScheduleList.subList(startIndex, endIndex);

        view.clearScheduleGrid();

        if (schedulesOnPage.isEmpty()) {
            showStatus("No schedules available for this movie.");
            return;
        }

        for (Schedule schedule : schedulesOnPage) {
            try {
                ScheduleDetails details = model.getScheduleDetailsView(schedule.scheduleId);
                Runnable selectAction = () -> handleScheduleSelection(schedule);

                view.createAndAddScheduleCard(
                        details.scheduleId,
                        details.screeningDate,
                        details.screeningTime,
                        details.availableSeats,
                        details.totalSeats,
                        selectAction
                );
            } catch (Exception e) {
                System.err.println("Error loading schedule " + schedule.scheduleId + ": " + e.getMessage());
            }
        }
    }

    private void handleScheduleSelection(Schedule schedule) {
        // Navigate to seat selection
        if (movieNavigationManager != null) {
            movieNavigationManager.showSeatSelection(schedule.scheduleId);
        }
    }

    private void showMovieDetailsView(Movie movie) {
        String genreNames = getGenreNamesForMovie(movie.movieId);

        view.showMovieDetailsView(
                movie.movieTitle,
                genreNames,
                movie.movieRating,
                movie.movieDescription,
                movie.moviePosterURL
        );
    }

    private void handleBackToMovieList() {
        currentlySelectedMovie = null;
        currentScheduleList.clear();
        currentSchedulePage = 1;
        view.showMovieListView();
        loadAllMovies();
    }

    private void handlePreviousSchedulePage() {
        if (currentSchedulePage > 1) {
            currentSchedulePage--;
            updateScheduleDisplay();
        }
    }

    private void handleNextSchedulePage() {
        if (currentSchedulePage < totalSchedulePages) {
            currentSchedulePage++;
            updateScheduleDisplay();
        }
    }

    // ===== HELPER METHODS =====

    private void showStatus(String message) {
        if (view != null) {
            view.showStatusMessage(message, false);
        }
    }

    // ===== GETTERS =====

    public CustomerMovieView getView() {
        return view;
    }

    public Movie getCurrentlySelectedMovie() {
        return currentlySelectedMovie;
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
                movie.moviePosterURL = ""; // Clear invalid data
                return;
            }

            // 1. Get the raw string from DB
            String rawDbPath = parts[0];

            // 2. Extract ONLY the filename (removes any accidental "C:/" or "/" from the DB string)
            String fileName = Paths.get(rawDbPath).getFileName().toString();

            // 3. Define where you want it saved relative to your project
            // We force it into "src/main/resources/images" to keep it organized
            String relativeDirectory = "src/main/resources/images";

            // 4. Construct the absolute path anchored to the Project Root
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path targetFilePath = projectRoot.resolve(relativeDirectory).resolve(fileName);

            String base64Data = parts[1];

            // Decode Base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Create directory structure if it doesn't exist
            Files.createDirectories(targetFilePath.getParent());

            // Save file locally
            Files.write(targetFilePath, imageBytes);

            // Update movie object to contain the classpath-relative path
            // This ensures the UI can load it later using getResource("/images/" + fileName)
            movie.moviePosterURL = "images/" + fileName;

        } catch (Exception e) {
            movie.moviePosterURL = ""; // Clear on error
        }
    }

    private void startAutoRefresh() {
        // Use java.util.Timer instead of JavaFX Timeline
        autoRefreshTimer = new Timer(true); // daemon thread
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Delegate to view to run on JavaFX thread
                view.runOnUIThread(() -> handleRefresh());
            }
        }, 30000, 30000); // 30 seconds delay and period
    }

    /**
     * Call this method when shutting down to clean up resources
     */
    public void shutdown() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }
}