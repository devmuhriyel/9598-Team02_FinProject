package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    private final DashboardModel dashboardModel;
    private final DashboardView view;
    private final int currentAdminId;

    private Movie[] cachedAllMovies = new Movie[0];
    private Genre[] cachedGenres = new Genre[0];

    public DashboardController(int adminId, DashboardModel dashboardModel, DashboardView view) {
        this.currentAdminId = adminId;
        this.dashboardModel = dashboardModel;
        this.view = view;
    }

    public void initializeDashboard() {
        cachedAllMovies = new Movie[0];
        loadGenres();
        loadTotalRevenue();
        loadAllMovieData(null, "");
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Event handlers for all controls
        view.setGenreComboBoxAction(this::handleGenreFilterChange);
        view.setSearchButtonAction(this::handleSearch);
        view.setClearSearchButtonAction(this::handleClearSearch);
        view.setRefreshButtonAction(this::handleRefresh);
        view.setAllMoviesSelectionListener(() -> handleMovieListings("All"));
        setupGenreColumn();
    }

    private void setupGenreColumn() {
        // Populate genre column in All Movies table
        view.allMoviesTableView.getColumns().stream()
                .filter(col -> "allColGenre".equals(col.getId()))
                .findFirst()
                .ifPresent(col -> ((TableColumn<Movie, String>) col)
                        .setCellValueFactory(cellData -> {
                            String genres = getGenreNamesForMovie(cellData.getValue().movieId);
                            return new SimpleStringProperty(genres);
                        }));
    }

    private String getGenreNamesForMovie(int movieId) {
        try {
            Genre[] genres = AdminParentModel.adminServices.getMovieGenres(currentAdminId, movieId);
            if (genres == null || genres.length == 0) return "No Genre";

            return Arrays.stream(genres)
                    .map(g -> g.genreName)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            System.err.println("Error loading genres for movie " + movieId + ": " + e.getMessage());
            return "Error";
        }
    }

    private void loadTotalRevenue() {
        try {
            double totalRevenue = dashboardModel.getMovieRevenue(currentAdminId, 0);
            view.updateSystemTotalRevenue(totalRevenue);
        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for total revenue: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading total revenue: " + e.getMessage());
        }
    }

    private void loadGenres() {
        try {
            cachedGenres = AdminParentModel.adminServices.getAllGenres(currentAdminId);
            List<String> genreNames = Arrays.stream(cachedGenres)
                    .map(g -> g.genreName)
                    .collect(Collectors.toList());
            genreNames.add(0, "All Genres"); // Add default option
            view.setGenreComboBoxOptions(genreNames);
        } catch (AuthenticationException e) {
            System.err.println("Authentication failed to load genres: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading genres: " + e.getMessage());
        }
    }



    private void loadAllMovieData(String genreName, String searchQuery) {
        try {
            // Load all movies from server if not cached
            if (cachedAllMovies.length == 0) {
                cachedAllMovies = AdminParentModel.adminServices.getAllMovies(currentAdminId);
            }

            List<Movie> moviesToDisplay = new ArrayList<>(Arrays.asList(cachedAllMovies));

            // Filter by selected genre
            if (genreName != null && !genreName.isEmpty() && !"All Genres".equals(genreName)) {
                // Find the genre ID from the cached genres
                int targetGenreId = -1;
                for (Genre g : cachedGenres) {
                    if (g.genreName.equals(genreName)) {
                        targetGenreId = g.genreId;
                        break;
                    }
                }

                // Filter movies that belong to this genre
                if (targetGenreId > 0) {
                    final int genreIdToFilter = targetGenreId;
                    List<Movie> filteredByGenre = new ArrayList<>();

                    for (Movie movie : moviesToDisplay) {
                        try {
                            Genre[] movieGenres = AdminParentModel.adminServices.getMovieGenres(currentAdminId, movie.movieId);
                            if (movieGenres != null) {
                                for (Genre g : movieGenres) {
                                    if (g.genreId == genreIdToFilter) {
                                        filteredByGenre.add(movie);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error checking genres for movie " + movie.movieId + ": " + e.getMessage());
                        }
                    }
                    moviesToDisplay = filteredByGenre;
                }
            }

            // Filter by search query
            if (searchQuery != null && !searchQuery.isEmpty()) {
                final String query = searchQuery;
                moviesToDisplay = moviesToDisplay.stream()
                        .filter(m -> m.movieTitle.toLowerCase().contains(query))
                        .collect(Collectors.toList());
            }

            // Display results in All Movies table
            view.setAllMoviesTableData(moviesToDisplay);
            view.setStatus("Showing " + moviesToDisplay.size() + " movie(s)", false);

        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for movie data: " + e.getMessage());
            view.setStatus("Authentication failed", true);
        } catch (Exception e) {
            System.err.println("Error loading movie data: " + e.getMessage());
            view.setStatus("Error loading movies: " + e.getMessage(), true);
        }
    }

    private void handleGenreFilterChange() {
        String selectedGenre = view.getSelectedGenre();
        String searchQuery = view.getSearchQuery();
        loadAllMovieData(selectedGenre, searchQuery);
    }

    private void handleSearch() {
        String selectedGenre = view.getSelectedGenre();
        String searchQuery = view.getSearchQuery();

        if (searchQuery.isEmpty()) {
            view.setStatus("Please enter a search term", true);
            return;
        }

        loadAllMovieData(selectedGenre, searchQuery);
    }

    private void handleClearSearch() {
        view.clearSearch();
        handleRefresh();
    }

    private void handleRefresh() {
        cachedAllMovies = new Movie[0]; // Clear cache to reload
        loadTotalRevenue();
        String selectedGenre = view.getSelectedGenre();
        loadAllMovieData(selectedGenre, "");
        view.setStatus("✅ Dashboard refreshed", false);
    }

    private void handleMovieListings(String category) {
        Movie selectedMovie = null;

        if ("All".equals(category)) {
            selectedMovie = view.allMoviesTableView.getSelectionModel().getSelectedItem();
        }

        if (selectedMovie != null) {
            handleMovieSelect(selectedMovie);
        }
    }

    private void handleMovieSelect(Movie movie) {
        try {
            double revenue = dashboardModel.getMovieRevenue(currentAdminId, movie.movieId);
            double avgOccupancy = 0.0;

            try {
                avgOccupancy = dashboardModel.getScheduleOccupancy(currentAdminId, 1); // Placeholder
            } catch (Exception e) {
                System.err.println("Could not get occupancy: " + e.getMessage());
            }

            view.updateMovieAnalytics(movie.movieTitle, revenue, avgOccupancy);

        } catch (Exception e) {
            System.err.println("Error loading movie analytics: " + e.getMessage());
            view.updateMovieAnalytics(movie.movieTitle, 0.0, 0.0);
        }
    }
}
