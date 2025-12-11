package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

import java.time.LocalDate;
import java.util.List;

public class DashboardView {
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Label systemTotalRevenueResult;
    @FXML private Label selectedMovieTitleLabel;
    @FXML private Label selectedMovieRevenueResult;
    @FXML private Label selectedMovieOccupancyResult;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label detailedReportStandardSalesResult;
    @FXML private Label detailedReportVipSalesResult;

    // Table Views
    @FXML public TableView<Movie> nowShowingTableView;
    @FXML public TableView<Movie> upcomingTableView;
    @FXML public TableView<Movie> doneTableView;
    @FXML private TableView<Movie> topMoviesTableView;
    @FXML public TableView<Movie> allMoviesTableView;

    // All Movies Table Columns
    @FXML private TableColumn<Movie, Integer> allColId;
    @FXML private TableColumn<Movie, String> allColTitle;
    @FXML private TableColumn<Movie, String> allColGenre;
    @FXML private TableColumn<Movie, String> allColStatus;

    // Now Showing Table Columns
    @FXML private TableColumn<Movie, Integer> nsColId;
    @FXML private TableColumn<Movie, String> nsColTitle;
    @FXML private TableColumn<Movie, String> nsColGenre;
    @FXML private TableColumn<Movie, String> nsColBookings;

    // Upcoming Table Columns
    @FXML private TableColumn<Movie, Integer> upColId;
    @FXML private TableColumn<Movie, String> upColTitle;
    @FXML private TableColumn<Movie, String> upColGenre;
    @FXML private TableColumn<Movie, String> upColRelease;

    // Done Table Columns
    @FXML private TableColumn<Movie, Integer> doneColId;
    @FXML private TableColumn<Movie, String> doneColTitle;
    @FXML private TableColumn<Movie, String> doneColGenre;
    @FXML private TableColumn<Movie, String> doneColRevenue;

    // Top Movies Table Columns
    @FXML private TableColumn<Movie, Integer> topColId;
    @FXML private TableColumn<Movie, String> topColTitle;
    @FXML private TableColumn<Movie, String> topColGenre;
    @FXML private TableColumn<Movie, String> topColBookings;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        // ===================================================================
        // CRITICAL: CORBA-generated classes have PUBLIC FIELDS, not getters
        // We must use lambda expressions to access the fields directly
        // ===================================================================

        // Configure All Movies Table
        if (allColId != null) {
            allColId.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().movieId).asObject()
            );
        }
        if (allColTitle != null) {
            allColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (allColGenre != null) {
            allColGenre.setCellValueFactory(cellData -> {
                Movie movie = cellData.getValue();
                String genres = getGenreNamesForMovie(movie.movieId);
                return new SimpleStringProperty(genres);
            });
        }
        if (allColStatus != null) {
            allColStatus.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().createdAt) // Placeholder
            );
        }

        // Configure Now Showing Table
        if (nsColId != null) {
            nsColId.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().movieId).asObject()
            );
        }
        if (nsColTitle != null) {
            nsColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (nsColGenre != null) {
            nsColGenre.setCellValueFactory(cellData ->
                    new SimpleStringProperty("Action") // genre of movie
            );
        }
        if (nsColBookings != null) {
            nsColBookings.setCellValueFactory(cellData ->
                    new SimpleStringProperty("0") // status
            );
        }

        // Configure Upcoming Table
        if (upColId != null) {
            upColId.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().movieId).asObject()
            );
        }
        if (upColTitle != null) {
            upColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (upColGenre != null) {
            upColGenre.setCellValueFactory(cellData ->
                    new SimpleStringProperty("Action") // Placeholder
            );
        }
        if (upColRelease != null) {
            upColRelease.setCellValueFactory(cellData ->
                    new SimpleStringProperty("TBA") // Placeholder
            );
        }

        // Configure Done Table
        if (doneColId != null) {
            doneColId.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().movieId).asObject()
            );
        }
        if (doneColTitle != null) {
            doneColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (doneColGenre != null) {
            doneColGenre.setCellValueFactory(cellData ->
                    new SimpleStringProperty("Action") // Placeholder
            );
        }
        if (doneColRevenue != null) {
            doneColRevenue.setCellValueFactory(cellData ->
                    new SimpleStringProperty("₱0.00") // Placeholder
            );
        }

        // Configure Top Movies Table
        if (topColId != null) {
            topColId.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().movieId).asObject()
            );
        }
        if (topColTitle != null) {
            topColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (topColGenre != null) {
            topColGenre.setCellValueFactory(cellData ->
                    new SimpleStringProperty("Action") // Placeholder
            );
        }
        if (topColBookings != null) {
            topColBookings.setCellValueFactory(cellData ->
                    new SimpleStringProperty("0") // Placeholder
            );
        }
    }

    // Getters
    public String getSelectedGenre() {
        return genreComboBox != null ? genreComboBox.getValue() : null;
    }

    public LocalDate getStartDate() {
        return startDatePicker != null ? startDatePicker.getValue() : null;
    }

    public LocalDate getEndDate() {
        return endDatePicker != null ? endDatePicker.getValue() : null;
    }

    // Event Handlers
    public void setGenreComboBoxAction(Runnable action) {
        if (genreComboBox != null) {
            genreComboBox.setOnAction(event -> action.run());
        }
    }

    public void setDateRangeAction(Runnable action) {
        if (startDatePicker != null) {
            startDatePicker.setOnAction(event -> action.run());
        }
        if (endDatePicker != null) {
            endDatePicker.setOnAction(event -> action.run());
        }
    }

    private String getGenreNamesForMovie(int movieId) {
        // This will be called from the controller
        // For now, return empty string - controller will set this up properly
        return "";
    }

    public void setAllMoviesSelectionListener(Runnable action) {
        if (allMoviesTableView != null) {
            allMoviesTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) action.run();
                    }
            );
        }
    }

    public void setNowShowingSelectionListener(Runnable action) {
        if (nowShowingTableView != null) {
            nowShowingTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) action.run();
                    }
            );
        }
    }

    public void setUpcomingSelectionListener(Runnable action) {
        if (upcomingTableView != null) {
            upcomingTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) action.run();
                    }
            );
        }
    }

    public void setDoneSelectionListener(Runnable action) {
        if (doneTableView != null) {
            doneTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) action.run();
                    }
            );
        }
    }

    // Data setters
    public void setGenreComboBoxOptions(List<String> genreNames) {
        if (genreComboBox != null) {
            genreComboBox.setItems(FXCollections.observableArrayList(genreNames));
            if (!genreNames.isEmpty()) {
                genreComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    public void updateSystemTotalRevenue(double revenue) {
        if (systemTotalRevenueResult != null) {
            systemTotalRevenueResult.setText(String.format("₱ %,.2f", revenue));
        }
    }

    public void updateMovieAnalytics(String title, double revenue, double occupancyPercentage) {
        if (selectedMovieTitleLabel != null) {
            selectedMovieTitleLabel.setText(title);
        }
        if (selectedMovieRevenueResult != null) {
            selectedMovieRevenueResult.setText(String.format("₱ %,.2f", revenue));
        }
        if (selectedMovieOccupancyResult != null) {
            selectedMovieOccupancyResult.setText(String.format("%.1f%%", occupancyPercentage));
        }
    }

    public void updateDetailedSalesReport(int standardTickets, int vipTickets) {
        if (detailedReportStandardSalesResult != null) {
            detailedReportStandardSalesResult.setText(String.format("%,d", standardTickets));
        }
        if (detailedReportVipSalesResult != null) {
            detailedReportVipSalesResult.setText(String.format("%,d", vipTickets));
        }
    }

    // Table data setters
    public void setAllMoviesTableData(List<Movie> data) {
        if (allMoviesTableView != null) {
            allMoviesTableView.setItems(FXCollections.observableArrayList(data));
        }
    }

    public void setNowShowingTableData(List<Movie> data) {
        if (nowShowingTableView != null) {
            nowShowingTableView.setItems(FXCollections.observableArrayList(data));
        }
    }

    public void setUpcomingTableData(List<Movie> data) {
        if (upcomingTableView != null) {
            upcomingTableView.setItems(FXCollections.observableArrayList(data));
        }
    }

    public void setDoneTableData(List<Movie> data) {
        if (doneTableView != null) {
            doneTableView.setItems(FXCollections.observableArrayList(data));
        }
    }

    public void setTopMoviesTableData(List<Movie> data) {
        if (topMoviesTableView != null) {
            topMoviesTableView.setItems(FXCollections.observableArrayList(data));
        }
    }
}