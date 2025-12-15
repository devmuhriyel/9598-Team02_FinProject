package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MovieView - Complete View with Schedule Management UI
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
    @FXML private VBox scheduleListContainer;
    @FXML private Button btnAddNewSchedule;
    @FXML private DatePicker scheduleDatePickerEdit;
    @FXML private ComboBox<String> scheduleTimeComboBoxEdit;

    // ===== ACTION BUTTONS =====
    @FXML private Button btnAddNewMovie;
    @FXML private Button btnAddMovie;
    @FXML private Button btnUpdateMovie;
    @FXML private Button btnDeleteMovie;
    @FXML private Button btnClearFields;

    // ===== COMBO BOXES =====
    @FXML private ComboBox<String> scheduleTimeComboBox;

    // Genre tracking with checkbox state
    private Timeline refreshTimer;
    private final Map<String, BooleanProperty> genreCheckMap = new LinkedHashMap<>();
    private final ObservableList<String> genreNames = FXCollections.observableArrayList();

    // Genre tracking for EDIT view
    private final Map<String, BooleanProperty> genreCheckMapAdd = new HashMap<>();
    private final Map<String, BooleanProperty> genreCheckMapEdit = new HashMap<>();
    private final ObservableList<String> genreNamesEdit = FXCollections.observableArrayList();

    // Image selection tracking
    private String selectedImagePath = null;

    // ===== INITIALIZATION =====

    @FXML
    public void initialize() {
        System.out.println("🎬 MovieView.initialize() called");

        if (genreListView == null) {
            System.err.println("❌ FATAL: genreListView is NULL!");
        } else {
            System.out.println("✅ genreListView is linked correctly.");
        }

        if (scheduleListContainer == null) {
            System.err.println("❌ CRITICAL: scheduleListContainer is NULL!");
        } else {
            System.out.println("✅ scheduleListContainer found");
            scheduleListContainer.setStyle("-fx-background-color: transparent;");
        }

        // 1. Setup UI Components IN ORDER
        setupGenreListView();
        setupGenreListViewEdit();
        setupRatingComboBox();
        setupRatingComboBoxEdit();
        setupDatePicker();          // ✅ Add view date picker
        setupTimeComboBox();        // ✅ Add view time combo
        setupDatePickerEdit();      // ✅ NEW: Edit view date picker
        setupTimeComboBoxEdit();    // ✅ NEW: Edit view time combo
        setupImageBrowsing();
        setupImageBrowsingEdit();

        // 2. Verify setup was successful
        verifyScheduleUIComponents();

        // 3. Default Visibility State
        showGridView();

        System.out.println("✅ MovieView initialization complete");
    }


    public String getScheduleDateValue() {
        if (scheduleDatePicker == null) {
            System.err.println("❌ scheduleDatePicker is NULL");
            return null;
        }

        LocalDate date = scheduleDatePicker.getValue();
        if (date == null) {
            System.err.println("⚠️ DatePicker value is NULL");
            return null;
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("📅 Getting date value: " + dateStr);
        return dateStr;
    }

    public String getScheduleTimeValue() {
        if (scheduleTimeComboBox == null) {
            System.err.println("❌ scheduleTimeComboBox is NULL");
            return null;
        }

        String time = scheduleTimeComboBox.getValue();

        if (time == null || time.isEmpty()) {
            System.err.println("⚠️ TimeComboBox value is NULL or empty");
            System.err.println("   → Items in combo box: " + scheduleTimeComboBox.getItems().size());
            System.err.println("   → Prompt text: " + scheduleTimeComboBox.getPromptText());
        } else {
            System.out.println("🕐 Getting time value: " + time);
        }

        return time;
    }

    public void setScheduleDateToToday() {
        if (scheduleDatePicker != null) {
            scheduleDatePicker.setValue(LocalDate.now());
            System.out.println("📅 Set date to today: " + LocalDate.now());
        } else {
            System.err.println("❌ Cannot set date - scheduleDatePicker is NULL");
        }
    }


    //Setup date picker with default value
    private void setupDatePicker() {
        if (scheduleDatePicker != null) {
            System.out.println("📅 Setting up date picker...");

            // Set default to today
            scheduleDatePicker.setValue(LocalDate.now());

            // Disable past dates
            scheduleDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    }
                }
            });

            System.out.println("✅ Date picker initialized with: " + scheduleDatePicker.getValue());
        } else {
            System.err.println("❌ scheduleDatePicker is NULL! Check FXML fx:id='scheduleDatePicker'");
        }
    }

    private void setupDatePickerEdit() {
        if (scheduleDatePickerEdit != null) {
            System.out.println("📅 Setting up EDIT date picker...");

            // Set default to today
            scheduleDatePickerEdit.setValue(LocalDate.now());

            // Disable past dates
            scheduleDatePickerEdit.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    }
                }
            });

            System.out.println("✅ Edit date picker initialized with: " + scheduleDatePickerEdit.getValue());
        } else {
            System.err.println("❌ scheduleDatePickerEdit is NULL! Check FXML fx:id='scheduleDatePickerEdit'");
        }
    }

    private void setupTimeComboBoxEdit() {
        if (scheduleTimeComboBoxEdit != null) {
            System.out.println("🕐 Setting up EDIT time combo box...");

            // Create time slots list (matching database enum values)
            List<String> timeSlots = new ArrayList<>();
            for (int hour = 8; hour < 24; hour++) {
                timeSlots.add(String.format("%02d:00", hour));
            }

            // Set items to combo box
            scheduleTimeComboBoxEdit.setItems(FXCollections.observableArrayList(timeSlots));
            scheduleTimeComboBoxEdit.setPromptText("Select time");

            System.out.println("✅ Edit time combo box populated with " + timeSlots.size() + " slots");
            System.out.println("   → First: " + timeSlots.get(0) + ", Last: " + timeSlots.get(timeSlots.size()-1));

        } else {
            System.err.println("❌ scheduleTimeComboBoxEdit is NULL! Check FXML fx:id='scheduleTimeComboBoxEdit'");
        }
    }

    private void verifyScheduleUIComponents() {
        System.out.println("🔍 === SCHEDULE UI VERIFICATION ===");

        // Check ADD VIEW components
        System.out.println("📝 ADD VIEW Components:");
        if (scheduleDatePicker != null) {
            System.out.println("✅ scheduleDatePicker: FOUND");
            System.out.println("   → Value: " + scheduleDatePicker.getValue());
        } else {
            System.err.println("❌ scheduleDatePicker: NULL");
        }

        if (scheduleTimeComboBox != null) {
            System.out.println("✅ scheduleTimeComboBox: FOUND");
            System.out.println("   → Items: " + scheduleTimeComboBox.getItems().size());
        } else {
            System.err.println("❌ scheduleTimeComboBox: NULL");
        }

        // Check EDIT VIEW components
        System.out.println("\n✏️ EDIT VIEW Components:");
        if (scheduleDatePickerEdit != null) {
            System.out.println("✅ scheduleDatePickerEdit: FOUND");
            System.out.println("   → Value: " + scheduleDatePickerEdit.getValue());
        } else {
            System.err.println("❌ scheduleDatePickerEdit: NULL");
        }

        if (scheduleTimeComboBoxEdit != null) {
            System.out.println("✅ scheduleTimeComboBoxEdit: FOUND");
            System.out.println("   → Items: " + scheduleTimeComboBoxEdit.getItems().size());
        } else {
            System.err.println("❌ scheduleTimeComboBoxEdit: NULL");
        }

        // Check Container
        if (scheduleListContainer != null) {
            System.out.println("✅ scheduleListContainer: FOUND");
        } else {
            System.err.println("❌ scheduleListContainer: NULL");
        }

        // Check Add Button
        if (btnAddNewSchedule != null) {
            System.out.println("✅ btnAddNewSchedule: FOUND");
        } else {
            System.err.println("❌ btnAddNewSchedule: NULL");
        }

        System.out.println("=====================================");
    }


    private void setupTimeComboBox() {
        if (scheduleTimeComboBox != null) {
            System.out.println("🕐 Setting up time combo box...");

            // Create time slots list
            List<String> timeSlots = new ArrayList<>();
            for (int hour = 8; hour < 24; hour++) {
                timeSlots.add(String.format("%02d:00", hour));
            }

            // Set items to combo box
            scheduleTimeComboBox.setItems(FXCollections.observableArrayList(timeSlots));

            // Set prompt text (don't set a default value, let user choose)
            scheduleTimeComboBox.setPromptText("Select time");

            System.out.println("✅ Time combo box populated with " + timeSlots.size() + " slots");
            System.out.println("   → First: " + timeSlots.get(0) + ", Last: " + timeSlots.get(timeSlots.size()-1));

        } else {
            System.err.println("❌ scheduleTimeComboBox is NULL! Check FXML fx:id='scheduleTimeComboBox'");
        }
    }


    //Method to reset schedule fields to defaults
    public void resetScheduleFields() {
        System.out.println("🔄 Resetting schedule fields...");

        if (scheduleDatePicker != null) {
            scheduleDatePicker.setValue(LocalDate.now());
            System.out.println("  ✅ Date reset to: " + LocalDate.now());
        } else {
            System.err.println("  ❌ scheduleDatePicker is NULL");
        }

        if (scheduleTimeComboBox != null) {
            // Clear selection
            scheduleTimeComboBox.getSelectionModel().clearSelection();

            // Verify items still exist
            if (scheduleTimeComboBox.getItems().isEmpty()) {
                System.err.println("  ⚠️ Time combo box items are empty! Re-populating...");
                setupTimeComboBox();
            } else {
                System.out.println("  ✅ Time selection cleared (still has " +
                        scheduleTimeComboBox.getItems().size() + " items)");
            }
        } else {
            System.err.println("  ❌ scheduleTimeComboBox is NULL");
        }
    }




    private void setupRatingComboBox() {
        if (ratingComboBox != null) {
            ratingComboBox.getItems().clear();
            List<Integer> ratings = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                ratings.add(i);
            }
            ratingComboBox.getItems().addAll(ratings);
            ratingComboBox.setPromptText("Select rating");
        }
    }

    private void setupGenreListView() {
        if (genreListView != null) {
            // 1. Force a Placeholder (so you know if it's empty vs invisible)
            Label placeholder = new Label("No genres loaded");
            placeholder.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
            genreListView.setPlaceholder(placeholder);

            // 2. Define the Cell Factory INLINE (Safest way)
            genreListView.setCellFactory(param -> new ListCell<String>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    // FORCE WHITE TEXT
                    checkBox.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    setTextFill(Color.WHITE); // Ensure cell text is also white
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        // Update Text
                        checkBox.setText(item);

                        // Handle Selection State
                        checkBox.setOnAction(null); // Remove old listener

                        // Check if map has this item
                        if (genreCheckMapAdd.containsKey(item)) {
                            BooleanProperty prop = genreCheckMapAdd.get(item);
                            checkBox.setSelected(prop.get());

                            // Add new listener
                            checkBox.setOnAction(e -> prop.set(checkBox.isSelected()));

                            setGraphic(checkBox);
                        } else {
                            // Fallback if map is missing data (Just show text)
                            setText(item);
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    private void setupImageBrowsing() {
        if (btnBrowsePoster != null) {
            btnBrowsePoster.setOnAction(event -> handleImageBrowsing());
        }
    }

    private void setupImageBrowsingEdit() {
        if (btnBrowsePosterEdit != null) {
            btnBrowsePosterEdit.setOnAction(event -> handleImageBrowsing());
        }
    }


    private void setupRatingComboBoxEdit() {
        if (ratingComboBoxEdit != null) {
            ratingComboBoxEdit.getItems().clear();
            List<Integer> ratings = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                ratings.add(i);
            }
            ratingComboBoxEdit.getItems().addAll(ratings);
            ratingComboBoxEdit.setPromptText("Select rating");
        }
    }

    private void setupGenreListViewEdit() {
        if (genreListViewEdit != null) {
            genreListViewEdit.setItems(genreNamesEdit);
            genreListViewEdit.setPlaceholder(new Label("Loading genres..."));

            genreListViewEdit.setCellFactory(param -> new ListCell<String>() {
                private final CheckBox checkBox = new CheckBox();

                {
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
                        BooleanProperty selectedProperty = genreCheckMapEdit.get(item);
                        if (selectedProperty != null) {
                            checkBox.setOnAction(null);
                            checkBox.setSelected(selectedProperty.get());
                            checkBox.setOnAction(e -> {
                                selectedProperty.set(checkBox.isSelected());
                            });
                        }
                        setGraphic(checkBox);
                    }
                }
            });
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
        }
    }

    // ===== SCHEDULE CARD CREATION =====

    /**
     * Create a schedule card UI element
     */
    public VBox createScheduleCard(int scheduleId, int movieScheduleId, String date, String time,
                                   Runnable onSaveAction, Runnable onDeleteAction) {
        VBox scheduleCard = new VBox(12);
        scheduleCard.setStyle(
                "-fx-background-color: #1A1A1A; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-color: #404040; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5;"
        );

        // Header with Schedule ID
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label scheduleIdLabel = new Label("Schedule #" + scheduleId);
        scheduleIdLabel.setStyle("-fx-text-fill: #ff2525; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label movieScheduleIdLabel = new Label("Time Slot ID: " + movieScheduleId);
        movieScheduleIdLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        header.getChildren().addAll(scheduleIdLabel, spacer, movieScheduleIdLabel);

        // Date Picker
        VBox dateBox = new VBox(5);
        Label dateLabel = new Label("📅 Screening Date");
        dateLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");

        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(250);
        datePicker.setStyle("-fx-background-color: #292929;");

        try {
            LocalDate localDate = LocalDate.parse(date);
            datePicker.setValue(localDate);
        } catch (Exception e) {
            System.err.println("Invalid date format: " + date);
            datePicker.setValue(LocalDate.now());
        }

        dateBox.getChildren().addAll(dateLabel, datePicker);

        // Time ComboBox
        VBox timeBox = new VBox(5);
        Label timeLabel = new Label("🕐 Screening Time");
        timeLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");

        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.setMaxWidth(250);
        timeComboBox.setStyle(
                "-fx-background-color: #292929; " +
                        "-fx-border-color: #404040; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-prompt-text-fill: #888888;"
        );

        // Populate Time Slots
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 8; hour < 24; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
        }

        timeComboBox.setItems(FXCollections.observableArrayList(timeSlots));

        // ✅ SET THE VALUE FROM DATABASE
        timeComboBox.setValue(time);
        System.out.println("✅ Schedule card time set to: " + time);

        timeBox.getChildren().addAll(timeLabel, timeComboBox);

        // ✅ Store references in userData BEFORE creating buttons
        Map<String, Object> userData = new HashMap<>();
        userData.put("scheduleId", scheduleId);
        userData.put("movieScheduleId", movieScheduleId);
        userData.put("datePicker", datePicker);
        userData.put("timeComboBox", timeComboBox);
        scheduleCard.setUserData(userData);

        // Action Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveButton = new Button("✅ Save");
        saveButton.setStyle(
                "-fx-background-color: #00AA00; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );

        saveButton.setOnAction(e -> {
            if (onSaveAction != null) {
                onSaveAction.run();
            }
        });

        Button deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle(
                "-fx-background-color: #8B0000; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );

        if (onDeleteAction != null) {
            deleteButton.setOnAction(e -> onDeleteAction.run());
        }

        buttonBox.getChildren().addAll(saveButton, deleteButton);

        // Add all components to card
        scheduleCard.getChildren().addAll(header, dateBox, timeBox, buttonBox);

        return scheduleCard;
    }
    public Map<String, Object> getScheduleCardData(Object card) {
        if (!(card instanceof VBox)) {
            System.err.println("❌ Card is not a VBox");
            return null;
        }

        VBox scheduleCard = (VBox) card;
        Map<String, Object> userData = (Map<String, Object>) scheduleCard.getUserData();

        if (userData == null) {
            System.err.println("❌ No userData found in card");
            return null;
        }

        // Get the actual controls
        DatePicker datePicker = (DatePicker) userData.get("datePicker");
        ComboBox<String> timeComboBox = (ComboBox<String>) userData.get("timeComboBox");

        if (datePicker == null || timeComboBox == null) {
            System.err.println("❌ Controls not found in userData");
            return null;
        }

        // Get current values from controls
        String newDate = datePicker.getValue() != null ?
                datePicker.getValue().toString() : LocalDate.now().toString();
        String newTime = timeComboBox.getValue() != null ?
                timeComboBox.getValue() : "10:00";

        System.out.println("📅 Getting schedule data:");
        System.out.println("  Schedule ID: " + userData.get("scheduleId"));
        System.out.println("  Date: " + newDate);
        System.out.println("  Time: " + newTime);

        // Create result map
        Map<String, Object> result = new HashMap<>();
        result.put("scheduleId", userData.get("scheduleId"));
        result.put("movieScheduleId", userData.get("movieScheduleId"));
        result.put("newDate", newDate);
        result.put("newTime", newTime);

        return result;
    }
    /**
     * Clear schedule container
     */
    public void clearScheduleContainer() {
        if (scheduleListContainer != null) {
            scheduleListContainer.getChildren().clear();
        }
    }

    /**
     * Add schedule card to container
     */
    /**
     * Add schedule card to container
     */
    public void addScheduleCardToContainer(Object card) {
        if (scheduleListContainer == null) {
            System.err.println("❌ ERROR: scheduleListContainer is NULL!");
            return;
        }

        if (!(card instanceof VBox)) {
            System.err.println("❌ ERROR: card is not a VBox! Type: " + card.getClass().getName());
            return;
        }

        scheduleListContainer.getChildren().add((VBox) card);
        System.out.println("✅ Added schedule card. Total cards: " +
                scheduleListContainer.getChildren().size());

        // Force layout refresh
        scheduleListContainer.layout();
    }

    private static Button getButton(int scheduleId, Runnable onDeleteAction, VBox scheduleCard) {
        Button deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle(
                "-fx-background-color: #8B0000; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );

        if (onDeleteAction != null) {
            deleteButton.setOnAction(e -> {
                // --- FIX START (Replace Map.of() with HashMap) ---
                Map<String, Object> userData = new HashMap<>();
                userData.put("scheduleId", scheduleId);

                scheduleCard.setUserData(userData);
                // --- FIX END ---

                onDeleteAction.run();
            });
        }
        return deleteButton;
    }

    // ===== GENRE MANAGEMENT =====

    public void populateGenreList(List<String> genres) {
        genreNames.clear();
        genreCheckMap.clear();
        genreNamesEdit.clear();
        genreCheckMapEdit.clear();

        for (String genre : genres) {
            genreNames.add(genre);
            genreCheckMap.put(genre, new SimpleBooleanProperty(false));
            genreNamesEdit.add(genre);
            genreCheckMapEdit.put(genre, new SimpleBooleanProperty(false));
        }

        genreListView.refresh();
        genreListViewEdit.refresh();
    }

    public List<String> getSelectedGenres() {
        if (editMovieView != null && editMovieView.isVisible()) {
            return genreCheckMapEdit.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            return genreCheckMap.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
    }

    public void selectGenres(List<String> genresToSelect) {
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMapEdit.entrySet()) {
            entry.getValue().set(genresToSelect.contains(entry.getKey()));
        }
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMap.entrySet()) {
            entry.getValue().set(genresToSelect.contains(entry.getKey()));
        }
        if (genreListView != null) genreListView.refresh();
        if (genreListViewEdit != null) genreListViewEdit.refresh();
    }

    public void clearGenreSelection() {
        for (BooleanProperty property : genreCheckMap.values()) {
            property.set(false);
        }
        for (BooleanProperty property : genreCheckMapEdit.values()) {
            property.set(false);
        }
        if (genreListView != null) genreListView.refresh();
        if (genreListViewEdit != null) genreListViewEdit.refresh();
    }

    public void updateGenreOptions(List<String> genreNames) {
        if (genreNames == null) return;
        System.out.println("📥 Updating Genre List with " + genreNames.size() + " items");

        ObservableList<String> genres = FXCollections.observableArrayList(genreNames);

        // --- CRITICAL STEP: POPULATE THE MAPS ---
        // If we don't do this, the checkboxes will be null/broken

        // A. Reset ADD Map
        genreCheckMapAdd.clear();
        for (String g : genreNames) {
            genreCheckMapAdd.put(g, new SimpleBooleanProperty(false));
        }

        // B. Reset EDIT Map
        genreCheckMapEdit.clear();
        for (String g : genreNames) {
            genreCheckMapEdit.put(g, new SimpleBooleanProperty(false));
        }

        // --- PUSH TO UI ---
        if (genreListView != null) {
            genreListView.setItems(genres);
            genreListView.refresh(); // Force redraw
        }

        if (genreListViewEdit != null) {
            genreListViewEdit.setItems(genres);
            genreListViewEdit.refresh();
        }
    }
    // ===== IMAGE MANAGEMENT =====

    public void clearSelectedImage() {
        selectedImagePath = null;
        clearSelectedImageLabel();
    }

    // ===== VIEW SWITCHING METHODS =====

    // === VIEW SWITCHING METHODS (Add these to MovieView) ===
    public void showGridView() {
        // Show Main View
        if (movieView != null) {
            movieView.setVisible(true);
            movieView.setManaged(true);
            movieView.toFront();
        }
        // Hide Forms (collapse space)
        if (addMovieView != null) {
            addMovieView.setVisible(false);
            addMovieView.setManaged(false);
        }
        if (editMovieView != null) {
            editMovieView.setVisible(false);
            editMovieView.setManaged(false);
        }
    }

    public void showAddView() {
        // Hide Main View
        if (movieView != null) {
            movieView.setVisible(false);
            movieView.setManaged(false);
        }
        // Hide Edit View
        if (editMovieView != null) {
            editMovieView.setVisible(false);
            editMovieView.setManaged(false);
        }
        // Show Add View
        if (addMovieView != null) {
            addMovieView.setVisible(true);
            addMovieView.setManaged(true);
            addMovieView.toFront();
        } else {
            System.err.println("❌ ERROR: addMovieView is NULL. Check FXML fx:id!");
        }
    }

    public void showEditView() {
        // Hide Main View
        if (movieView != null) {
            movieView.setVisible(false);
            movieView.setManaged(false);
        }
        // Hide Add View
        if (addMovieView != null) {
            addMovieView.setVisible(false);
            addMovieView.setManaged(false);
        }
        // Show Edit View
        if (editMovieView != null) {
            editMovieView.setVisible(true);
            editMovieView.setManaged(true);
            editMovieView.toFront();

            // ✅ CRITICAL: Ensure components are ready when view is shown
            ensureScheduleComponentsReady();
            ensureScheduleComponentsReadyEdit();

            // ✅ Reset fields
            resetScheduleFields();
        } else {
            System.err.println("❌ ERROR: editMovieView is NULL!");
        }
    }


    // ===== MOVIE CARD CREATION =====
    public VBox createMovieCard(String title, String genres, int rating, String posterURL, Runnable onClickAction) {
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

        StackPane posterStack = new StackPane();
        posterStack.setPrefSize(180, 280);
        posterStack.setMinSize(180, 280);
        posterStack.setMaxSize(180, 280);

        if (posterURL != null && !posterURL.isEmpty()) {
            ImageView posterView = createPosterImageView(posterURL);
            if (posterView != null) {
                posterView.setFitWidth(180);
                posterView.setFitHeight(280);
                posterView.setPreserveRatio(false);
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

        Region gradientOverlay = new Region();
        gradientOverlay.setPrefSize(180, 280);
        gradientOverlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, transparent 40%, rgba(0,0,0,0.7) 70%, #000000 100%);" +
                        "-fx-background-radius: 5;");
        posterStack.getChildren().add(gradientOverlay);

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
            // Handle embedded data format
            if (posterURL != null && posterURL.contains("|||IMAGE_DATA|||")) {
                String[] parts = posterURL.split("\\|\\|\\|IMAGE_DATA\\|\\|\\|");
                posterURL = parts[0];
            }

            String cleanPath = posterURL.startsWith("/") ? posterURL.substring(1) : posterURL;

            Image image = null;

            // ✅ TRY 1: Load from project root
            java.nio.file.Path projectRootPath = java.nio.file.Paths.get(cleanPath);
            if (java.nio.file.Files.exists(projectRootPath)) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(projectRootPath.toFile())) {
                    image = new Image(fis, 180, 280, false, true);
                    if (!image.isError()) {
                        System.out.println("✅ Loaded from project root: " + cleanPath);
                    }
                }
            }

            // ✅ TRY 2: Load from resources (classpath)
            if (image == null || image.isError()) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(cleanPath);
                if (inputStream != null) {
                    image = new Image(inputStream, 180, 280, false, true);
                    if (!image.isError()) {
                        System.out.println("✅ Loaded from classpath: " + cleanPath);
                    }
                }
            }

            // ✅ TRY 3: Load from resources with file system
            if (image == null || image.isError()) {
                java.nio.file.Path resourcesPath = java.nio.file.Paths.get("src/main/resources/" + cleanPath);
                if (java.nio.file.Files.exists(resourcesPath)) {
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(resourcesPath.toFile())) {
                        image = new Image(fis, 180, 280, false, true);
                        if (!image.isError()) {
                            System.out.println("✅ Loaded from resources folder: " + cleanPath);
                        }
                    }
                }
            }

            // Check if we successfully loaded an image
            if (image == null || image.isError()) {
                System.err.println("⚠️ Image not found in any location: " + cleanPath);
                System.err.println("   Tried:");
                System.err.println("   1. " + java.nio.file.Paths.get(cleanPath).toAbsolutePath());
                System.err.println("   2. Classpath: " + cleanPath);
                System.err.println("   3. " + java.nio.file.Paths.get("src/main/resources/" + cleanPath).toAbsolutePath());
                return null;
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("❌ Exception loading poster: " + posterURL);
            e.printStackTrace();
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
        if (titleInput != null) titleInput.clear();
        if (ratingComboBox != null) ratingComboBox.getSelectionModel().clearSelection();
        if (descriptionInput != null) descriptionInput.clear();
        if (titleInputEdit != null) titleInputEdit.clear();
        if (ratingComboBoxEdit != null) ratingComboBoxEdit.getSelectionModel().clearSelection();
        if (descriptionInputEdit != null) descriptionInputEdit.clear();

        clearGenreSelection();
        clearSelectedImageLabel();
        clearSelectedImage();

        // ✅ Clear schedule fields
        resetScheduleFields();
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

    // ===== HELPER CLASSES =====
    private static class CheckBoxListCell extends ListCell<String> {
        private final CheckBox checkBox = new CheckBox();
        private final Map<String, BooleanProperty> checkMap;

        public CheckBoxListCell(Map<String, BooleanProperty> checkMap) {
            this.checkMap = checkMap;
            checkBox.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                checkBox.setText(item);
                // Unbind previous to prevent errors
                checkBox.selectedProperty().unbind();

                // Get the property for this genre
                BooleanProperty property = checkMap.get(item);
                if (property != null) {
                    // Bind the checkbox to the map state
                    checkBox.selectedProperty().bindBidirectional(property);
                }
                setGraphic(checkBox);
            }
        }
    }

    /**
     * Programmatically check boxes for the Edit screen (when loading a movie).
     */
    public void selectGenresForEdit(List<String> genresToSelect) {
        // 1. Uncheck all first
        for (BooleanProperty prop : genreCheckMapEdit.values()) {
            prop.set(false);
        }

        // 2. Check the matching ones
        if (genresToSelect != null) {
            for (String g : genresToSelect) {
                if (genreCheckMapEdit.containsKey(g)) {
                    genreCheckMapEdit.get(g).set(true);
                }
            }
        }
        // Refresh list to show changes
        if (genreListViewEdit != null) genreListViewEdit.refresh();
    }

    /**
     * Get list of selected genres from ADD screen
     */
    public List<String> getSelectedGenresAdd() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMapAdd.entrySet()) {
            if (entry.getValue().get()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    public List<String> getSelectedGenresEdit() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> entry : genreCheckMapEdit.entrySet()) {
            if (entry.getValue().get()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }
    // ===== TIMERS =====
    /**
     * Starts a timer that runs the given action every 10 seconds.
     */
    public void startAutoRefresh(Runnable action) {
        // Stop existing timer if running
        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        // Create new timer
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            if (action != null) {
                action.run(); // Run the logic passed from Controller
            }
        }));

        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    /**
     * Stops the timer (good for cleanup).
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Public method to show no schedules message
     */
    public void showNoSchedulesMessage() {
        if (scheduleListContainer != null) {
            scheduleListContainer.getChildren().clear();

            VBox placeholder = new VBox(10);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setStyle("-fx-padding: 30;");

            Label icon = new Label("📅");
            icon.setStyle("-fx-font-size: 48px;");

            Label message = new Label("No schedules yet");
            message.setStyle("-fx-text-fill: #888888; -fx-font-size: 16px;");

            Label hint = new Label("Click '+ Add New Schedule' to create one");
            hint.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

            placeholder.getChildren().addAll(icon, message, hint);
            scheduleListContainer.getChildren().add(placeholder);

            System.out.println("📝 Displayed 'No schedules' message");
        }
    }

    /**
     * Ensure schedule container is visible and ready
     */
    public void ensureScheduleContainerVisible() {
        if (scheduleListContainer != null) {
            scheduleListContainer.setVisible(true);
            scheduleListContainer.setManaged(true);

            // Force a layout pass
            scheduleListContainer.layout();

            System.out.println("✅ Schedule container visibility ensured");
        }
    }

    public void ensureScheduleComponentsReady() {
        System.out.println("🔧 Ensuring schedule components are ready...");

        // Re-verify date picker
        if (scheduleDatePicker != null) {
            if (scheduleDatePicker.getValue() == null) {
                scheduleDatePicker.setValue(LocalDate.now());
                System.out.println("  ✅ Reset date picker to: " + LocalDate.now());
            }
        }

        // Re-verify time combo box
        if (scheduleTimeComboBox != null) {
            if (scheduleTimeComboBox.getItems().isEmpty()) {
                System.err.println("  ⚠️ Time combo box is empty! Re-populating...");
                setupTimeComboBox();
            } else {
                System.out.println("  ✅ Time combo box has " +
                        scheduleTimeComboBox.getItems().size() + " items");
            }
        }
    }

    public void ensureScheduleComponentsReadyEdit() {
        System.out.println("🔧 Ensuring EDIT schedule components are ready...");

        // Re-verify date picker
        if (scheduleDatePickerEdit != null) {
            if (scheduleDatePickerEdit.getValue() == null) {
                scheduleDatePickerEdit.setValue(LocalDate.now());
                System.out.println("  ✅ Reset EDIT date picker to: " + LocalDate.now());
            }
        } else {
            System.err.println("  ❌ scheduleDatePickerEdit is NULL");
        }

        // Re-verify time combo box
        if (scheduleTimeComboBoxEdit != null) {
            if (scheduleTimeComboBoxEdit.getItems().isEmpty()) {
                System.err.println("  ⚠️ Edit time combo box is empty! Re-populating...");
                setupTimeComboBoxEdit();
            } else {
                System.out.println("  ✅ Edit time combo box has " +
                        scheduleTimeComboBoxEdit.getItems().size() + " items");
            }
        } else {
            System.err.println("  ❌ scheduleTimeComboBoxEdit is NULL");
        }
    }



    // ===== GETTERS =====
    public String getScheduleDateValueEdit() {
        if (scheduleDatePickerEdit == null) {
            System.err.println("❌ scheduleDatePickerEdit is NULL");
            return null;
        }

        LocalDate date = scheduleDatePickerEdit.getValue();
        if (date == null) {
            System.err.println("⚠️ DatePicker Edit value is NULL");
            return null;
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("📅 Getting EDIT date value: " + dateStr);
        return dateStr;
    }

    public String getScheduleTimeValueEdit() {
        if (scheduleTimeComboBoxEdit == null) {
            System.err.println("❌ scheduleTimeComboBoxEdit is NULL");
            return null;
        }

        String time = scheduleTimeComboBoxEdit.getValue();

        if (time == null || time.isEmpty()) {
            System.err.println("⚠️ TimeComboBox Edit value is NULL or empty");
            System.err.println("   → Items in combo box: " + scheduleTimeComboBoxEdit.getItems().size());
            System.err.println("   → Prompt text: " + scheduleTimeComboBoxEdit.getPromptText());
        } else {
            System.out.println("🕐 Getting EDIT time value: " + time);
        }

        return time;
    }


    public void setScheduleDateToTodayEdit() {
        if (scheduleDatePickerEdit != null) {
            scheduleDatePickerEdit.setValue(LocalDate.now());
            System.out.println("📅 Set EDIT date to today: " + LocalDate.now());
        } else {
            System.err.println("❌ Cannot set date - scheduleDatePickerEdit is NULL");
        }
    }

    public void resetScheduleFieldsEdit() {
        System.out.println("🔄 Resetting EDIT schedule fields...");

        if (scheduleDatePickerEdit != null) {
            scheduleDatePickerEdit.setValue(LocalDate.now());
            System.out.println("  ✅ Edit date reset to: " + LocalDate.now());
        } else {
            System.err.println("  ❌ scheduleDatePickerEdit is NULL");
        }

        if (scheduleTimeComboBoxEdit != null) {
            // Clear selection
            scheduleTimeComboBoxEdit.getSelectionModel().clearSelection();

            // Verify items still exist
            if (scheduleTimeComboBoxEdit.getItems().isEmpty()) {
                System.err.println("  ⚠️ Edit time combo box items are empty! Re-populating...");
                setupTimeComboBoxEdit();
            } else {
                System.out.println("  ✅ Edit time selection cleared (still has " +
                        scheduleTimeComboBoxEdit.getItems().size() + " items)");
            }
        } else {
            System.err.println("  ❌ scheduleTimeComboBoxEdit is NULL");
        }
    }

    public DatePicker getScheduleDatePickerEdit() {
        return scheduleDatePickerEdit;
    }

    public ComboBox<String> getScheduleTimeComboBoxEdit() {
        return scheduleTimeComboBoxEdit;
    }

    public VBox getMovieView() { return movieView; }
    public VBox getAddMovieView() { return addMovieView; }
    public VBox getEditMovieView() { return editMovieView; }
    public Button getBtnBackToGrid() { return btnBackToGrid; }
    public Button getBtnBackToGridFromEdit() { return btnBackToGridFromEdit; }
    public Button getBtnPreviousPage() { return btnPreviousPage; }
    public Button getBtnNextPage() { return btnNextPage; }
    public TextField getSearchField() { return searchField; }
    public TextField getTitleInput() { return titleInput; }
    public ComboBox<Integer> getRatingComboBox() { return ratingComboBox; }
    public TextArea getDescriptionInput() { return descriptionInput; }
    public Button getBtnAddNewMovie() { return btnAddNewMovie; }
    public Button getBtnAddMovie() { return btnAddMovie; }
    public Button getBtnUpdateMovie() { return btnUpdateMovie; }
    public Button getBtnDeleteMovie() { return btnDeleteMovie; }
    public Button getBtnClearFields() { return btnClearFields; }
    public DatePicker getScheduleDatePicker() { return scheduleDatePicker; }
    public TextField getScheduleTimeField() { return scheduleTimeField; }
    public TextField getTitleInputEdit() { return titleInputEdit; }
    public ComboBox<Integer> getRatingComboBoxEdit() { return ratingComboBoxEdit; }
    public TextArea getDescriptionInputEdit() { return descriptionInputEdit; }
    public String getSelectedImagePath() { return selectedImagePath; }
    public Button getBtnAddNewSchedule() { return btnAddNewSchedule; }
    public ComboBox<String> getScheduleTimeComboBox() {
        return scheduleTimeComboBox;
    }
}