package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Booking;

public class AdminBookingView {

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> colId, colCustId, colSchedId;
    @FXML private TableColumn<Booking, Double> colPrice;
    @FXML private TableColumn<Booking, String> colStatus, colDate;

    @FXML private TextField searchField;
    @FXML private Button btnSearch, btnClearSearch;
    @FXML private Button btnCancelBooking, btnRefresh;
    @FXML private Label lblStatus;

    public void setBookingList(ObservableList<Booking> list) {
        bookingTable.setItems(list);
    }

    public void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setStyle(isError ? "-fx-text-fill: #ff2525;" : "-fx-text-fill: #28a745;");
    }

    public Booking getSelectedBooking() {
        return bookingTable.getSelectionModel().getSelectedItem();
    }


    public TableView<Booking> getBookingTable() { return bookingTable; }


    public TableColumn<Booking, Integer> getColId() { return colId; }
    public TableColumn<Booking, Integer> getColCustId() { return colCustId; }
    public TableColumn<Booking, Integer> getColSchedId() { return colSchedId; }
    public TableColumn<Booking, Double> getColPrice() { return colPrice; }
    public TableColumn<Booking, String> getColStatus() { return colStatus; }
    public TableColumn<Booking, String> getColDate() { return colDate; }


    public TextField getSearchField() { return searchField; }
    public Button getBtnSearch() { return btnSearch; }
    public Button getBtnClearSearch() { return btnClearSearch; }
    public Button getBtnCancelBooking() { return btnCancelBooking; }
    public Button getBtnRefresh() { return btnRefresh; }
}