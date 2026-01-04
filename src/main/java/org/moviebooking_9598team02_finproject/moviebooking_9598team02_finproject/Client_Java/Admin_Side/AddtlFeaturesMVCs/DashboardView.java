package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Movie;

import java.util.List;

public class DashboardView {
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Label systemTotalRevenueResult;
    @FXML private Label selectedMovieTitleLabel;
    @FXML private Label selectedMovieRevenueResult;
    @FXML private Label lblStatus;

    @FXML public TableView<Movie> allMoviesTableView;

    @FXML private TableColumn<Movie, String> allColTitle;
    @FXML private TableColumn<Movie, String> allColGenre;
    @FXML private TableColumn<Movie, String> allColStatus;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnClearSearch;
    @FXML private Button btnRefresh;


    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        if (allColTitle != null) {
            allColTitle.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().movieTitle)
            );
        }
        if (allColGenre != null) {
            // Leave this as a placeholder, the controller will set the CellValueFactory
            // to fetch the genre names based on movie ID.
            allColGenre.setCellValueFactory(cellData -> new SimpleStringProperty("Loading..."));
        }
        if (allColStatus != null) {
            allColStatus.setCellValueFactory(cellData ->
                    // Placeholder as status is not directly a field in the CORBA Movie struct
                    new SimpleStringProperty(cellData.getValue().createdAt)
            );
        }
    }

    // Getters
    public String getSelectedGenre() {
        return genreComboBox != null ? genreComboBox.getValue() : null;
    }

    public String getSearchQuery() {
        return searchField != null ? searchField.getText().toLowerCase().trim() : "";
    }

    public void setStatus(String msg, boolean isError) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
            lblStatus.setStyle(isError ? "-fx-text-fill: #ff2525;" : "-fx-text-fill: #4CAF50;");
        }
    }

    public void clearSearch() {
        if (searchField != null) {
            searchField.setText("");
        }
    }

    // Event Handlers
    public void setGenreComboBoxAction(Runnable action) {
        if (genreComboBox != null) {
            genreComboBox.setOnAction(event -> action.run());
        }
    }

    public void setSearchButtonAction(Runnable action) {
        if (btnSearch != null) {
            btnSearch.setOnAction(event -> action.run());
        }
    }

    public void setClearSearchButtonAction(Runnable action) {
        if (btnClearSearch != null) {
            btnClearSearch.setOnAction(event -> action.run());
        }
    }

    public void setRefreshButtonAction(Runnable action) {
        if (btnRefresh != null) {
            btnRefresh.setOnAction(event -> action.run());
        }
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
    }

    // Table data setters
    public void setAllMoviesTableData(List<Movie> data) {
        if (allMoviesTableView != null) {
            allMoviesTableView.setItems(FXCollections.observableArrayList(data));
        }
    }
}