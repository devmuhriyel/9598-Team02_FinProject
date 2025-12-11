package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Objects;

/**
 * CustomerMovieView - Handles all JavaFX UI operations
 * Works with CustomerBrowseMovies.fxml (complete version with details view)
 */
public class CustomerMovieView {

    // ===== MOVIE LIST VIEW COMPONENTS =====
    @FXML private VBox movieListView;
    @FXML private TextField searchField;
    @FXML private Label statusMessageLabel;
    @FXML private ScrollPane movieScrollPane;
    @FXML private TilePane movieTilePane;

    // ===== MOVIE DETAILS VIEW COMPONENTS =====
    @FXML private VBox movieDetailsView;
    @FXML private Button btnBackToList;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieGenreLabel;
    @FXML private Label movieRatingLabel;
    @FXML private TextArea movieDescriptionArea;
    @FXML private ImageView moviePosterImageView;
    @FXML private GridPane scheduleGridPane;
    @FXML private Button btnPreviousSchedulePage;
    @FXML private Button btnNextSchedulePage;
    @FXML private Label schedulePageLabel;

    @FXML
    public void initialize() {
        System.out.println("✅ CustomerMovieView initialized via FXML");

        // Initially show movie list view
        if (movieListView != null && movieDetailsView != null) {
            showMovieListView();
        }
    }

    // ===== VIEW SWITCHING =====

    public void showMovieListView() {
        if (movieListView != null) {
            movieListView.setVisible(true);
            movieListView.setManaged(true);
        }
        if (movieDetailsView != null) {
            movieDetailsView.setVisible(false);
            movieDetailsView.setManaged(false);
        }
        System.out.println("📄 Showing movie list view");
    }

    public void showMovieDetailsView(String title, String genres, int rating, String description, String posterURL) {
        if (movieListView != null) {
            movieListView.setVisible(false);
            movieListView.setManaged(false);
        }
        if (movieDetailsView != null) {
            movieDetailsView.setVisible(true);
            movieDetailsView.setManaged(true);
        }

        // Populate movie details
        if (movieTitleLabel != null) {
            movieTitleLabel.setText(title);
        }
        if (movieGenreLabel != null) {
            movieGenreLabel.setText("Genres: " + genres);
        }
        if (movieRatingLabel != null) {
            movieRatingLabel.setText("⭐ " + rating + "/10");
        }
        if (movieDescriptionArea != null) {
            movieDescriptionArea.setText(description != null ? description : "No description available.");
        }

        // Load poster image
        if (moviePosterImageView != null) {
            loadPosterImage(posterURL);
        }

        System.out.println("📋 Showing movie details view for: " + title);
    }

    // ===== MOVIE CARD CREATION =====

    public void createAndAddMovieCard(int movieId, String title, String genres, int rating,
                                      String posterURL, Runnable onClickAction) {
        VBox card = createMovieCard(movieId, title, genres, rating, posterURL, onClickAction);
        if (movieTilePane != null) {
            movieTilePane.getChildren().add(card);
        }
    }

    private VBox createMovieCard(int movieId, String title, String genres, int rating,
                                 String posterURL, Runnable onClickAction) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(250);
        card.setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 8;");

        // Movie poster
        if (posterURL != null && !posterURL.isEmpty()) {
            ImageView posterView = createPosterImageView(posterURL, 180, 180);
            if (posterView != null) {
                card.getChildren().add(posterView);
            } else {
                card.getChildren().add(createDefaultPoster());
            }
        } else {
            card.getChildren().add(createDefaultPoster());
        }

        // Movie info container
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPrefHeight(70);
        infoBox.setPrefWidth(180);
        infoBox.setPadding(new Insets(5, 10, 0, 10));

        // Movie title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);

        // Movie genre and rating
        Label genreLabel = new Label(genres + " | ⭐ " + rating + "/10");
        genreLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px;");
        genreLabel.setWrapText(true);
        genreLabel.setMaxWidth(160);

        infoBox.getChildren().addAll(titleLabel, genreLabel);
        card.getChildren().add(infoBox);

        // Add click handler
        if (onClickAction != null) {
            card.setOnMouseClicked(e -> onClickAction.run());
        }

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3D3D3D; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 8;"));

        return card;
    }

    private Label createDefaultPoster() {
        Label posterLabel = new Label("POSTER");
        posterLabel.setAlignment(Pos.CENTER);
        posterLabel.setPrefHeight(180);
        posterLabel.setPrefWidth(180);
        posterLabel.setStyle("-fx-background-color: #444444; -fx-background-radius: 8 8 0 0; -fx-text-fill: white;");
        posterLabel.setFont(new javafx.scene.text.Font(18));
        return posterLabel;
    }

    // ===== SCHEDULE CARD CREATION =====

    public void createAndAddScheduleCard(int scheduleId, String date, String time,
                                         long availableSeats, long totalSeats, Runnable onClickAction) {
        VBox card = createScheduleCard(scheduleId, date, time, availableSeats, totalSeats, onClickAction);

        if (scheduleGridPane != null) {
            int currentSize = scheduleGridPane.getChildren().size();
            int row = currentSize / 2;  // 2 columns
            int col = currentSize % 2;
            scheduleGridPane.add(card, col, row);
        }
    }

    private VBox createScheduleCard(int scheduleId, String date, String time,
                                    long availableSeats, long totalSeats, Runnable onClickAction) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefSize(280, 120);
        card.setStyle("-fx-background-color: #2D2D2D; -fx-border-color: #00CC66; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Date and Time
        Label dateTimeLabel = new Label(date + " at " + time);
        dateTimeLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Availability
        Label availabilityLabel = new Label(availableSeats + " / " + totalSeats + " seats available");
        availabilityLabel.setStyle("-fx-text-fill: #00CC66; -fx-font-size: 13px;");

        // Select button
        Button selectButton = new Button("Select Seats");
        selectButton.setStyle("-fx-background-color: #00CC66; -fx-text-fill: #FFFFFF; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");

        if (onClickAction != null) {
            selectButton.setOnAction(e -> onClickAction.run());
        }

        card.getChildren().addAll(dateTimeLabel, availabilityLabel, selectButton);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3D3D3D; -fx-border-color: #00CC66; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2D2D2D; -fx-border-color: #00CC66; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"));

        return card;
    }

    public void clearScheduleGrid() {
        if (scheduleGridPane != null) {
            scheduleGridPane.getChildren().clear();
        }
    }

    // ===== PAGINATION =====

    public void updateSchedulePageLabel(int currentPage) {
        if (schedulePageLabel != null) {
            schedulePageLabel.setText(String.valueOf(currentPage));
        }
    }

    public void setSchedulePaginationButtons(boolean enablePrevious, boolean enableNext) {
        if (btnPreviousSchedulePage != null) {
            btnPreviousSchedulePage.setDisable(!enablePrevious);
        }
        if (btnNextSchedulePage != null) {
            btnNextSchedulePage.setDisable(!enableNext);
        }
    }

    // ===== BUTTON ACTIONS =====

    public void setBackButtonAction(Runnable action) {
        if (btnBackToList != null) {
            btnBackToList.setOnAction(e -> action.run());
        }
    }

    public void setPreviousSchedulePageAction(Runnable action) {
        if (btnPreviousSchedulePage != null) {
            btnPreviousSchedulePage.setOnAction(e -> action.run());
        }
    }

    public void setNextSchedulePageAction(Runnable action) {
        if (btnNextSchedulePage != null) {
            btnNextSchedulePage.setOnAction(e -> action.run());
        }
    }

    // ===== IMAGE HANDLING =====

    private ImageView createPosterImageView(String posterURL, int width, int height) {
        try {
            String resourcePath = posterURL.startsWith("/") ? posterURL : "/" + posterURL;

            Image image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)),
                    width, height, true, true
            );

            if (image.isError()) {
                System.err.println("⚠️ Error loading image: " + image.getException());
                return null;
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("⚠️ Exception loading image: " + e.getMessage());
            return null;
        }
    }

    private void loadPosterImage(String posterURL) {
        if (posterURL == null || posterURL.isEmpty()) {
            if (moviePosterImageView != null) {
                moviePosterImageView.setImage(null);
            }
            return;
        }

        try {
            String resourcePath = posterURL.startsWith("/") ? posterURL : "/" + posterURL;

            Image image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)),
                    300, 400, true, true
            );

            if (!image.isError() && moviePosterImageView != null) {
                moviePosterImageView.setImage(image);
            } else {
                System.err.println("⚠️ Error loading poster: " + (image.isError() ? image.getException() : "ImageView is null"));
                if (moviePosterImageView != null) {
                    moviePosterImageView.setImage(null);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Exception loading poster: " + e.getMessage());
            if (moviePosterImageView != null) {
                moviePosterImageView.setImage(null);
            }
        }
    }

    // ===== SEARCH FIELD LISTENER =====

    public void addSearchFieldListener(ChangeListener<String> listener) {
        if (searchField != null) {
            searchField.textProperty().addListener(listener);
        }
    }

    // ===== UI UPDATE METHODS =====

    public void showStatusMessage(String message, boolean isError) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle(isError ?
                    "-fx-text-fill: #ff2525; -fx-font-weight: bold;" :
                    "-fx-text-fill: #00CC66; -fx-font-weight: bold;");
            statusMessageLabel.setVisible(true);
        }
    }

    public void clearStatusMessage() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
            statusMessageLabel.setVisible(false);
        }
    }

    public void clearMovieCards() {
        if (movieTilePane != null) {
            movieTilePane.getChildren().clear();
        }
    }

    // ===== GETTERS =====

    public VBox getMovieListView() {
        return movieListView;
    }

    public VBox getMovieDetailsView() {
        return movieDetailsView;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Label getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public TilePane getMovieTilePane() {
        return movieTilePane;
    }

    public ScrollPane getMovieScrollPane() {
        return movieScrollPane;
    }
}