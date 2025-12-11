package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;

public class MovieModel extends AdminParentModel {

    private final AdminService adminService;
    private int currentAdminId;

    public MovieModel(AdminService adminService, int adminId) {
        this.adminService = adminService;
        this.currentAdminId = adminId;
    }

    // === MOVIE MANAGEMENT METHODS ===

    /**
     * Add a new movie to the system
     * @return the newly created movie ID
     */
    public int addMovie(String movieTitle, int[] genreIds, String movieDescription,
                        int movieRating, String moviePosterURL)
            throws AuthenticationException, InvalidInputException {
        return adminService.addMovie(currentAdminId, movieTitle, genreIds,
                movieDescription, movieRating, moviePosterURL);
    }

    /**
     * Update movie title
     */
    public void updateMovieTitle(int movieId, String newTitle)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieTitle(currentAdminId, movieId, newTitle);
    }

    /**
     * Update movie genres (replaces all existing genres)
     */
    public void updateMovieGenres(int movieId, int[] newGenreIds)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieGenres(currentAdminId, movieId, newGenreIds);
    }

    /**
     * Add a single genre to a movie
     */
    public void addGenreToMovie(int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.addGenreToMovie(currentAdminId, movieId, genreId);
    }

    /**
     * Remove a single genre from a movie
     */
    public void removeGenreFromMovie(int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.removeGenreFromMovie(currentAdminId, movieId, genreId);
    }

    /**
     * Update movie description
     */
    public void updateMovieDescription(int movieId, String newDescription)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieDescription(currentAdminId, movieId, newDescription);
    }

    /**
     * Update movie rating
     */
    public void updateMovieRating(int movieId, int newRating)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieRating(currentAdminId, movieId, newRating);
    }

    /**
     * Update movie poster URL
     */
    public void updateMoviePoster(int movieId, String newPosterURL)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMoviePoster(currentAdminId, movieId, newPosterURL);
    }

    /**
     * Delete a movie
     */
    public void deleteMovie(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.deleteMovie(currentAdminId, movieId);
    }

    /**
     * Get all movies
     */
    public Movie[] getAllMovies() throws AuthenticationException {
        return adminService.getAllMovies(currentAdminId);
    }

    /**
     * Get genres for a specific movie
     */
    public Genre[] getMovieGenres(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        return adminService.getMovieGenres(currentAdminId, movieId);
    }

    // === SCHEDULE MANAGEMENT METHODS ===

    /**
     * Create a new movie schedule (screening time slot)
     * @param screeningDate Date in format "yyyy-MM-dd"
     * @param screeningTime Time in format "HH:mm"
     * @return the newly created movie schedule ID
     */
    public int createMovieSchedule(String screeningDate, String screeningTime)
            throws AuthenticationException, InvalidInputException {
        return adminService.createMovieSchedule(currentAdminId, screeningDate, screeningTime);
    }

    /**
     * Assign a movie to a schedule (creates Schedule entry)
     * @param movieId The movie to assign
     * @param movieScheduleId The schedule time slot
     * @return the newly created schedule ID
     */
    public int assignMovieToSchedule(int movieId, int movieScheduleId)
            throws AuthenticationException, MovieNotFoundException, ScheduleNotFoundException {
        return adminService.assignMovieToSchedule(currentAdminId, movieId, movieScheduleId);
    }

    /**
     * Get all schedules for a specific movie
     */
    public Schedule[] getSchedulesByMovie(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        return adminService.getSchedulesByMovie(currentAdminId, movieId);
    }

    // === GENRE MANAGEMENT METHODS ===

    /**
     * Get all genres
     */
    public Genre[] getAllGenres() throws AuthenticationException {
        return adminService.getAllGenres(currentAdminId);
    }

    /**
     * Get a specific genre by ID
     */
    public Genre getGenreById(int genreId) throws AuthenticationException {
        return adminService.getGenreById(currentAdminId, genreId);
    }

    // === ADMIN ID SETTER ===

    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }
}