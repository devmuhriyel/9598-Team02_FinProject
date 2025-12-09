package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

/**
 * CustomerMgmtController - Pure business logic, no JavaFX imports
 */
public class CustomerMgmtController {
    private CustomerMgmtModel model;
    private CustomerMgmtView view;
    private Customer[] customerArrays;

    public CustomerMgmtController(CustomerMgmtModel model, CustomerMgmtView view) throws AuthenticationException {
        this.model = model;
        this.view = view;
        initializeButtons();
        loadAllCustomers();
    }

    // ===== BUTTON INITIALIZATION =====
    private void initializeButtons() {
        // List Panel Buttons
        view.getSearchButton().setOnAction(event -> handleSearchButtonClick());
        view.getRefreshButton().setOnAction(event -> handleRefreshButtonClick());

        // Detail Panel - Basic Info
        view.getSaveButton().setOnAction(event -> handleSaveCustomerButtonClick());
        view.getBackToListButton().setOnAction(event -> {
            view.showListPanel();
            view.clearDetailMessage();
        });
    }

    // ===== LIST PANEL HANDLERS =====
    private void handleSearchButtonClick() {
        String searchText = view.getSearchField().getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            view.showListMessage("Please enter a search term");
            return;
        }

        try {
            view.clearListMessage();

            if (searchText.matches("\\d+")) {
                Customer customer = model.getCustomerDetails(searchText);
                if (customer != null) {
                    Customer[] result = new Customer[1];
                    result[0] = customer;
                    view.displayCustomerList(result);
                    view.showListMessage("Found 1 customer");
                } else {
                    view.displayCustomerList(new Customer[0]);
                    view.showListMessage("No customer found with ID: " + searchText);
                }
            } else {
                Customer[] results = model.searchCustomers(searchText);
                view.displayCustomerList(results);
                view.showListMessage("Found " + results.length + " customer(s)");
            }
        } catch (AuthenticationException e) {
            view.showListMessage("Error: " + e.getMessage());
        }
    }

    private void handleRefreshButtonClick() {
        try {
            view.clearListMessage();
            loadAllCustomers();
            view.showListMessage("Customer list refreshed");
        } catch (AuthenticationException e) {
            view.showListMessage("Error: " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - SAVE CUSTOMER INFO =====
    private void handleSaveCustomerButtonClick() {
        try {
            String customerIdText = view.getDetailCustomerIdLabel().getText();
            int targetCustomerId = Integer.parseInt(customerIdText);  // ✅ Changed to long

            String email = view.getDetailEmailField().getText();
            String fullName = view.getDetailFullNameField().getText();
            String phoneNumber = view.getDetailPhoneField().getText();

            // Validate inputs
            if (email == null || email.trim().isEmpty()) {
                view.showDetailMessage("❌ Email cannot be empty");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                view.showDetailMessage("❌ Invalid email format");
                return;
            }

            view.clearDetailMessage();
            model.updateCustomerInfo(targetCustomerId, email, phoneNumber, fullName);
            view.showDetailMessage("✅ Customer information updated successfully!");

            try {
                loadAllCustomers();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Error: Invalid customer ID");
        } catch (AuthenticationException e) {
            view.showDetailMessage("❌ Error: " + e.getMessage());
        }
    }

    // ===== DATA LOADING =====
    public void loadAllCustomers() throws AuthenticationException {
        System.out.println("=== Loading Customers ===");
        customerArrays = model.loadAllCustomers();
        System.out.println("Got " + customerArrays.length + " customers from model");

        for (Customer customer : customerArrays) {
            System.out.println("Customer: " + customer.customerId + " - " + customer.username + " - " + customer.email);
        }

        view.displayCustomerList(customerArrays);
        System.out.println("Data passed to view");
    }
}