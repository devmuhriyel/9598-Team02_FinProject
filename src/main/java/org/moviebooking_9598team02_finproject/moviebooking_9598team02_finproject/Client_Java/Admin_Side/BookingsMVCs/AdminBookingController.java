package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.util.StringConverter;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Booking;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

import java.util.HashSet;
import java.util.Set;

public class AdminBookingController {

    private AdminBookingView view;
    private AdminBookingModel model;
    private int currentAdminId;
    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredData;
    private ObservableList<Movie> movieList = FXCollections.observableArrayList();

    public AdminBookingController(AdminBookingView view, AdminBookingModel model, int adminId) {
        this.view = view;
        this.model = model;
        this.currentAdminId = adminId;

        initializeTable();
        initializeFilters();
        initializeButtons();

        loadMovies();
        loadData();
    }

    private void initializeTable() {
        view.getColId().setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().bookingId));
        view.getColCustId().setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().customerId));
        view.getColSchedId().setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().scheduleId));
        view.getColPrice().setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().totalPrice));
        view.getColStatus().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bookingStatus));
        view.getColDate().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().createdAt));

        filteredData = new FilteredList<>(bookingList, p -> true);
        SortedList<Booking> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(view.getBookingTable().comparatorProperty());
        view.setBookingList(sortedData);
    }

    private void initializeFilters() {
        // Status Filter
        view.getComboStatus().getItems().addAll("All", "pending", "confirmed", "cancelled");
        view.getComboStatus().getSelectionModel().selectFirst();
        view.getComboMovie().setItems(movieList);
        view.getComboMovie().setConverter(new StringConverter<Movie>() {
            @Override
            public String toString(Movie movie) {
                if (movie == null) return "All Movies";
                if (movie.movieId == -1) return "All Movies";
                return movie.movieId + ": " + movie.movieTitle;
            }

            @Override
            public Movie fromString(String string) {
                return null;
            }
        });

        view.getComboStatus().setOnAction(e -> applyClientFilter());
        view.getComboSchedule().setOnAction(e -> applyClientFilter());
        view.getSearchField().textProperty().addListener((obs, old, newVal) -> applyClientFilter());
        view.getComboMovie().setOnAction(e -> loadData());
    }

    private void initializeButtons() {
        view.getBtnRefresh().setOnAction(e -> loadData());
        view.getBtnClearFilters().setOnAction(e -> resetFilters());
    }

    // LOAD MOVIES
    private void loadMovies() {
        new Thread(() -> {
            try {
                Movie[] movies = model.getAllMovies(currentAdminId);
                Platform.runLater(() -> {
                    movieList.clear();
                    Movie allMovie = new Movie();
                    allMovie.movieId = -1;
                    allMovie.movieTitle = "All Movies";
                    movieList.add(allMovie);

                    if (movies != null) {
                        movieList.addAll(movies);
                    }
                    view.getComboMovie().getSelectionModel().selectFirst();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // LOAD DATA
    private void loadData() {
        Movie selectedMovie = view.getComboMovie().getValue();

        new Thread(() -> {
            try {
                Booking[] bookings;

                if (selectedMovie == null || selectedMovie.movieId == -1) {
                    bookings = model.getAllBookings(currentAdminId);
                } else {
                    bookings = model.getBookingsByMovie(currentAdminId, selectedMovie.movieId);
                }

                Platform.runLater(() -> {
                    bookingList.clear();
                    Set<String> uniqueSchedules = new HashSet<>();
                    uniqueSchedules.add("All");

                    if (bookings != null) {
                        for (Booking b : bookings) {
                            bookingList.add(b);
                            uniqueSchedules.add(String.valueOf(b.scheduleId));
                        }
                    }

                    view.getComboSchedule().getItems().setAll(uniqueSchedules);
                    view.getComboSchedule().getSelectionModel().selectFirst();

                    view.setStatus("Data loaded successfully.", false);
                    applyClientFilter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> view.setStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    // CLIENT FILTER
    private void applyClientFilter() {
        String searchText = view.getSearchField().getText().toLowerCase().trim();
        String selectedStatus = view.getComboStatus().getValue();
        String selectedSchedule = view.getComboSchedule().getValue();

        filteredData.setPredicate(booking -> {
            if (!searchText.isEmpty()) {
                boolean matchesId = String.valueOf(booking.bookingId).contains(searchText);
                boolean matchesCust = String.valueOf(booking.customerId).contains(searchText);
                if (!matchesId && !matchesCust) return false;
            }

            if (selectedStatus != null && !"All".equals(selectedStatus)) {
                if (!selectedStatus.equalsIgnoreCase(booking.bookingStatus)) return false;
            }

            if (selectedSchedule != null && !"All".equals(selectedSchedule)) {
                if (!selectedSchedule.equals(String.valueOf(booking.scheduleId))) return false;
            }

            return true;
        });
    }

    private void resetFilters() {
        view.getSearchField().clear();
        view.getComboStatus().getSelectionModel().selectFirst();
        view.getComboMovie().getSelectionModel().selectFirst();
    }
}