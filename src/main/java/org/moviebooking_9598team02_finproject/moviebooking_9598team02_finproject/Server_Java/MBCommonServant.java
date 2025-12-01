package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.CommonServicePOA;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.MovieNotFoundException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.MovieSchedule;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Schedule;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.ScheduleNotFoundException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Seat;

public class MBCommonServant extends CommonServicePOA{
    @Override
    public Movie[] getAllMovies() {
        return new Movie[0];
    }

    @Override
    public Movie getMovieById(int movieId) throws MovieNotFoundException {
        return null;
    }

    @Override
    public Movie[] filterMoviesByScheduleDate(int dayNumber, int monthNumber, int yearNumber) {
        return new Movie[0];
    }

    @Override
    public Movie[] filterMoviesByGenres(String[] genres) {
        return new Movie[0];
    }

    @Override
    public Movie[] filterMoviesByRating(int minRating) {
        return new Movie[0];
    }

    @Override
    public Movie[] searchMoviesByTitle(String searchString) {
        return new Movie[0];
    }

    @Override
    public MovieSchedule[] getAllMovieSchedules() {
        return new MovieSchedule[0];
    }

    @Override
    public Schedule[] getSchedulesByMovieId(int movieId) throws MovieNotFoundException {
        return new Schedule[0];
    }

    @Override
    public Schedule[] getSchedulesByDate(int dayNumber, int monthNumber, int yearNumber) {
        return new Schedule[0];
    }

    @Override
    public MovieSchedule getMovieScheduleById(int scheduleId) throws ScheduleNotFoundException {
        return null;
    }

    @Override
    public Seat[] getSeatsByScheduleId(int scheduleId) throws ScheduleNotFoundException {
        return new Seat[0];
    }

    @Override
    public Seat[] getAvailableSeatsByScheduleId(int scheduleId) throws ScheduleNotFoundException {
        return new Seat[0];
    }

    @Override
    public boolean areSeatsAvailable(int[] seatIds) {
        return false;
    }
}
