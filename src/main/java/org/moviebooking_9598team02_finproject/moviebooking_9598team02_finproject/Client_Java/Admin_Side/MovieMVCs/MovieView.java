package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MovieView - View component for Movie Management
 * Fixed issues:
 * - Image browsing now handled in view
 * - Rating dropdown properly populated
 * - Genre checkboxes working
 * - No JavaFX imports in controller
 */
public class MovieView {

    // ===== ROOT CONTAINERS =====
    @FXML private StackPane rootContentStack;
    @FXML private VBox movieView;

    // ===== GRID LAYOUT COMPONENTS =====
    @FXML private GridPane movieGridPane;
    @FXML private VBox addMovieView;
    @FXML private VBox editMovieView;
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

    // ===== INPUT FIELDS - ADD VIEW =====
    @FXML private TextField titleInput;
    @FXML private ListView<String> genreListView;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextArea descriptionInput;
    @FXML private Button btnBrowsePoster;
    @FXML private Label selectedImageLabel;

    // ===== INPUT FIELDS - EDIT VIEW =====
    @FXML private TextField titleInputEdit;
    @FXML private ListView<String> genreListViewEdit;
    @FXML private ComboBox<Integer> ratingComboBoxEdit;
    @FXML private TextArea descriptionInputEdit;
    @FXML private Button btnBrowsePosterEdit;
    @FXML private Label selectedImageLabelEdit;

    // ===== SCHEDULE FIELDS =====
    @FXML private DatePicker scheduleDatePicker;
    @FXML private TextField scheduleTimeField;

    // ===== ACTION BUTTONS =====
    @FXML private Button btnAddNewMovie;
    @FXML private Button btnAddMovie;
    @FXML private Button btnUpdateMovie;
    @FXML private Button btnDeleteMovie;
    @FXML private Button btnClearFields;
    @FXML private Button btnManageSchedules;

    // Genre tracking with checkbox state
    private final Map<String, BooleanProperty> genreCheckMap = new LinkedHashMap<>();
    private final ObservableList<String> genreNames = FXCollections.observableArrayList();

    // Genre tracking for EDIT view
    private final Map<String, BooleanProperty> genreCheckMapEdit = new LinkedHashMap<>();
    private final ObservableList<String> genreNamesEdit = FXCollections.observableArrayList();

    // Image selection tracking
    private String selectedImagePath = null;

    // ===== INITIALIZATION =====

    @FXML
    public void initialize() {
        System.out.println("🎬 MovieView.initialize() called");
        System.out.println("   ADD VIEW:");
        System.out.println("   - ratingComboBox: " + (ratingComboBox != null ? "OK" : "NULL"));
        System.out.println("   - genreListView: " + (genreListView != null ? "OK" : "NULL"));
        System.out.println("   - btnBrowsePoster: " + (btnBrowsePoster != null ? "OK" : "NULL"));
        System.out.println("   EDIT VIEW:");
        System.out.println("   - ratingComboBoxEdit: " + (ratingComboBoxEdit != null ? "OK" : "NULL"));
        System.out.println("   - genreListViewEdit: " + (genreListViewEdit != null ? "OK" : "NULL"));
        System.out.println("   - btnBrowsePosterEdit: " + (btnBrowsePosterEdit != null ? "OK" : "NULL"));

        // Setup ADD view
        setupRatingComboBox();
        setupGenreListView();
        setupImageBrowsing();

        // Setup EDIT view
        setupRatingComboBoxEdit();
        setupGenreListViewEdit();
        setupImageBrowsingEdit();

        System.out.println("✅ MovieView initialization complete");
    }

    /**
     * Setup rating dropdown (1-10)
     */
    private void setupRatingComboBox() {
        if (ratingComboBox != null) {
            ratingComboBox.getItems().clear();
            List<Integer> ratings = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                ratings.add(i);
            }
            ratingComboBox.getItems().addAll(ratings);
            ratingComboBox.setPromptText("Select rating");
            System.out.println("✅ Rating ComboBox populated with 1-10");
        } else {
            System.err.println("❌ ERROR: ratingComboBox is NULL!");
        }
    }

    /**
     * Setup ListView to display genres with checkboxes
     */
    private void setupGenreListView() {
        if (genreListView != null) {
            genreListView.setItems(genreNames);
            genreListView.setPlaceholder(new Label("Loading genres..."));

            // Create custom cell factory with checkboxes
            genreListView.setCellFactory(param -> new ListCell<String>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    // Style the checkbox for white text
                    checkBox.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        checkBox.setText(item);

                        // Get the property for this specific item in ADD view
                        BooleanProperty selectedProperty = genreCheckMap.get(item);
                        if (selectedProperty != null) {
                            // Remove any existing listeners/bindings first
                            checkBox.setOnAction(null);
                            checkBox.setSelected(selectedProperty.get());

                            // Add action listener instead of bidirectional binding
                            checkBox.setOnAction(e -> {
                                selectedProperty.set(checkBox.isSelected());
                                System.out.println("ADD: " + item + " = " + checkBox.isSelected());
                            });
                        }

                        setGraphic(checkBox);
                    }
                }
            });

            System.out.println("✅ Genre ListView configured with checkboxes");
        } else {
            System.err.println("❌ ERROR: genreListView is NULL!");
        }
    }

    /**
     * Setup image browsing functionality (stays in View, no controller dependency)
     */
    private void setupImageBrowsing() {
        if (btnBrowsePoster != null) {
            btnBrowsePoster.setOnAction(event -> handleImageBrowsing());
        }
    }

    /**
     * Setup image browsing for EDIT view
     */
    private void setupImageBrowsingEdit() {
        if (btnBrowsePosterEdit != null) {
            btnBrowsePosterEdit.setOnAction(event -> handleImageBrowsing());
        }
    }

    /**
     * Setup rating dropdown for EDIT view (1-10)
     */
    private void setupRatingComboBoxEdit() {
        if (ratingComboBoxEdit != null) {
            ratingComboBoxEdit.getItems().clear();
            List<Integer> ratings = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                ratings.add(i);
            }
            ratingComboBoxEdit.getItems().addAll(ratings);
            ratingComboBoxEdit.setPromptText("Select rating");
            System.out.println("✅ Rating ComboBoxEdit populated with 1-10");
        } else {
            System.err.println("❌ ERROR: ratingComboBoxEdit is NULL!");
        }
    }

    /**
     * Setup ListView for EDIT view to display genres with checkboxes
     */
    private void setupGenreListViewEdit() {
        if (genreListViewEdit != null) {
            genreListViewEdit.setItems(genreNamesEdit);
            genreListViewEdit.setPlaceholder(new Label("Loading genres..."));

            // Create custom cell factory with checkboxes
            genreListViewEdit.setCellFactory(param -> new ListCell<String>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    // Style the checkbox for white text
                    checkBox.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        checkBox.setText(item);

                        // Get the property for this specific item in EDIT view
                        BooleanProperty selectedProperty = genreCheckMapEdit.get(item);
                        if (selectedProperty != null) {
                            // Remove any existing listeners/bindings first
                            checkBox.setOnAction(null);
                            checkBox.setSelected(selectedProperty.get());

                            // Add action listener instead of bidirectional binding
                            checkBox.setOnAction(e -> {
                                selectedProperty.set(checkBox.isSelected());
                                System.out.println("EDIT: " + item + " = " + checkBox.isSelected());
                            });
                        }

                        setGraphic(checkBox);
                    }
                }
            });

            System.out.println("✅ Genre ListViewEdit configured with checkboxes");
        } else {
            System.err.println("❌ ERROR: genreListViewEdit is NULL!");
        }
    }

    private void handleImageBrowsing() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            String labelText = "Selected: " + selectedFile.getName();
            updateSelectedImageLabel(labelText);
            updateSelectedImageLabelEdit(labelText);
            System.out.println("🖼️ Image selected: " + selectedImagePath);
        }
    }

    // ===== GENRE MANAGEMENT =====

    /**
     * Re-initialize rating ComboBox (public method for controller)
     */
    public void refreshRatingComboBox() {
        setupRatingComboBox();
    }

    /**
     * Populate the genre ListView with available genres (for both ADD and EDIT views)
     */
    public void populateGenreList(List<String> genres) {
        System.out.println("📋 Populating genre list with " + genres.size() + " genres");

        // ADD view
        genreNames.clear();
        genreCheckMap.clear();

        // EDIT view
        genreNamesEdit.clear();
        genreCheckMapEdit.clear();

        for (String genre : genres) {
            // ADD view
            genreNames.add(genre);
            genreCheckMap.put(genre, new SimpleBooleanProperty(false));

            // EDIT view
            genreNamesEdit.add(genre);
            genreCheckMapEdit.put(genre, new SimpleBooleanProperty(false));
        }

        genreListView.refresh();
        genreListViewEdit.refresh();
        System.out.println("✅ Genre list populated for both views: " + genres);
    }

    /**
     * Get list of selected genre names (checks both ADD and EDIT views)
     */
    public List<String> getSelectedGenres() {
        // Check which view is visible
        if (editMovieView != null && editMovieView.isVisible()) {
            // Use EDIT view genres
            List<String> selected = genreCheckMapEdit.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            System.out.println("📋 Selected genres (EDIT): " + selected);
            return selected;
        } else {
            // Use ADD view genres
            List<String> selected = genreCheckMap.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            System.out.println("📋 Selected genres (ADD): " + selected);
            return selected;
        }
    }

    /**
     * Select specific genres (for edit mode - sets both views)
     */
    public void selectGenres(List<String> genresToSelect) {
        System.out.println("📘 Selecting genres: " + genresToSelect);

        // SET in EDIT view
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMapEdit.entrySet()) {
            boolean shouldSelect = genresToSelect.contains(entry.getKey());
            entry.getValue().set(shouldSelect);
        }

        // Also set in ADD view (for consistency)
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMap.entrySet()) {
            boolean shouldSelect = genresToSelect.contains(entry.getKey());
            entry.getValue().set(shouldSelect);
        }

        // Force refresh to update UI
        if (genreListView != null) {
            genreListView.refresh();
        }
        if (genreListViewEdit != null) {
            genreListViewEdit.refresh();
        }
    }

    /**
     * Clear all genre selections (both views)
     */
    public void clearGenreSelection() {
        System.out.println("🧹 Clearing genre selections");

        // ADD view
        for (BooleanProperty property : genreCheckMap.values()) {
            property.set(false);
        }

        // EDIT view
        for (BooleanProperty property : genreCheckMapEdit.values()) {
            property.set(false);
        }

        // Force refresh to update UI
        if (genreListView != null) {
            genreListView.refresh();
        }
        if (genreListViewEdit != null) {
            genreListViewEdit.refresh();
        }
    }

    // ===== IMAGE MANAGEMENT =====

    /**
     * Get selected image path
     */
    public String getSelectedImagePath() {
        return selectedImagePath;
    }

    /**
     * Clear selected image
     */
    public void clearSelectedImage() {
        selectedImagePath = null;
        clearSelectedImageLabel();
    }

    // ===== VIEW SWITCHING METHODS =====

    public void showGridView() {
        movieView.setVisible(true);
        movieView.setManaged(true);
        addMovieView.setVisible(false);
        addMovieView.setManaged(false);
        editMovieView.setVisible(false);
        editMovieView.setManaged(false);
        System.out.println("📊 Showing Grid View");
    }

    public void showAddView() {
        movieView.setVisible(false);
        movieView.setManaged(false);
        addMovieView.setVisible(true);
        addMovieView.setManaged(true);
        editMovieView.setVisible(false);
        editMovieView.setManaged(false);
        System.out.println("➕ Showing Add View");
    }

    public void showEditView() {
        movieView.setVisible(false);
        movieView.setManaged(false);
        addMovieView.setVisible(false);
        addMovieView.setManaged(false);
        editMovieView.setVisible(true);
        editMovieView.setManaged(true);
        System.out.println("✏️ Showing Edit View");
    }

    // ===== MOVIE CARD CREATION =====

    /**
     * Creates a movie card with poster image covering the whole card
     * and gradient overlay from middle to bottom
     */
    public VBox createMovieCard(String title, String genres, int rating, String posterURL, Runnable onClickAction) {
        // Main card container
        VBox card = new VBox();
        card.setAlignment(Pos.BOTTOM_CENTER);
        card.setPrefSize(180, 280);
        card.setMinSize(180, 280);
        card.setMaxSize(180, 280);
        card.setStyle(
                "-fx-border-color: #ff2525; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;");

        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-border-color: #ff2525; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #ff2525, 10, 0.5, 0, 0);"));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-border-color: #ff2525; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"));

        if (onClickAction != null) {
            card.setOnMouseClicked(e -> onClickAction.run());
        }

        // Create StackPane for poster + gradient overlay
        StackPane posterStack = new StackPane();
        posterStack.setPrefSize(180, 280);
        posterStack.setMinSize(180, 280);
        posterStack.setMaxSize(180, 280);

        // Background poster image
        if (posterURL != null && !posterURL.isEmpty()) {
            ImageView posterView = createPosterImageView(posterURL);
            if (posterView != null) {
                posterView.setFitWidth(180);
                posterView.setFitHeight(280);
                posterView.setPreserveRatio(false);
                posterView.setStyle("-fx-background-radius: 5;");
                posterStack.getChildren().add(posterView);
            } else {
                posterStack.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5;");
                Circle avatar = createDefaultAvatar();
                posterStack.getChildren().add(avatar);
            }
        } else {
            posterStack.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5;");
            Circle avatar = createDefaultAvatar();
            posterStack.getChildren().add(avatar);
        }

        // Gradient overlay
        Region gradientOverlay = new Region();
        gradientOverlay.setPrefSize(180, 280);
        gradientOverlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, transparent 40%, rgba(0,0,0,0.7) 70%, #000000 100%);" +
                        "-fx-background-radius: 5;");
        posterStack.getChildren().add(gradientOverlay);

        // Text content on top of gradient
        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.BOTTOM_LEFT);
        textBox.setPadding(new Insets(10));
        textBox.setMaxWidth(180);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);

        Label genreLabel = new Label("Genres: " + genres);
        genreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CCCCCC;");
        genreLabel.setWrapText(true);
        genreLabel.setMaxWidth(160);

        Label ratingLabel = new Label("⭐ " + rating + "/10");
        ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");

        textBox.getChildren().addAll(titleLabel, genreLabel, ratingLabel);
        posterStack.getChildren().add(textBox);
        StackPane.setAlignment(textBox, Pos.BOTTOM_LEFT);

        card.getChildren().add(posterStack);

        return card;
    }

    private ImageView createPosterImageView(String posterURL) {
        try {
            String resourcePath = posterURL.startsWith("/") ? posterURL : "/" + posterURL;

            Image image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)),
                    180, 280, false, true
            );

            if (image.isError()) {
                System.err.println("⚠️ Error loading image: " + image.getException());
                return null;
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(280);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("⚠️ Exception loading image: " + e.getMessage());
            return null;
        }
    }

    private Circle createDefaultAvatar() {
        Circle avatar = new Circle(60);
        avatar.setFill(Color.web("#ff2525"));
        avatar.setStroke(Color.web("#FFFFFF"));
        avatar.setStrokeWidth(2);
        return avatar;
    }

    public void clearMovieGrid() {
        if (movieGridPane != null) {
            movieGridPane.getChildren().clear();
        }
    }

    public void addCardToGrid(Object card, int col, int row) {
        if (movieGridPane != null && card != null && card instanceof VBox) {
            movieGridPane.add((VBox) card, col, row);
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

    public void updateSelectedImageLabel(String text) {
        if (selectedImageLabel != null) {
            selectedImageLabel.setText(text);
            selectedImageLabel.setVisible(true);
        }
    }

    public void updateSelectedImageLabelEdit(String text) {
        if (selectedImageLabelEdit != null) {
            selectedImageLabelEdit.setText(text);
            selectedImageLabelEdit.setVisible(true);
        }
    }

    public void clearSelectedImageLabel() {
        if (selectedImageLabel != null) {
            selectedImageLabel.setText("No image selected");
            selectedImageLabel.setVisible(true);
        }
        if (selectedImageLabelEdit != null) {
            selectedImageLabelEdit.setText("No image selected");
            selectedImageLabelEdit.setVisible(true);
        }
    }

    public void clearAllFields() {
        // ADD view
        if (titleInput != null) titleInput.clear();
        if (ratingComboBox != null) ratingComboBox.getSelectionModel().clearSelection();
        if (descriptionInput != null) descriptionInput.clear();

        // EDIT view
        if (titleInputEdit != null) titleInputEdit.clear();
        if (ratingComboBoxEdit != null) ratingComboBoxEdit.getSelectionModel().clearSelection();
        if (descriptionInputEdit != null) descriptionInputEdit.clear();

        clearGenreSelection();
        clearSelectedImageLabel();
        clearSelectedImage();

        // Clear schedule fields
        if (scheduleTimeField != null) scheduleTimeField.clear();
    }

    public boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType buttonYes = new ButtonType("Yes, Delete");
        ButtonType buttonNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonYes;
    }

    public void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showInfoAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.INFORMATION);
    }

    public void showWarningAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.WARNING);
    }

    public void showErrorAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.ERROR);
    }

    // ===== GETTERS =====

    public StackPane getRootContentStack() { return rootContentStack; }
    public VBox getMovieView() { return movieView; }
    public GridPane getMovieGridPane() { return movieGridPane; }
    public VBox getAddMovieView() { return addMovieView; }
    public VBox getEditMovieView() { return editMovieView; }
    public Button getBtnBackToGrid() { return btnBackToGrid; }
    public Button getBtnBackToGridFromEdit() { return btnBackToGridFromEdit; }
    public Button getBtnPreviousPage() { return btnPreviousPage; }
    public Button getBtnNextPage() { return btnNextPage; }
    public Label getPageLabel() { return pageLabel; }
    public TextField getSearchField() { return searchField; }
    public Label getStatusMessageLabel() { return statusMessageLabel; }
    public Label getCurrentMovieDetailsLabel() { return currentMovieDetailsLabel; }
    public TextField getTitleInput() { return titleInput; }
    public ListView<String> getGenreListView() { return genreListView; }
    public ComboBox<Integer> getRatingComboBox() { return ratingComboBox; }
    public TextArea getDescriptionInput() { return descriptionInput; }
    public Button getBtnBrowsePoster() { return btnBrowsePoster; }
    public Label getSelectedImageLabel() { return selectedImageLabel; }
    public Button getBtnAddNewMovie() { return btnAddNewMovie; }
    public Button getBtnAddMovie() { return btnAddMovie; }
    public Button getBtnUpdateMovie() { return btnUpdateMovie; }
    public Button getBtnDeleteMovie() { return btnDeleteMovie; }
    public Button getBtnClearFields() { return btnClearFields; }
    public Button getBtnManageSchedules() { return btnManageSchedules; }

    // Schedule field getters
    public DatePicker getScheduleDatePicker() { return scheduleDatePicker; }
    public TextField getScheduleTimeField() { return scheduleTimeField; }

    // EDIT view field getters
    public TextField getTitleInputEdit() { return titleInputEdit; }
    public ComboBox<Integer> getRatingComboBoxEdit() { return ratingComboBoxEdit; }
    public TextArea getDescriptionInputEdit() { return descriptionInputEdit; }
    public Label getSelectedImageLabelEdit() { return selectedImageLabelEdit; }
}