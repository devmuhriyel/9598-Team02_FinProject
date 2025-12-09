package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

import java.util.Optional;

/**
 * AdminMgmtView - Pure UI with side-by-side layout (no dialogs)
 * Handles all JavaFX-specific UI logic including dialogs
 */
public class AdminMgmtView {

    // ===== PANELS =====
    @FXML private StackPane listPanel;
    @FXML private StackPane detailPanel;
    @FXML private StackPane createPanel;

    // ===== LIST PANEL =====
    @FXML private TableView<Admin> adminTable;
    @FXML private TableColumn<Admin, Integer> colAdminId;
    @FXML private TableColumn<Admin, String> colUsername;
    @FXML private TableColumn<Admin, String> colEmail;
    @FXML private TableColumn<Admin, String> colFullName;
    @FXML private TableColumn<Admin, String> colPhoneNumber;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button createButton;
    @FXML private Button refreshButton;
    @FXML private Text listMessageText;

    // ===== DETAIL PANEL - Basic Info =====
    @FXML private Label detailAdminIdLabel;
    @FXML private Label detailUsernameLabel;
    @FXML private Label detailEmailLabel;
    @FXML private TextField detailFullNameField;
    @FXML private TextField detailPhoneField;
    @FXML private Button saveButton;
    @FXML private Button backToListButton;
    @FXML private Text detailMessageText;

    // ===== DETAIL PANEL - Change Email Form =====
    @FXML private TextField newEmailField;
    @FXML private PasswordField emailPasswordField;
    @FXML private Button submitEmailButton;

    // ===== DETAIL PANEL - Change Password Form =====
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button submitPasswordButton;

    // ===== DETAIL PANEL - Delete =====
    @FXML private Button deleteButton;

    // ===== CREATE PANEL =====
    @FXML private TextField createUsernameField;
    @FXML private TextField createEmailField;
    @FXML private PasswordField createPasswordField;
    @FXML private PasswordField createConfirmPasswordField;
    @FXML private TextField createFullNameField;
    @FXML private TextField createPhoneField;
    @FXML private Button createSaveButton;
    @FXML private Button createCancelButton;
    @FXML private Text createMessageText;

    // ===== VARIABLES =====
    private ObservableList<Admin> adminList;

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        setupListPanel();
        showListPanel();
        adminTable.setStyle("-fx-text-fill: black;");
    }

    private void setupListPanel() {
        // Use SimpleProperty wrappers for CORBA objects
        colAdminId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().adminId).asObject());

        colUsername.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().username));

        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().email));

        colFullName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().fullName));

        colPhoneNumber.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().phoneNumber));

        adminTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        adminList = FXCollections.observableArrayList();
        adminTable.setItems(adminList);

        adminTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Admin selected = adminTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    displayAdminDetails(selected);
                    showDetailPanel();
                }
            }
        });
    }

    public void showListPanel() {
        listPanel.setVisible(true);
        detailPanel.setVisible(false);
        createPanel.setVisible(false);
    }

    public void showDetailPanel() {
        listPanel.setVisible(false);
        detailPanel.setVisible(true);
        createPanel.setVisible(false);
        clearDetailMessage();
        clearDetailForms();
    }

    public void showCreatePanel() {
        listPanel.setVisible(false);
        detailPanel.setVisible(false);
        createPanel.setVisible(true);
        clearCreateFields();
        clearCreateMessage();
    }

    // ===== DATA DISPLAY =====
    public void displayAdminList(Admin[] admins) {
        System.out.println("=== View Displaying Admins ===");
        adminList.clear();

        if (admins != null) {
            System.out.println("Received " + admins.length + " admins");
            adminList.addAll(admins);
            System.out.println("Added to observable list. List size now: " + adminList.size());
        } else {
            System.out.println("Received NULL admins array!");
        }

        System.out.println("Table items size: " + adminTable.getItems().size());
    }

    public void displayAdminDetails(Admin admin) {
        if (admin == null) return;

        detailAdminIdLabel.setText(String.valueOf(admin.adminId));
        detailUsernameLabel.setText(admin.username);
        detailEmailLabel.setText(admin.email);
        detailFullNameField.setText(admin.fullName != null ? admin.fullName : "");
        detailPhoneField.setText(admin.phoneNumber != null ? admin.phoneNumber : "");
    }

    // ===== CLEAR FORMS =====
    private void clearDetailForms() {
        // Clear change email form
        newEmailField.clear();
        emailPasswordField.clear();

        // Clear change password form
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void clearCreateFields() {
        createUsernameField.clear();
        createEmailField.clear();
        createPasswordField.clear();
        createConfirmPasswordField.clear();
        createFullNameField.clear();
        createPhoneField.clear();
    }

    // ===== MESSAGE HELPERS =====
    public void showListMessage(String message) {
        if (listMessageText != null) {
            listMessageText.setText(message);
            listMessageText.setVisible(true);
        }
    }

    public void clearListMessage() {
        if (listMessageText != null) {
            listMessageText.setText("");
            listMessageText.setVisible(false);
        }
    }

    public void showDetailMessage(String message) {
        if (detailMessageText != null) {
            detailMessageText.setText(message);
            detailMessageText.setVisible(true);
        }
    }

    public void clearDetailMessage() {
        if (detailMessageText != null) {
            detailMessageText.setText("");
            detailMessageText.setVisible(false);
        }
    }

    public void showCreateMessage(String message) {
        if (createMessageText != null) {
            createMessageText.setText(message);
            createMessageText.setVisible(true);
        }
    }

    public void clearCreateMessage() {
        if (createMessageText != null) {
            createMessageText.setText("");
            createMessageText.setVisible(false);
        }
    }

    // ===== DIALOG METHODS (JavaFX UI Logic) =====

    /**
     * Show delete confirmation dialog
     * @param username The username of the admin to delete
     * @return true if user confirms, false otherwise
     */
    public boolean showDeleteConfirmation(String username) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Admin Account");
        confirmAlert.setContentText("Are you sure you want to delete admin '" + username + "'?\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show success alert dialog
     * @param title The title of the alert
     * @param message The message to display
     */
    public void showSuccessAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ===== GETTERS - Panels =====
    public StackPane getListPanel() { return listPanel; }
    public StackPane getDetailPanel() { return detailPanel; }
    public StackPane getCreatePanel() { return createPanel; }

    // ===== GETTERS - List Panel =====
    public TableView<Admin> getAdminTable() { return adminTable; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getCreateButton() { return createButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Text getListMessageText() { return listMessageText; }

    // ===== GETTERS - Detail Panel Basic Info =====
    public Label getDetailAdminIdLabel() { return detailAdminIdLabel; }
    public Label getDetailUsernameLabel() { return detailUsernameLabel; }
    public Label getDetailEmailLabel() { return detailEmailLabel; }
    public TextField getDetailFullNameField() { return detailFullNameField; }
    public TextField getDetailPhoneField() { return detailPhoneField; }
    public Button getSaveButton() { return saveButton; }
    public Button getBackToListButton() { return backToListButton; }
    public Text getDetailMessageText() { return detailMessageText; }

    // ===== GETTERS - Change Email Form =====
    public TextField getNewEmailField() { return newEmailField; }
    public PasswordField getEmailPasswordField() { return emailPasswordField; }
    public Button getSubmitEmailButton() { return submitEmailButton; }

    // ===== GETTERS - Change Password Form =====
    public PasswordField getOldPasswordField() { return oldPasswordField; }
    public PasswordField getNewPasswordField() { return newPasswordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getSubmitPasswordButton() { return submitPasswordButton; }

    // ===== GETTERS - Delete =====
    public Button getDeleteButton() { return deleteButton; }

    // ===== GETTERS - Create Panel =====
    public TextField getCreateUsernameField() { return createUsernameField; }
    public TextField getCreateEmailField() { return createEmailField; }
    public PasswordField getCreatePasswordField() { return createPasswordField; }
    public PasswordField getCreateConfirmPasswordField() { return createConfirmPasswordField; }
    public TextField getCreateFullNameField() { return createFullNameField; }
    public TextField getCreatePhoneField() { return createPhoneField; }
    public Button getCreateSaveButton() { return createSaveButton; }
    public Button getCreateCancelButton() { return createCancelButton; }
    public Text getCreateMessageText() { return createMessageText; }

    public ObservableList<Admin> getAdminList() { return adminList; }
}