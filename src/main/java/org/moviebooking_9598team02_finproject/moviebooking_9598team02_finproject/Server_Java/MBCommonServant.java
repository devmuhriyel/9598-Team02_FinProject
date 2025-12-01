package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.omg.CORBA.ORB;

import java.sql.Connection;

public class MBCommonServant extends CommonServicePOA{

    // ===== DATABASE VARIABLES =====
    private Connection conn = null;

    //
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

    @Override
    public ScheduleListSeat[] getScheduleSeats(int scheduleId) throws ScheduleNotFoundException {
        return new ScheduleListSeat[0];
    }

    @Override
    public ScheduleListSeat getScheduleSeatById(int scheduleListSeatId) {
        return null;
    }

    @Override
    public boolean isSeatAvailableForSchedule(int scheduleId, int seatId) {
        return false;
    }
}
