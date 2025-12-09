package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;

public class CustomerHomeController {
    private CustomerHomeView view;
    private CustomerHomeModel model;
    private CustomerNavigationManager navManager;
    private Customer customer;

    public CustomerHomeController(CustomerHomeView homeView, CustomerHomeModel homeModel, CustomerNavigationManager navManager, Customer customerAcct) {
        this.view = homeView;
        this.model = homeModel;
        this.navManager = navManager;
        this.customer = customerAcct;
        view.setCustomerName(customer.username);
        initializeButtons();
//        switchToDashboard();
    }

    private void initializeButtons() {
        view.getLogOutButton().setOnMouseClicked(event -> logOut());
        view.getNavDashboard().setOnMouseClicked(event -> switchToDashboard());
        view.getNavMovie().setOnMouseClicked(event -> switchToMovieMgmt());
        view.getNavShowtime().setOnMouseClicked(event -> switchToSeatMgmt());
        view.getNavBooking().setOnMouseClicked(event -> switchToBookingMgmt());
        view.getNavCustomer().setOnMouseClicked(event -> switchToCustomerMgmt());
        view.getNavGenre().setOnMouseClicked(event -> switchToGenreMgmt());
    }

    public void logOut() {
        customer = null;
        navManager.logoutAndReturnToAuth();
    }

    public void switchToDashboard() {
        view.changePanes("navDashboard");
        navManager.showDashboardView();
    }

    public void switchToMovieMgmt() {
        view.changePanes("navMovie");
        navManager.showMovieMgmtView();
    }

    public void switchToSeatMgmt() {
        view.changePanes("navShowtime");
        navManager.showSeatMgmtView();
    }

    public void switchToBookingMgmt() {
        view.changePanes("navBooking");
        navManager.showBookingMgmtView();
    }

    public void switchToCustomerMgmt() {
        view.changePanes("navCustomer");
        navManager.showCustomerMgmtView();
    }

    public void switchToGenreMgmt(){
        view.changePanes("navGenre");
//        navManager.showGenreMgmtView();
    }
}