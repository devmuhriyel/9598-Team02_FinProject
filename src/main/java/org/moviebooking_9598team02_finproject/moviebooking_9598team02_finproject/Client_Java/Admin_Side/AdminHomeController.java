package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.scene.layout.StackPane;
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
        switchToDashboard();
    }

    private void initializeButtons() {
        view.getLogOutButton().setOnMouseClicked(event -> logOut());
        view.getNavDashboard().setOnMouseClicked(event -> switchToDashboard());
        view.getNavMovie().setOnMouseClicked(event -> switchToMovieMgmt());
        view.getNavShowtime().setOnMouseClicked(event -> switchToSeatMgmt());
        view.getNavBooking().setOnMouseClicked(event -> switchToBookingMgmt());
        view.getNavCustomer().setOnMouseClicked(event -> switchToCustomerMgmt());
    }

    public void logOut() {
        admin = null;
        navManager.logoutAndReturnToAuth();
    }

    public void switchToDashboard() {
        navManager.showDashboardView();
    }
    public void switchToMovieMgmt() {
        navManager.showMovieMgmtView();
    }
    public void switchToSeatMgmt() {
        navManager.showSeatMgmtView();
    }
    public void switchToBookingMgmt() {
        navManager.showBookingMgmtView();
    }
    public void switchToCustomerMgmt() {
        navManager.showCustomerMgmtView();
    }
}
