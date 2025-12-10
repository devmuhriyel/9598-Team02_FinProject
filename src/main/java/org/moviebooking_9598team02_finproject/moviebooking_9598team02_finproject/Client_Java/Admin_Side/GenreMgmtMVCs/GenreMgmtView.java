package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;

/**
 * GenreMgmtView - Pure UI for Genre Management
 * Handles all JavaFX-specific UI logic
 */
public class GenreMgmtView {

    // ===== TABLE =====
    @FXML
    private TableView<Genre> genreTable;
    @FXML
    private TableColumn<Genre, Long> idColumn;
    @FXML
    private TableColumn<Genre, String> nameColumn;
    @FXML
    private TableColumn<Genre, String> descriptionColumn;

    // ===== FORM FIELDS =====
    @FXML
    private TextField genreNameField;
    @FXML
    private TextArea genreDescriptionField;

    // ===== BUTTONS =====
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

    // ===== MESSAGES =====
    @FXML
    private Text messageText;

    // ===== VARIABLES =====
    private ObservableList<Genre> genreList;

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {
        // Use SimpleProperty wrappers for CORBA objects
        idColumn.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().genreId).asObject());

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().genreName));

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().genreDescription != null ? cellData.getValue().genreDescription : ""));

        genreTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        genreList = FXCollections.observableArrayList();
        genreTable.setItems(genreList);

        // When a row is selected, populate the form fields
        genreTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFields(newValue);
                    }
                }
        );
    }

    // ===== DATA DISPLAY =====
    public void displayGenreList(Genre[] genres) {
        System.out.println("=== View Displaying Genres ===");
        genreList.clear();

        if (genres != null) {
            System.out.println("Received " + genres.length + " genres");
            genreList.addAll(genres);
            System.out.println("Added to observable list. List size now: " + genreList.size());
        } else {
            System.out.println("Received NULL genres array!");
        }

        System.out.println("Table items size: " + genreTable.getItems().size());
    }

    public void populateFields(Genre genre) {
        if (genre == null) return;

        genreNameField.setText(genre.genreName != null ? genre.genreName : "");
        genreDescriptionField.setText(genre.genreDescription != null ? genre.genreDescription : "");
    }

    // ===== MESSAGE HELPERS =====
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

    // ===== FIELD HELPERS =====
    public void clearFields() {
        genreNameField.clear();
        genreDescriptionField.clear();
    }

    // ===== GETTERS =====
    public TableView<Genre> getGenreTable() {
        return genreTable;
    }

    public TextField getGenreNameField() {
        return genreNameField;
    }

    public TextArea getGenreDescriptionField() {
        return genreDescriptionField;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Text getMessageText() {
        return messageText;
    }

    public ObservableList<Genre> getGenreList() {
        return genreList;
    }
}
