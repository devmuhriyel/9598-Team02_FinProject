package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerMovieView implements Initializable {

    // FXML injected fields
    @FXML private StackPane rootContentStack;
    @FXML private VBox movieView;

    // Search and Filter Section
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreFilterComboBox;
    @FXML private ComboBox<Integer> ratingFilterComboBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private Label statusMessageLabel;

    // Movie Display Area
    @FXML private FlowPane movieCardsContainer;
    @FXML private ScrollPane movieScrollPane;

    // Movie Details Panel
    @FXML private VBox movieDetailsPanel;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieRatingLabel;
    @FXML private Label movieGenresLabel;
    @FXML private Label movieDescriptionLabel;
    @FXML private ImageView moviePosterImageView;

    // Action Buttons
    @FXML private Button viewSchedulesButton;
    @FXML private Button backButton;
    @FXML private Button homeButton;

    // ===== INITIALIZATION =====

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // FXML injection happens automatically before this method is called
        if (movieDetailsPanel != null) {
            movieDetailsPanel.setVisible(false);
        }
    }

    // ===== GETTERS =====

    public StackPane getRootContentStack() {
        return rootContentStack;
    }

    public VBox getMovieView() {
        return movieView;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public ComboBox<String> getGenreFilterComboBox() {
        return genreFilterComboBox;
    }

    public ComboBox<Integer> getRatingFilterComboBox() {
        return ratingFilterComboBox;
    }

    public DatePicker getDateFilterPicker() {
        return dateFilterPicker;
    }

    public Label getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public FlowPane getMovieCardsContainer() {
        return movieCardsContainer;
    }

    public ScrollPane getMovieScrollPane() {
        return movieScrollPane;
    }

    public VBox getMovieDetailsPanel() {
        return movieDetailsPanel;
    }

    public Label getMovieTitleLabel() {
        return movieTitleLabel;
    }

    public Label getMovieRatingLabel() {
        return movieRatingLabel;
    }

    public Label getMovieGenresLabel() {
        return movieGenresLabel;
    }

    public Label getMovieDescriptionLabel() {
        return movieDescriptionLabel;
    }

    public ImageView getMoviePosterImageView() {
        return moviePosterImageView;
    }

    public Button getViewSchedulesButton() {
        return viewSchedulesButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getHomeButton() {
        return homeButton;
    }

    // ===== UI UPDATE METHODS =====

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
     * Show the movie details panel
     */
    public void showMovieDetails() {
        if (movieDetailsPanel != null) {
            movieDetailsPanel.setVisible(true);
        }
    }

    /**
     * Hide the movie details panel
     */
    public void hideMovieDetails() {
        if (movieDetailsPanel != null) {
            movieDetailsPanel.setVisible(false);
        }
    }

    /**
     * Update movie details display
     */
    public void updateMovieDetails(String title, String rating, String genres, String description) {
        if (movieTitleLabel != null) {
            movieTitleLabel.setText(title);
        }
        if (movieRatingLabel != null) {
            movieRatingLabel.setText(rating);
        }
        if (movieGenresLabel != null) {
            movieGenresLabel.setText(genres);
        }
        if (movieDescriptionLabel != null) {
            movieDescriptionLabel.setText(description);
        }
    }

    /**
     * Clear all movie cards from the container
     */
    public void clearMovieCards() {
        if (movieCardsContainer != null) {
            movieCardsContainer.getChildren().clear();
        }
    }

    /**
     * Clear all filters
     */
    public void clearFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (genreFilterComboBox != null) {
            genreFilterComboBox.getSelectionModel().clearSelection();
        }
        if (ratingFilterComboBox != null) {
            ratingFilterComboBox.getSelectionModel().clearSelection();
        }
        if (dateFilterPicker != null) {
            dateFilterPicker.setValue(null);
        }
    }
}