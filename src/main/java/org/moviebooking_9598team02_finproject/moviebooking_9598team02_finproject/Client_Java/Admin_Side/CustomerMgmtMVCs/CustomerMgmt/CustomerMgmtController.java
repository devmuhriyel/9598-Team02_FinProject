package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

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
        view.getSearchButton().setOnAction(event -> handleSearchButtonClick());
        view.getRefreshButton().setOnAction(event -> handleRefreshButtonClick());
        view.getSaveButton().setOnAction(event -> handleSaveCustomerButtonClick());
        view.getBackToListButton().setOnAction(event -> {
            view.showListPanel();
            view.clearDetailMessage();
        });
    }

    // ===== LIST PANEL HANDLERS =====
    private void handleSearchButtonClick() {
        String searchText = view.getSearchField().getText();

        // Handle empty search - just refresh the list
        if (searchText == null || searchText.trim().isEmpty()) {
            try {
                loadAllCustomers();
                view.showListMessage("✅ Showing all customers");
            } catch (AuthenticationException e) {
                view.showListMessage("❌ " + e.getMessage());
            }
            return;
        }

        // ✅ VALIDATE using model
        String error = model.validateSearchQuery(searchText);
        if (error != null) {
            view.showListMessage("❌ " + error);
            return;
        }

        try {
            view.clearListMessage();
            String trimmed = searchText.trim();

            Customer[] results = model.searchCustomers(trimmed);
            view.displayCustomerList(results);
            view.showListMessage("✅ Found " + results.length + " customer(s)");
        } catch (AuthenticationException e) {
            view.showListMessage("❌ " + e.getMessage());
        }
    }

    private void handleRefreshButtonClick() {
        try {
            view.clearListMessage();
            loadAllCustomers();
            view.showListMessage("✅ Customer list refreshed");
        } catch (AuthenticationException e) {
            view.showListMessage("❌ " + e.getMessage());
        }
    }

    // ===== DETAIL PANEL - SAVE CUSTOMER INFO =====

    private void handleSaveCustomerButtonClick() {
        try {
            String customerIdText = view.getDetailCustomerIdLabel().getText();
            int targetCustomerId = Integer.parseInt(customerIdText);
            String email = view.getDetailEmailField().getText();
            String fullName = view.getDetailFullNameField().getText();
            String phoneNumber = view.getDetailPhoneField().getText();

            // ✅ VALIDATE using model
            String error = model.validateCustomerUpdate(email, fullName, phoneNumber);
            if (error != null) {
                view.showDetailMessage("❌ " + error);
                return;
            }

            view.clearDetailMessage();
            model.updateCustomerInfo(targetCustomerId, email, phoneNumber, fullName);
            view.showDetailMessage("✅ Customer information updated successfully!");
            loadAllCustomers();

        } catch (NumberFormatException e) {
            view.showDetailMessage("❌ Invalid customer ID");
        } catch (AuthenticationException e) {
            if (e.getMessage().contains("Email already")) {
                view.showDetailMessage("❌ This email is already in use");
            } else {
                view.showDetailMessage("❌ " + e.getMessage());
            }
        }
    }

    // ===== DATA LOADING =====
    public void loadAllCustomers() throws AuthenticationException {
        System.out.println("=== Loading Customers ===");
        customerArrays = model.loadAllCustomers();
        System.out.println("Got " + customerArrays.length + " customers from model");
        view.displayCustomerList(customerArrays);
        System.out.println("Data passed to view");
    }
}
