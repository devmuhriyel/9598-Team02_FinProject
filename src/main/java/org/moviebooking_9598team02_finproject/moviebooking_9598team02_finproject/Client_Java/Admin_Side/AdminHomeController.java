package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;

public class AdminHomeController {
    private AdminHomeView view;
    private AdminHomeModel model;
    private AdminNavigationManager navManager;
    private Admin admin;

    public AdminHomeController(AdminHomeView homeView, AdminHomeModel homeModel, AdminNavigationManager navManager, Admin adminAcct) {
        this.view = homeView;
        this.model = homeModel;
        this.navManager = navManager;
        this.admin = adminAcct;
        view.setAdminName(admin.username);
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
        view.getNavAdmin().setOnMouseClicked(event -> switchToAdminMgmt());
        view.getNavGenre().setOnMouseClicked(event -> switchToGenreMgmt());
    }

    public void logOut() {
        admin = null;
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

    private void switchToAdminMgmt() {
        view.changePanes("navAdmin");
        navManager.showAdminMgmtView();
    }

    public void switchToGenreMgmt(){
        view.changePanes("navGenre");
//        navManager.showGenreMgmtView();
    }
}