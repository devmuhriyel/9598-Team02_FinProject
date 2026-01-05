package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.util.StringConverter;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

import java.util.HashSet;
import java.util.Set;

public class AdminBookingController {

    private AdminBookingView view;
    private AdminBookingModel model;
    private int currentAdminId;
    private ObservableList<BookingDetails> bookingList = FXCollections.observableArrayList();
    private FilteredList<BookingDetails> filteredData;
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
        // Add null checks to prevent errors
        if (view.getColCustomerName() != null) {
            view.getColCustomerName().setCellValueFactory(cell -> 
                    new SimpleStringProperty(cell.getValue() != null && cell.getValue().customerName != null ? 
                            cell.getValue().customerName : "N/A"));
        }
        if (view.getColPrice() != null) {
            view.getColPrice().setCellValueFactory(cell -> 
                    new SimpleObjectProperty<>(cell.getValue() != null ? cell.getValue().totalPrice : 0.0));
        }
        if (view.getColStatus() != null) {
            view.getColStatus().setCellValueFactory(cell -> 
                    new SimpleStringProperty(cell.getValue() != null && cell.getValue().bookingStatus != null ? 
                            cell.getValue().bookingStatus : ""));
        }

        filteredData = new FilteredList<>(bookingList, p -> true);
        SortedList<BookingDetails> sortedData = new SortedList<>(filteredData);
        if (view.getBookingTable() != null) {
            sortedData.comparatorProperty().bind(view.getBookingTable().comparatorProperty());
        }
        view.setBookingList(sortedData);
    }

    private void initializeFilters() {
        // Status Filter
        if (view.getComboStatus() != null) {
            view.getComboStatus().getItems().addAll("All", "pending", "confirmed", "cancelled");
            view.getComboStatus().getSelectionModel().selectFirst();
            view.getComboStatus().setOnAction(e -> applyClientFilter());
        }
        
        if (view.getComboMovie() != null) {
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
            view.getComboMovie().setOnAction(e -> loadData());
        }
        
        if (view.getComboSchedule() != null) {
            view.getComboSchedule().setOnAction(e -> applyClientFilter());
        }
        
        if (view.getSearchField() != null) {
            view.getSearchField().textProperty().addListener((obs, old, newVal) -> applyClientFilter());
        }
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
        Movie selectedMovie = view.getComboMovie() != null ? view.getComboMovie().getValue() : null;

        new Thread(() -> {
            try {
                BookingDetails[] bookings;

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
                        for (BookingDetails bd : bookings) {
                            if (bd != null) {
                                bookingList.add(bd);
                                uniqueSchedules.add(String.valueOf(bd.scheduleId));
                            }
                        }
                    }

                    if (view.getComboSchedule() != null) {
                        view.getComboSchedule().getItems().setAll(uniqueSchedules);
                        view.getComboSchedule().getSelectionModel().selectFirst();
                    }

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
        if (filteredData == null) return; // Safety check
        
        String searchText = view.getSearchField() != null ? view.getSearchField().getText().toLowerCase().trim() : "";
        String selectedStatus = view.getComboStatus() != null ? view.getComboStatus().getValue() : null;
        String selectedSchedule = view.getComboSchedule() != null ? view.getComboSchedule().getValue() : null;

        filteredData.setPredicate(booking -> {
            if (booking == null) return false;
            
            if (!searchText.isEmpty()) {
                // Search by customer name
                boolean matchesCustomerName = booking.customerName != null && 
                        booking.customerName.toLowerCase().contains(searchText);
                // Also allow searching by booking ID if needed
                boolean matchesId = String.valueOf(booking.bookingId).contains(searchText);
                if (!matchesCustomerName && !matchesId) return false;
            }

            if (selectedStatus != null && !"All".equals(selectedStatus)) {
                if (booking.bookingStatus == null || !selectedStatus.equalsIgnoreCase(booking.bookingStatus)) return false;
            }

            if (selectedSchedule != null && !"All".equals(selectedSchedule)) {
                if (!selectedSchedule.equals(String.valueOf(booking.scheduleId))) return false;
            }

            return true;
        });
    }

    private void resetFilters() {
        if (view.getSearchField() != null) {
            view.getSearchField().clear();
        }
        if (view.getComboStatus() != null) {
            view.getComboStatus().getSelectionModel().selectFirst();
        }
        if (view.getComboMovie() != null) {
            view.getComboMovie().getSelectionModel().selectFirst();
        }
    }
}