package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

public class DashboardModel extends AdminParentModel {
    private final int currentAdminId;

    public DashboardModel(int adminId, String sessionToken) {
        this.currentAdminId = adminId;
        this.adminId = adminId;
        this.sessionToken = sessionToken;
    }

    // === DASHBOARD METHODS FROM THE SERVANT===

    public String generateSalesReport(int adminId, String startDate, String endDate) throws AuthenticationException {
        return adminServices.generateSalesReport(adminId, sessionToken, startDate, endDate);
    }

    public double getMovieRevenue(int adminId, int movieId) throws AuthenticationException, MovieNotFoundException {
        return adminServices.getMovieRevenue(adminId, sessionToken, movieId);
    }

    public double getScheduleOccupancy(int adminId, int scheduleId) throws AuthenticationException, ScheduleNotFoundException {
        return adminServices.getScheduleOccupancy(adminId, sessionToken, scheduleId);
    }

    public Movie[] getPopularMovies(int adminId, int top) throws AuthenticationException {
        return adminServices.getPopularMovies(adminId, sessionToken, top);
    }

    public Movie[] getMoviesByGenre(int adminId, int genreId) throws AuthenticationException {
        return adminServices.getMoviesByGenre(adminId, sessionToken, genreId);
    }
}