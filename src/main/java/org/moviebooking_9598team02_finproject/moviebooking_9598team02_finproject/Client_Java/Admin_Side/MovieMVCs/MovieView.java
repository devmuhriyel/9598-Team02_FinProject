package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MovieView {

    @FXML private StackPane rootContentStack;
    @FXML private VBox movieView;

    // Search Section
    @FXML private TextField searchField;
    @FXML private Label statusMessageLabel;

    // Movie Table
    @FXML private TableView<MovieTableData> movieTableView;
    @FXML private TableColumn<MovieTableData, Integer> colMovieId;
    @FXML private TableColumn<MovieTableData, String> colTitle;
    @FXML private TableColumn<MovieTableData, String> colGenre;
    @FXML private TableColumn<MovieTableData, Integer> colDuration;
    @FXML private TableColumn<MovieTableData, Integer> colRating;
    @FXML private TableColumn<MovieTableData, String> colReleaseDate;
    @FXML private TableColumn<MovieTableData, String> colStatus;

    // Movie Details Section
    @FXML private Label currentMovieDetailsLabel;

    // Input Fields - Movie Information
    @FXML private TextField titleInput;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField durationInput;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextField releaseDateInput;
    @FXML private ComboBox<String> statusComboBox;

    // Input Fields - Additional Details
    @FXML private TextArea descriptionInput;
    @FXML private TextField posterUrlInput;
    @FXML private TextField directorInput;
    @FXML private TextField castInput;

    // Action Buttons
    @FXML private Button btnSaveMovie;
    @FXML private Button btnUpdateMovie;
    @FXML private Button btnDeleteMovie;

    // ===== INITIALIZATION =====

    public void initializeTableColumns() {
        colMovieId.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colReleaseDate.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ===== GETTERS =====

    public StackPane getRootContentStack() { return rootContentStack; }
    public VBox getMovieView() { return movieView; }
    public TextField getSearchField() { return searchField; }
    public Label getStatusMessageLabel() { return statusMessageLabel; }
    public TableView<MovieTableData> getMovieTableView() { return movieTableView; }
    public Label getCurrentMovieDetailsLabel() { return currentMovieDetailsLabel; }

    // Input Fields
    public TextField getTitleInput() { return titleInput; }
    public ComboBox<String> getGenreComboBox() { return genreComboBox; }
    public TextField getDurationInput() { return durationInput; }
    public ComboBox<Integer> getRatingComboBox() { return ratingComboBox; }
    public TextField getReleaseDateInput() { return releaseDateInput; }
    public ComboBox<String> getStatusComboBox() { return statusComboBox; }
    public TextArea getDescriptionInput() { return descriptionInput; }
    public TextField getPosterUrlInput() { return posterUrlInput; }
    public TextField getDirectorInput() { return directorInput; }
    public TextField getCastInput() { return castInput; }

    // Buttons
    public Button getBtnSaveMovie() { return btnSaveMovie; }
    public Button getBtnUpdateMovie() { return btnUpdateMovie; }
    public Button getBtnDeleteMovie() { return btnDeleteMovie; }

    // ===== METHODS FOR CONTROLLER TO UPDATE THE UI =====

    /**
     * Show a status message to the user
     */
    public void showStatusMessage(String message, boolean isError) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle(isError ?
                    "-fx-text-fill: #ff2525; -fx-font-weight: bold;" :
                    "-fx-text-fill: #00CC66; -fx-font-weight: bold;");
            statusMessageLabel.setVisible(true);
        }
    }

    /**
     * Clear the status message
     */
    public void clearStatusMessage() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
            statusMessageLabel.setVisible(false);
        }
    }

    /**
     * Update the selected movie details label
     */
    public void updateMovieDetailsLabel(String details) {
        if (currentMovieDetailsLabel != null) {
            currentMovieDetailsLabel.setText(details);
            currentMovieDetailsLabel.setStyle("-fx-text-fill: #FFFFFF;");
        }
    }

    /**
     * Clear all input fields
     */
    public void clearAllFields() {
        if (titleInput != null) titleInput.clear();
        if (genreComboBox != null) genreComboBox.getSelectionModel().clearSelection();
        if (durationInput != null) durationInput.clear();
        if (ratingComboBox != null) ratingComboBox.getSelectionModel().clearSelection();
        if (releaseDateInput != null) releaseDateInput.clear();
        if (statusComboBox != null) statusComboBox.getSelectionModel().clearSelection();
        if (descriptionInput != null) descriptionInput.clear();
        if (posterUrlInput != null) posterUrlInput.clear();
        if (directorInput != null) directorInput.clear();
        if (castInput != null) castInput.clear();
    }

    /**
     * Enable or disable edit mode buttons
     */
    public void setEditMode(boolean isEditMode) {
        if (btnSaveMovie != null) btnSaveMovie.setDisable(isEditMode);
        if (btnUpdateMovie != null) btnUpdateMovie.setDisable(!isEditMode);
        if (btnDeleteMovie != null) btnDeleteMovie.setDisable(!isEditMode);
    }

    // ===== INNER CLASS FOR TABLE DATA =====

    public static class MovieTableData {
        private final Integer movieId;
        private final String title;
        private final String genre;
        private final Integer duration;
        private final Integer rating;
        private final String releaseDate;
        private final String status;

        public MovieTableData(Integer movieId, String title, String genre,
                              Integer duration, Integer rating, String releaseDate, String status) {
            this.movieId = movieId;
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.rating = rating;
            this.releaseDate = releaseDate;
            this.status = status;
        }

        public Integer getMovieId() { return movieId; }
        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public Integer getDuration() { return duration; }
        public Integer getRating() { return rating; }
        public String getReleaseDate() { return releaseDate; }
        public String getStatus() { return status; }
    }
}