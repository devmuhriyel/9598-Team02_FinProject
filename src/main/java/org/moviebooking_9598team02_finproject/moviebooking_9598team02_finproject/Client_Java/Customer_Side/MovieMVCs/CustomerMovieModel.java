package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingClient.ClientService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.CustomerParentModel;

public class CustomerMovieModel extends CustomerParentModel {

    private ClientService clientService;
    private int currentCustomerId;

    public CustomerMovieModel(int customerId) {
        this.currentCustomerId = customerId;
        this.clientService = clientServices;
    }

    // ===== MOVIE BROWSING METHODS =====

    /**
     * Get all available movies
     */
    public Movie[] browseMovies() {
        return clientService.browseMovies();
    }

    /**
     * Search movies by title
     */
    public Movie[] searchMovies(String searchString) {
        return clientService.searchMovies(searchString);
    }

    /**
     * Get movie details by ID
     */
    public Movie getMovieDetails(int movieId) throws MovieNotFoundException {
        return clientService.getMovieDetails(movieId);
    }

    /**
     * Filter movies by genre IDs
     */
    public Movie[] filterByGenreIds(int[] genreIds) {
        return clientService.filterByGenreIds(genreIds);
    }

    /**
     * Filter movies by genre names
     */
    public Movie[] filterByGenreNames(String[] genreNames) {
        return clientService.filterByGenreNames(genreNames);
    }

    /**
     * Filter movies by minimum rating
     */
    public Movie[] filterByRating(int minRating) {
        return clientService.filterByRating(minRating);
    }

    /**
     * Get movies showing on specific date
     */
    public Movie[] getMoviesByDate(int dayNumber, int monthNumber, int yearNumber) {
        return clientService.getMoviesByDate(dayNumber, monthNumber, yearNumber);
    }

    // ===== GENRE BROWSING METHODS =====

    /**
     * Get all available genres
     */
    public Genre[] browseGenres() {
        return clientService.browseGenres();
    }

    /**
     * Get a specific genre by ID
     */
    public Genre getGenreDetails(int genreId) {
        return clientService.getGenreDetails(genreId);
    }

    /**
     * Get genres for a specific movie
     */
    public Genre[] getMovieGenres(int movieId) throws MovieNotFoundException {
        return clientService.getMovieGenres(movieId);
    }

    // ===== SCHEDULE METHODS =====

    /**
     * Get all available schedules for a movie
     */
    public Schedule[] getMovieSchedules(int movieId) throws MovieNotFoundException {
        return clientService.getMovieSchedules(movieId);
    }

    /**
     * Get schedule with movie info and availability
     */
    public ScheduleDetails getScheduleDetailsView(int scheduleId) throws ScheduleNotFoundException {
        return clientService.getScheduleDetailsView(scheduleId);
    }

    /**
     * Get all schedules with details
     */
    public ScheduleDetails[] getAllSchedulesWithDetails() {
        return clientService.getAllSchedulesWithDetails();
    }

    // ===== CUSTOMER ID SETTER =====

    public void setCurrentCustomerId(int customerId) {
        this.currentCustomerId = customerId;
    }

    public int getCurrentCustomerId() {
        return currentCustomerId;
    }
}