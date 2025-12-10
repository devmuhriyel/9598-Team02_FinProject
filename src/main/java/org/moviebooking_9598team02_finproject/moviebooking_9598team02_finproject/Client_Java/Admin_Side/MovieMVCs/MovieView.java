package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.util.Objects;

/**
 * MovieView - View component for Movie Management
 * Handles FXML bindings, UI updates, and card creation with image display
 */
public class MovieView {

    // ===== ROOT CONTAINERS =====
    @FXML private StackPane rootContentStack;
    @FXML private VBox movieView;

    // ===== GRID LAYOUT COMPONENTS =====
    @FXML private GridPane movieGridPane;
    @FXML private VBox addMovieView;
    @FXML private VBox editMovieView;
    @FXML private Button btnFilter;
    @FXML private Button btnEdit;
    @FXML private Button btnBackToGrid;
    @FXML private Button btnBackToGridFromEdit;
    @FXML private Button btnPreviousPage;
    @FXML private Button btnNextPage;
    @FXML private Label pageLabel;

    // ===== SEARCH SECTION =====
    @FXML private TextField searchField;
    @FXML private Label statusMessageLabel;

    // ===== MOVIE DETAILS SECTION =====
    @FXML private Label currentMovieDetailsLabel;

    // ===== INPUT FIELDS =====
    @FXML private TextField titleInput;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField durationInput;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextField releaseDateInput;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea descriptionInput;
    @FXML private TextField posterUrlInput;
    @FXML private Button btnBrowsePoster;
    @FXML private TextField directorInput;
    @FXML private TextField castInput;

    // ===== ACTION BUTTONS =====
    @FXML private Button btnSearch;
    @FXML private Button btnAddNewMovie;
    @FXML private Button btnSaveMovie;
    @FXML private Button btnUpdateMovie;
    @FXML private Button btnDeleteMovie;
    @FXML private Button btnClearFields;

    // ===== VIEW SWITCHING METHODS =====

    public void showGridView() {
        movieView.setVisible(true);
        movieView.setManaged(true);
        addMovieView.setVisible(false);
        addMovieView.setManaged(false);
        editMovieView.setVisible(false);
        editMovieView.setManaged(false);
    }

    public void showAddView() {
        movieView.setVisible(false);
        movieView.setManaged(false);
        addMovieView.setVisible(true);
        addMovieView.setManaged(true);
        editMovieView.setVisible(false);
        editMovieView.setManaged(false);
    }

    public void showEditView() {
        movieView.setVisible(false);
        movieView.setManaged(false);
        addMovieView.setVisible(false);
        addMovieView.setManaged(false);
        editMovieView.setVisible(true);
        editMovieView.setManaged(true);
    }

    // ===== MOVIE CARD CREATION WITH IMAGE =====

    /**
     * Create and add a movie card with poster image
     */
    public void createAndAddMovieCard(int movieId, String title, String genres, int rating,
                                      String posterURL, Runnable onClickAction, int col, int row) {
        VBox card = createMovieCardWithImage(movieId, title, genres, rating, posterURL, onClickAction);
        addCardToGrid(card, col, row);
    }

    /**
     * Create a movie card VBox with poster image display
     */
    private VBox createMovieCardWithImage(int movieId, String title, String genres, int rating,
                                          String posterURL, Runnable onClickAction) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #1A1A1A; " +
                "-fx-border-color: #ff2525; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #000000; " +
                        "-fx-border-color: #ff2525; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #1A1A1A; " +
                        "-fx-border-color: #ff2525; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"));

        // Click handler
        if (onClickAction != null) {
            card.setOnMouseClicked(e -> onClickAction.run());
        }

        // 🖼️ Poster Image or Circle Avatar
        if (posterURL != null && !posterURL.isEmpty()) {
            ImageView posterView = createPosterImageView(posterURL);
            if (posterView != null) {
                card.getChildren().add(posterView);
            } else {
                // Fallback to circle if image fails
                card.getChildren().add(createDefaultAvatar());
            }
        } else {
            // No poster URL - use default circle
            card.getChildren().add(createDefaultAvatar());
        }

        // Movie title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(150);
        titleLabel.setAlignment(Pos.CENTER);

        // Genres
        Label genreLabel = new Label("Genres: " + genres);
        genreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CCCCCC;");
        genreLabel.setWrapText(true);
        genreLabel.setMaxWidth(150);
        genreLabel.setAlignment(Pos.CENTER);

        // Rating
        Label ratingLabel = new Label("Rating: " + rating + "/10");
        ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CCCCCC;");

        card.getChildren().addAll(titleLabel, genreLabel, ratingLabel);

        // Set fixed size
        card.setPrefSize(180, 260);
        card.setMinSize(180, 260);
        card.setMaxSize(180, 260);

        return card;
    }

    /**
     * Create ImageView for movie poster
     */
    private ImageView createPosterImageView(String posterURL) {
        try {
            // Ensure path starts with "/" for classpath resource
            String resourcePath = posterURL.startsWith("/") ? posterURL : "/" + posterURL;

            // Load image from resources
            Image image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)),
                    120, 120, true, true
            );

            // Check if image failed to load
            if (image.isError()) {
                System.err.println("⚠️ Error loading image: " + image.getException());
                return null;
            }

            // Create ImageView and configure it
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("⚠️ Exception loading image: " + e.getMessage());
            return null;
        }
    }


    /**
     * Create default red circle avatar
     */
    private Circle createDefaultAvatar() {
        Circle avatar = new Circle(60);
        avatar.setFill(Color.web("#ff2525"));
        avatar.setStroke(Color.web("#FFFFFF"));
        avatar.setStrokeWidth(2);
        return avatar;
    }

    /**
     * Clear the movie grid
     */
    public void clearMovieGrid() {
        if (movieGridPane != null) {
            movieGridPane.getChildren().clear();
        }
    }

    /**
     * Add a card to the grid at specific position
     */
    public void addCardToGrid(VBox card, int col, int row) {
        if (movieGridPane != null && card != null) {
            movieGridPane.add(card, col, row);
        }
    }

    // ===== PAGINATION METHODS =====

    public void updatePageLabel(int currentPage) {
        if (pageLabel != null) {
            pageLabel.setText(String.valueOf(currentPage));
        }
    }

    public void setPaginationButtons(boolean enablePrevious, boolean enableNext) {
        if (btnPreviousPage != null) {
            btnPreviousPage.setDisable(!enablePrevious);
        }
        if (btnNextPage != null) {
            btnNextPage.setDisable(!enableNext);
        }
    }

    // ===== UI UPDATE METHODS =====

    public void showStatusMessage(String message, boolean isError) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle(isError ?
                    "-fx-text-fill: #ff2525; -fx-font-weight: bold; -fx-background-color: #1A1A1A; -fx-padding: 12; -fx-background-radius: 5;" :
                    "-fx-text-fill: #00CC66; -fx-font-weight: bold; -fx-background-color: #1A1A1A; -fx-padding: 12; -fx-background-radius: 5;");
            statusMessageLabel.setVisible(true);
        }
    }

    public void clearStatusMessage() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
            statusMessageLabel.setVisible(false);
        }
    }

    public void updateMovieDetailsLabel(String details) {
        if (currentMovieDetailsLabel != null) {
            currentMovieDetailsLabel.setText(details);
        }
    }

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

    public boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType buttonYes = new ButtonType("Yes, Delete");
        ButtonType buttonNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        java.util.Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonYes;
    }

    // ===== GETTERS =====

    public StackPane getRootContentStack() { return rootContentStack; }
    public VBox getMovieView() { return movieView; }
    public GridPane getMovieGridPane() { return movieGridPane; }
    public VBox getAddMovieView() { return addMovieView; }
    public VBox getEditMovieView() { return editMovieView; }
    public Button getBtnFilter() { return btnFilter; }
    public Button getBtnEdit() { return btnEdit; }
    public Button getBtnBackToGrid() { return btnBackToGrid; }
    public Button getBtnBackToGridFromEdit() { return btnBackToGridFromEdit; }
    public Button getBtnPreviousPage() { return btnPreviousPage; }
    public Button getBtnNextPage() { return btnNextPage; }
    public Label getPageLabel() { return pageLabel; }
    public TextField getSearchField() { return searchField; }
    public Label getStatusMessageLabel() { return statusMessageLabel; }
    public Label getCurrentMovieDetailsLabel() { return currentMovieDetailsLabel; }
    public TextField getTitleInput() { return titleInput; }
    public ComboBox<String> getGenreComboBox() { return genreComboBox; }
    public TextField getDurationInput() { return durationInput; }
    public ComboBox<Integer> getRatingComboBox() { return ratingComboBox; }
    public TextField getReleaseDateInput() { return releaseDateInput; }
    public ComboBox<String> getStatusComboBox() { return statusComboBox; }
    public TextArea getDescriptionInput() { return descriptionInput; }
    public TextField getPosterUrlInput() { return posterUrlInput; }
    public Button getBtnBrowsePoster() { return btnBrowsePoster; }
    public TextField getDirectorInput() { return directorInput; }
    public TextField getCastInput() { return castInput; }
    public Button getBtnSearch() { return btnSearch; }
    public Button getBtnAddNewMovie() { return btnAddNewMovie; }
    public Button getBtnSaveMovie() { return btnSaveMovie; }
    public Button getBtnUpdateMovie() { return btnUpdateMovie; }
    public Button getBtnDeleteMovie() { return btnDeleteMovie; }
    public Button getBtnClearFields() { return btnClearFields; }
}