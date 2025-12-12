package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MBCommonServant extends CommonServicePOA {

    // ===== SEAT METHODS (CRITICAL FOR SEAT SELECTION) =====

    @Override
    public ScheduleListSeat[] getScheduleSeats(int scheduleId) throws ScheduleNotFoundException {
        List<ScheduleListSeat> seatList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            // Check if schedule exists
            String checkQuery = "SELECT COUNT(*) FROM schedules WHERE schedule_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, scheduleId);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) == 0) {
                throw new ScheduleNotFoundException("Schedule not found", scheduleId);
            }

            // Get all seats for this schedule (including booked ones)
            String query = "SELECT * FROM schedule_list_seats " +
                    "WHERE schedule_id = ? " +
                    "ORDER BY seat_id";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ScheduleListSeat seat = new ScheduleListSeat();
                seat.scheduleListSeatId = rs.getInt("schedule_list_seat_id");
                seat.scheduleId = rs.getInt("schedule_id");
                seat.seatId = rs.getInt("seat_id");
                seat.status = rs.getString("status");

                // Handle NULL booking_id
                int bookingId = rs.getInt("booking_id");
                seat.bookingId = rs.wasNull() ? 0 : bookingId;

                seat.createdAt = rs.getString("created_at");
                seat.updatedAt = rs.getString("updated_at");
                seatList.add(seat);
            }

            System.out.println("✅ Retrieved " + seatList.size() + " seats for schedule " + scheduleId);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching schedule seats", scheduleId);
        }
        return seatList.toArray(new ScheduleListSeat[0]);
    }

    @Override
    public Seat[] getSeatsByScheduleId(int scheduleId) throws ScheduleNotFoundException {
        List<Seat> seatList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT s.* FROM seats s " +
                    "INNER JOIN schedule_list_seats sls ON s.seat_id = sls.seat_id " +
                    "WHERE sls.schedule_id = ? " +
                    "ORDER BY s.seat_row, s.seat_number";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat();
                seat.seatId = rs.getInt("seat_id");
                seat.seatRow = rs.getString("seat_row");
                seat.seatNumber = rs.getInt("seat_number");
                seat.seatType = rs.getString("seat_type");
                seat.seatPrice = rs.getDouble("seat_price");
                seat.createdAt = rs.getString("created_at");
                seat.updatedAt = rs.getString("updated_at");
                seatList.add(seat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching seats", scheduleId);
        }
        return seatList.toArray(new Seat[0]);
    }

    @Override
    public SeatAvailability[] getAvailableSeatsByScheduleId(int scheduleId) throws ScheduleNotFoundException {
        List<SeatAvailability> availList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_seat_availability " +
                    "WHERE schedule_id = ? " +
                    "ORDER BY seat_row, seat_number";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                SeatAvailability avail = new SeatAvailability();
                avail.scheduleListSeatId = rs.getInt("schedule_list_seat_id");
                avail.scheduleId = rs.getInt("schedule_id");
                avail.seatId = rs.getInt("seat_id");
                avail.seatRow = rs.getString("seat_row");
                avail.seatNumber = rs.getInt("seat_number");
                avail.seatType = rs.getString("seat_type");
                avail.seatPrice = rs.getDouble("seat_price");
                avail.status = rs.getString("status");
                avail.movieTitle = rs.getString("movie_title");
                avail.screeningDate = rs.getString("screening_date");
                avail.screeningTime = rs.getString("screening_time");
                availList.add(avail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching seat availability", scheduleId);
        }
        return availList.toArray(new SeatAvailability[0]);
    }

    @Override
    public boolean areSeatsAvailable(int[] seatIds) {
        if (seatIds == null || seatIds.length == 0) {
            return false;
        }

        try (Connection connection = DatabaseAccess.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT COUNT(*) FROM schedule_list_seats WHERE status = 'available' AND schedule_list_seat_id IN ("
            );

            for (int i = 0; i < seatIds.length; i++) {
                query.append("?");
                if (i < seatIds.length - 1) query.append(",");
            }
            query.append(")");

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < seatIds.length; i++) {
                statement.setInt(i + 1, seatIds[i]);
            }

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int availableCount = rs.getInt(1);
                return availableCount == seatIds.length;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ScheduleListSeat getScheduleSeatById(int scheduleListSeatId) {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM schedule_list_seats WHERE schedule_list_seat_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleListSeatId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                ScheduleListSeat seat = new ScheduleListSeat();
                seat.scheduleListSeatId = rs.getInt("schedule_list_seat_id");
                seat.scheduleId = rs.getInt("schedule_id");
                seat.seatId = rs.getInt("seat_id");
                seat.status = rs.getString("status");

                int bookingId = rs.getInt("booking_id");
                seat.bookingId = rs.wasNull() ? 0 : bookingId;

                seat.createdAt = rs.getString("created_at");
                seat.updatedAt = rs.getString("updated_at");
                return seat;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isSeatAvailableForSchedule(int scheduleId, int seatId) {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT status FROM schedule_list_seats " +
                    "WHERE schedule_id = ? AND seat_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            statement.setInt(2, seatId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return "available".equals(rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== MOVIE METHODS =====

    @Override
    public Movie[] getAllMovies() {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies ORDER BY movie_title";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie getMovieById(int movieId) throws MovieNotFoundException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                return movie;
            } else {
                throw new MovieNotFoundException("Movie not found", movieId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching movie", movieId);
        }
    }

    @Override
    public Movie[] filterMoviesByScheduleDate(int dayNumber, int monthNumber, int yearNumber) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT DISTINCT m.* FROM movies m " +
                    "INNER JOIN schedules s ON m.movie_id = s.movie_id " +
                    "INNER JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                    "WHERE DAY(ms.screening_date) = ? " +
                    "AND MONTH(ms.screening_date) = ? " +
                    "AND YEAR(ms.screening_date) = ? " +
                    "ORDER BY m.movie_title";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, dayNumber);
            statement.setInt(2, monthNumber);
            statement.setInt(3, yearNumber);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] filterMoviesByGenreIds(int[] genreIds) {
        if (genreIds == null || genreIds.length == 0) {
            return new Movie[0];
        }

        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT m.* FROM movies m " +
                            "INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                            "WHERE mg.genre_id IN ("
            );

            for (int i = 0; i < genreIds.length; i++) {
                query.append("?");
                if (i < genreIds.length - 1) query.append(",");
            }
            query.append(") ORDER BY m.movie_title");

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < genreIds.length; i++) {
                statement.setInt(i + 1, genreIds[i]);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] filterMoviesByGenreNames(String[] genreNames) {
        if (genreNames == null || genreNames.length == 0) {
            return new Movie[0];
        }

        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT m.* FROM movies m " +
                            "INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                            "INNER JOIN genres g ON mg.genre_id = g.genre_id " +
                            "WHERE g.genre_name IN ("
            );

            for (int i = 0; i < genreNames.length; i++) {
                query.append("?");
                if (i < genreNames.length - 1) query.append(",");
            }
            query.append(") ORDER BY m.movie_title");

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < genreNames.length; i++) {
                statement.setString(i + 1, genreNames[i]);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] filterMoviesByRating(int minRating) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_rating >= ? ORDER BY movie_rating DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, minRating);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    @Override
    public Movie[] searchMoviesByTitle(String searchString) {
        List<Movie> movieList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movies WHERE movie_title LIKE ? ORDER BY movie_title";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + searchString + "%");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.movieId = rs.getInt("movie_id");
                movie.movieTitle = rs.getString("movie_title");
                movie.movieDescription = rs.getString("movie_description");
                movie.movieRating = rs.getInt("movie_rating");
                movie.moviePosterURL = rs.getString("movie_poster_url");
                movie.createdByAdmin = rs.getInt("created_by_admin");
                movie.createdAt = rs.getString("created_at");
                movie.updatedAt = rs.getString("updated_at");
                movieList.add(movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieList.toArray(new Movie[0]);
    }

    // ===== SCHEDULE METHODS =====

    @Override
    public MovieSchedule[] getAllMovieSchedules() {
        List<MovieSchedule> scheduleList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM movie_schedules ORDER BY screening_date, screening_time";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                MovieSchedule schedule = new MovieSchedule();
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.screeningDate = rs.getString("screening_date");
                schedule.screeningTime = rs.getString("screening_time");
                schedule.createdByAdmin = rs.getInt("created_by_admin");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scheduleList.toArray(new MovieSchedule[0]);
    }

    @Override
    public Schedule[] getSchedulesByMovieId(int movieId) throws MovieNotFoundException {
        List<Schedule> scheduleList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM schedules WHERE movie_id = ? ORDER BY schedule_id";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.scheduleId = rs.getInt("schedule_id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching schedules", movieId);
        }
        return scheduleList.toArray(new Schedule[0]);
    }

    @Override
    public Schedule[] getSchedulesByDate(int dayNumber, int monthNumber, int yearNumber) {
        List<Schedule> scheduleList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT s.* FROM schedules s " +
                    "INNER JOIN movie_schedules ms ON s.movie_schedule_id = ms.movie_schedule_id " +
                    "WHERE DAY(ms.screening_date) = ? " +
                    "AND MONTH(ms.screening_date) = ? " +
                    "AND YEAR(ms.screening_date) = ? " +
                    "ORDER BY ms.screening_time";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, dayNumber);
            statement.setInt(2, monthNumber);
            statement.setInt(3, yearNumber);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.scheduleId = rs.getInt("schedule_id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                scheduleList.add(schedule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scheduleList.toArray(new Schedule[0]);
    }

    @Override
    public MovieSchedule getMovieScheduleById(int scheduleId) throws ScheduleNotFoundException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT ms.* FROM movie_schedules ms " +
                    "INNER JOIN schedules s ON ms.movie_schedule_id = s.movie_schedule_id " +
                    "WHERE s.schedule_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                MovieSchedule schedule = new MovieSchedule();
                schedule.movieScheduleId = rs.getInt("movie_schedule_id");
                schedule.screeningDate = rs.getString("screening_date");
                schedule.screeningTime = rs.getString("screening_time");
                schedule.createdByAdmin = rs.getInt("created_by_admin");
                schedule.createdAt = rs.getString("created_at");
                schedule.updatedAt = rs.getString("updated_at");
                return schedule;
            } else {
                throw new ScheduleNotFoundException("Schedule not found", scheduleId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching schedule", scheduleId);
        }
    }

    @Override
    public ScheduleDetails getScheduleDetailsById(int scheduleId) throws ScheduleNotFoundException {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM v_schedule_details WHERE schedule_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, scheduleId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                ScheduleDetails details = new ScheduleDetails();
                details.scheduleId = rs.getInt("schedule_id");
                details.movieId = rs.getInt("movie_id");
                details.movieScheduleId = rs.getInt("movie_schedule_id");
                details.movieTitle = rs.getString("movie_title");
                details.movieDescription = rs.getString("movie_description");
                details.movieRating = rs.getInt("movie_rating");
                details.moviePosterURL = rs.getString("movie_poster_url");
                details.movieGenres = rs.getString("movie_genres");
                details.screeningDate = rs.getString("screening_date");
                details.screeningTime = rs.getString("screening_time");
                details.availableSeats = rs.getInt("available_seats");
                details.totalSeats = rs.getInt("total_seats");
                details.createdAt = rs.getString("created_at");
                details.updatedAt = rs.getString("updated_at");
                return details;
            } else {
                throw new ScheduleNotFoundException("Schedule not found", scheduleId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ScheduleNotFoundException("Error fetching schedule details", scheduleId);
        }
    }

    // ===== GENRE METHODS =====

    @Override
    public Genre[] getAllGenres() {
        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM genres ORDER BY genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                genreList.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genreList.toArray(new Genre[0]);
    }

    @Override
    public Genre getGenreById(int genreId) {
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT * FROM genres WHERE genre_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, genreId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                return genre;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Genre[] getGenresByMovieId(int movieId) throws MovieNotFoundException {
        List<Genre> genreList = new ArrayList<>();
        try (Connection connection = DatabaseAccess.getConnection()) {
            String query = "SELECT g.* FROM genres g " +
                    "INNER JOIN movie_genres mg ON g.genre_id = mg.genre_id " +
                    "WHERE mg.movie_id = ? ORDER BY g.genre_name";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, movieId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre();
                genre.genreId = rs.getInt("genre_id");
                genre.genreName = rs.getString("genre_name");
                genre.genreDescription = rs.getString("genre_description");
                genre.createdAt = rs.getString("created_at");
                genre.updatedAt = rs.getString("updated_at");
                genreList.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MovieNotFoundException("Error fetching genres", movieId);
        }
        return genreList.toArray(new Genre[0]);
    }

    private Movie attachImageDataToMovie(Movie movie) {
        if (movie.moviePosterURL == null || movie.moviePosterURL.isEmpty()) {
            return movie;
        }

        try {
            // Read image file
            java.nio.file.Path imagePath = java.nio.file.Paths.get(movie.moviePosterURL);

            if (!java.nio.file.Files.exists(imagePath)) {
                System.err.println("⚠️ Image file not found: " + movie.moviePosterURL);
                return movie;
            }

            byte[] imageBytes = java.nio.file.Files.readAllBytes(imagePath);
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

            // Concatenate: path|||IMAGE_DATA|||base64data
            movie.moviePosterURL = movie.moviePosterURL + "|||IMAGE_DATA|||" + base64Image;

            System.out.println("✅ Attached image data for movie " + movie.movieId);

        } catch (IOException e) {
            System.err.println("❌ Error attaching image data: " + e.getMessage());
        }

        return movie;
    }
}