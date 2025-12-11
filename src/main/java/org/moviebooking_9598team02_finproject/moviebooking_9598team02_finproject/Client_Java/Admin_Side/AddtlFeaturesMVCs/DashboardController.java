package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

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
        loadGenres();
        loadTotalRevenue();
        loadAllMovieData(null);
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Only All Movies table and genre filter are needed
        view.setGenreComboBoxAction(this::handleGenreFilterChange);
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



    private void loadAllMovieData(String genreName) {
        try {
            // Load all movies from server if not cached
            if (cachedAllMovies.length == 0) {
                cachedAllMovies = AdminParentModel.adminServices.getAllMovies(currentAdminId);
            }

            List<Movie> moviesToDisplay = Arrays.asList(cachedAllMovies);

            // Filter by selected genre
            if (genreName != null && !genreName.isEmpty() && !"All Genres".equals(genreName)) {
                int genreId = Arrays.stream(cachedGenres)
                        .filter(g -> g.genreName.equals(genreName))
                        .map(g -> g.genreId)
                        .findFirst()
                        .orElse(0);

                if (genreId > 0) {
                    moviesToDisplay = Arrays.asList(dashboardModel.getMoviesByGenre(currentAdminId, genreId));
                }
            }

            // Display only in All Movies table
            view.setAllMoviesTableData(moviesToDisplay);

        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for movie data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading movie data: " + e.getMessage());
        }
    }

    private void handleGenreFilterChange() {
        String selectedGenre = view.getSelectedGenre();
        cachedAllMovies = new Movie[0]; // Clear cache to reload
        loadAllMovieData(selectedGenre);
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
