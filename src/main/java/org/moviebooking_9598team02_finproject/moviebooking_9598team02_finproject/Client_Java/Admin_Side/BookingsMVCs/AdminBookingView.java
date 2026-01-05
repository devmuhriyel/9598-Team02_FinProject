package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.BookingDetails;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

public class AdminBookingView {

    @FXML private TableView<BookingDetails> bookingTable;
    @FXML private TableColumn<BookingDetails, String> colCustomerName;
    @FXML private TableColumn<BookingDetails, Double> colPrice;
    @FXML private TableColumn<BookingDetails, String> colStatus;

    @FXML private TextField searchField;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatus;

    @FXML private ComboBox<String> comboStatus;
    @FXML private ComboBox<String> comboSchedule;
    @FXML private ComboBox<Movie> comboMovie;
    @FXML private Button btnClearFilters;

    public void setBookingList(ObservableList<BookingDetails> list) {
        bookingTable.setItems(list);
    }

    public void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setStyle(isError ? "-fx-text-fill: #ff2525;" : "-fx-text-fill: #28a745;");
    }

    public BookingDetails getSelectedBooking() {
        return bookingTable.getSelectionModel().getSelectedItem();
    }

    public TableView<BookingDetails> getBookingTable() { return bookingTable; }
    public TableColumn<BookingDetails, String> getColCustomerName() { return colCustomerName; }
    public TableColumn<BookingDetails, Double> getColPrice() { return colPrice; }
    public TableColumn<BookingDetails, String> getColStatus() { return colStatus; }
    public TextField getSearchField() { return searchField; }
    public Button getBtnRefresh() { return btnRefresh; }
    public ComboBox<String> getComboStatus() { return comboStatus; }
    public ComboBox<String> getComboSchedule() { return comboSchedule; }
    public ComboBox<Movie> getComboMovie() { return comboMovie; }
    public Button getBtnClearFilters() { return btnClearFilters; }
}