package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;

import java.util.Optional;

/**
 * CustomerMgmtView - Pure UI for Customer Management
 * Handles all JavaFX-specific UI logic including dialogs
 */
public class CustomerMgmtView {

    // ===== PANELS =====
    @FXML
    private StackPane listPanel;
    @FXML
    private StackPane detailPanel;

    // ===== LIST PANEL =====
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> colUsername;
    @FXML
    private TableColumn<Customer, String> colEmail;
    @FXML
    private TableColumn<Customer, String> colFullName;
    @FXML
    private TableColumn<Customer, String> colPhoneNumber;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Text listMessageText;

    // ===== DETAIL PANEL - Basic Info =====
    @FXML
    private Label detailCustomerIdLabel;
    @FXML
    private Label detailUsernameLabel;
    @FXML
    private TextField detailEmailField;
    @FXML
    private TextField detailFullNameField;
    @FXML
    private TextField detailPhoneField;
    @FXML
    private Button saveButton;
    @FXML
    private Button backToListButton;
    @FXML
    private Text detailMessageText;

    // ===== DETAIL PANEL - Status =====
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;


    // ===== VARIABLES =====
    private ObservableList<Customer> customerList;

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        setupListPanel();
        showListPanel();
        customerTable.setStyle("-fx-text-fill: black;");
    }

    private void setupListPanel() {
        // Use SimpleProperty wrappers for CORBA objects
        colUsername.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().username));

        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().email));

        colFullName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().fullName));

        colPhoneNumber.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().phoneNumber));

        customerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        customerList = FXCollections.observableArrayList();
        customerTable.setItems(customerList);

        customerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Customer selected = customerTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    displayCustomerDetails(selected);
                    showDetailPanel();
                }
            }
        });
    }

    public void showListPanel() {
        listPanel.setVisible(true);
        detailPanel.setVisible(false);
    }

    public void showDetailPanel() {
        listPanel.setVisible(false);
        detailPanel.setVisible(true);
        clearDetailMessage();
    }

    // ===== DATA DISPLAY =====
    public void displayCustomerList(Customer[] customers) {
        System.out.println("=== View Displaying Customers ===");
        customerList.clear();

        if (customers != null) {
            System.out.println("Received " + customers.length + " customers");
            customerList.addAll(customers);
            System.out.println("Added to observable list. List size now: " + customerList.size());
        } else {
            System.out.println("Received NULL customers array!");
        }

        System.out.println("Table items size: " + customerTable.getItems().size());
    }

    public void displayCustomerDetails(Customer customer) {
        if (customer == null) return;

        detailCustomerIdLabel.setText(String.valueOf(customer.customerId));
        detailUsernameLabel.setText(customer.username);
        detailEmailField.setText(customer.email != null ? customer.email : "");
        detailFullNameField.setText(customer.fullName != null ? customer.fullName : "");
        detailPhoneField.setText(customer.phoneNumber != null ? customer.phoneNumber : "");

        // Status - default to Active since field may not be populated
        statusLabel.setText("Active");
        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14;");

        // Created date
        createdAtLabel.setText(customer.createdAt != null ? customer.createdAt : "N/A");
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

    /**
     * Show success alert dialog
     *
     * @param title   The title of the alert
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

    /**
     * Update customer status display
     *
     * @param isActive true for active, false for inactive
     */
    public void updateCustomerStatus(boolean isActive) {
        statusLabel.setText(isActive ? "Active" : "Inactive");
        statusLabel.setStyle(isActive ?
                "-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14;" :
                "-fx-text-fill: #DC3545; -fx-font-weight: bold; -fx-font-size: 14;");
    }

    // ===== GETTERS - Panels =====
    public StackPane getListPanel() {
        return listPanel;
    }

    public StackPane getDetailPanel() {
        return detailPanel;
    }

    // ===== GETTERS - List Panel =====
    public TableView<Customer> getCustomerTable() {
        return customerTable;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Text getListMessageText() {
        return listMessageText;
    }

    // ===== GETTERS - Detail Panel Basic Info =====
    public Label getDetailCustomerIdLabel() {
        return detailCustomerIdLabel;
    }

    public Label getDetailUsernameLabel() {
        return detailUsernameLabel;
    }

    public TextField getDetailEmailField() {
        return detailEmailField;
    }

    public TextField getDetailFullNameField() {
        return detailFullNameField;
    }

    public TextField getDetailPhoneField() {
        return detailPhoneField;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getBackToListButton() {
        return backToListButton;
    }

    public Text getDetailMessageText() {
        return detailMessageText;
    }

    // ===== GETTERS - Status =====
    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getCreatedAtLabel() {
        return createdAtLabel;
    }

    public ObservableList<Customer> getCustomerList() {
        return customerList;
    }
}