package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

public class GenreMgmtView {

    // ===== VARIABLES =====
    @FXML
    private TableView<org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem> genreTable;
    @FXML
    private TableColumn<org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem, Integer> idColumn;
    @FXML
    private TableColumn<org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem, String> nameColumn;
    @FXML
    private TableColumn<org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem, String> descriptionColumn;
    @FXML
    private TextField genreNameField;
    @FXML
    private TextArea genreDescriptionField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Text messageText;

    private org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtController controller;
    private ObservableList<org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem> genreList;

    // ===== INITIALIZATION =====
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("genreId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("genreDescription"));

        // Make table rows selectable
        genreTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Initialize observable list
        genreList = FXCollections.observableArrayList();
        genreTable.setItems(genreList);

        // Set up table selection listener
        genreTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFields(newValue);
                    }
                }
        );
    }

    // ===== CONTROLLER SETUP =====
    public void setController(org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtController controller) {
        this.controller = controller;
        setupButtonHandlers();
        loadGenres();
    }

    private void setupButtonHandlers() {
        // Add button
        addButton.setOnAction(event -> handleAddGenre());

        // Update button
        updateButton.setOnAction(event -> handleUpdateGenre());

        // Delete button
        deleteButton.setOnAction(event -> handleDeleteGenre());

        // Clear button
        clearButton.setOnAction(event -> handleClearFields());

        // Refresh button
        refreshButton.setOnAction(event -> loadGenres());
    }

    // ===== BUTTON HANDLERS =====
    private void handleAddGenre() {
        if (controller == null) return;

        String genreName = getGenreName();
        String genreDescription = getGenreDescription();

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreOperationResult result = controller.addGenre(genreName, genreDescription);
        showMessage(result.getMessage());

        if (result.isSuccess()) {
            clearFields();
            loadGenres();
        }
    }

    private void handleUpdateGenre() {
        if (controller == null) return;

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem selectedGenre = getSelectedGenre();
        if (selectedGenre == null) {
            showMessage("Error: Please select a genre to update");
            return;
        }

        String genreName = getGenreName();
        String genreDescription = getGenreDescription();

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreOperationResult result = controller.updateGenre(
                selectedGenre.getGenreId(), genreName, genreDescription);
        showMessage(result.getMessage());

        if (result.isSuccess()) {
            clearFields();
            loadGenres();
        }
    }

    private void handleDeleteGenre() {
        if (controller == null) return;

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem selectedGenre = getSelectedGenre();
        if (selectedGenre == null) {
            showMessage("Error: Please select a genre to delete");
            return;
        }

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreOperationResult result = controller.deleteGenre(selectedGenre.getGenreId());
        showMessage(result.getMessage());

        if (result.isSuccess()) {
            clearFields();
            loadGenres();
        }
    }

    private void handleClearFields() {
        clearFields();
        genreTable.getSelectionModel().clearSelection();
        clearMessage();
    }

    // ===== METHODS =====
    public void clearFields() {
        genreNameField.clear();
        genreDescriptionField.clear();
    }

    public void showMessage(String message) {
        if (messageText != null) {
            messageText.setText(message);
            messageText.setVisible(true);
        }
    }

    public void clearMessage() {
        if (messageText != null) {
            messageText.setText("");
            messageText.setVisible(false);
        }
    }

    public void populateFields(org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem genre) {
        if (genre != null) {
            genreNameField.setText(genre.getGenreName());
            genreDescriptionField.setText(genre.getGenreDescription());
        }
    }

    public void loadGenres() {
        if (controller == null) return;

        org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreLoadResult result = controller.loadGenres();

        if (result.isSuccess() && result.getGenres() != null) {
            genreList.clear();
            genreList.addAll(result.getGenres());
            clearMessage();
        } else {
            showMessage(result.getErrorMessage());
        }
    }

    // ===== GETTERS =====
    public String getGenreName() {
        return genreNameField.getText();
    }

    public String getGenreDescription() {
        return genreDescriptionField.getText();
    }

    public org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.GenreMgmtModel.GenreTableItem getSelectedGenre() {
        return genreTable.getSelectionModel().getSelectedItem();
    }
}

