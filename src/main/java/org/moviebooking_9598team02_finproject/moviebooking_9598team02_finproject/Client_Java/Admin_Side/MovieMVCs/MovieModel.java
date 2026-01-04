package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class MovieModel extends AdminParentModel {

    private final AdminService adminService;

    public MovieModel(AdminService adminService, int adminId) {
        this.adminService = adminService;

    }

    // === MOVIE MANAGEMENT METHODS ===

    /**
     * Add a new movie to the system
     * @return the newly created movie ID
     */
    public int addMovie(String movieTitle, int[] genreIds, String movieDescription, int movieRating, String moviePosterURL) throws AuthenticationException, InvalidInputException {
        return adminService.addMovie(adminId, sessionToken, movieTitle, genreIds, movieDescription, movieRating, moviePosterURL);
    }

    /**
     * Update movie title
     */
    public void updateMovieTitle(int movieId, String newTitle)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieTitle(adminId, sessionToken, movieId, newTitle);
    }

    /**
     * Update movie genres (replaces all existing genres)
     */
    public void updateMovieGenres(int movieId, int[] newGenreIds)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieGenres(adminId, sessionToken, movieId, newGenreIds);
    }

    /**
     * Add a single genre to a movie
     */
    public void addGenreToMovie(int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.addGenreToMovie(adminId, sessionToken, movieId, genreId);
    }

    /**
     * Remove a single genre from a movie
     */
    public void removeGenreFromMovie(int movieId, int genreId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.removeGenreFromMovie(adminId, sessionToken, movieId, genreId);
    }

    /**
     * Update movie description
     */
    public void updateMovieDescription(int movieId, String newDescription)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieDescription(adminId, sessionToken, movieId, newDescription);
    }

    /**
     * Update movie rating
     */
    public void updateMovieRating(int movieId, int newRating)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMovieRating(adminId, sessionToken, movieId, newRating);
    }

    /**
     * Update movie poster URL
     */
    public void updateMoviePoster(int movieId, String newPosterURL)
            throws AuthenticationException, MovieNotFoundException {
        adminService.updateMoviePoster(adminId, sessionToken, movieId, newPosterURL);
    }

    /**
     * Delete a movie
     */
    public void deleteMovie(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        adminService.deleteMovie(adminId, sessionToken, movieId);
    }

    /**
     * Get all movies
     */
    public Movie[] getAllMovies() throws AuthenticationException {
        return adminService.getAllMovies(adminId, sessionToken);
    }

    /**
     * Get genres for a specific movie
     */
    public Genre[] getMovieGenres(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        return adminService.getMovieGenres(adminId, sessionToken, movieId);
    }

    // === SCHEDULE MANAGEMENT METHODS ===

    public int createMovieSchedule(String date, String time) throws Exception {
        // Calls the server to create a time slot
        return adminService.createMovieSchedule(adminId, sessionToken, date, time);
    }

    public int assignMovieToSchedule(int movieId, int movieScheduleId) throws Exception {
        // Links the movie to the time slot
        return adminService.assignMovieToSchedule(adminId, sessionToken, movieId, movieScheduleId);
    }

    /**
     * Get all schedules for a specific movie
     */
    public Schedule[] getSchedulesByMovie(int movieId)
            throws AuthenticationException, MovieNotFoundException {
        return adminService.getSchedulesByMovie(adminId, sessionToken, movieId);
    }

    // In MovieModel.java

    public MovieSchedule getMovieScheduleDetails(int movieScheduleId)
            throws AuthenticationException, ScheduleNotFoundException {
        return commonServices.getMovieScheduleById(movieScheduleId);

    }

    /**
     * Update schedule date and time
     */
    public void updateScheduleDateTime(int scheduleId, String newDate, String newTime)
            throws AuthenticationException, ScheduleNotFoundException {
        adminService.updateScheduleDateTime(adminId, sessionToken, scheduleId, newDate, newTime);
    }

    /**
     * Delete a schedule
     */
    public void deleteSchedule(int scheduleId)
            throws AuthenticationException, ScheduleNotFoundException {
        adminService.deleteSchedule(adminId, sessionToken, scheduleId);
    }

    /**
     * Get all schedules
     */
    public Schedule[] getAllSchedules() throws AuthenticationException {
        return adminService.getAllSchedules(adminId, sessionToken);
    }

    // === GENRE MANAGEMENT METHODS ===

    /**
     * Get all genres
     */
    public Genre[] getAllGenres() throws AuthenticationException {
        return adminService.getAllGenres(adminId, sessionToken);
    }

    /**
     * Get a specific genre by ID
     */
    public Genre getGenreById(int genreId) throws AuthenticationException {
        return adminService.getGenreById(adminId, sessionToken, genreId);
    }

    // === ADMIN ID SETTER ===

}