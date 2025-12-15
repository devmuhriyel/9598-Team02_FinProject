package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CustomerHomeController - Controller for customer home screen
 * FIXED VERSION - Matches CustomerHome.fxml structure
 */
public class CustomerHomeController {
    private CustomerHomeView view;
    private CustomerHomeModel model;
    private CustomerNavigationManager navManager;
    private Customer customer;
    private Timeline dateTimeUpdater;

    public CustomerHomeController(CustomerHomeView homeView, CustomerHomeModel homeModel,
                                  CustomerNavigationManager navManager, Customer customerAcct) {
        this.view = homeView;
        this.model = homeModel;
        this.navManager = navManager;
        this.customer = customerAcct;

        view.setCustomerName(customer.username);
        initializeButtons();
        startDateTimeUpdater();

        // Optionally show Browse Movies by default
        // switchToBrowse();
    }

    // ===== BUTTON INITIALIZATION =====

    /**
     * Wire all navigation buttons to their respective handlers
     */
    private void initializeButtons() {
        view.getLogOutButton().setOnMouseClicked(event -> logOut());
        view.getNavBrowse().setOnMouseClicked(event -> switchToBrowse());
        view.getNavBookings().setOnMouseClicked(event -> switchToMyBookings());
        view.getNavProfile().setOnMouseClicked(event -> switchToMyProfile());
    }

    // ===== DATE/TIME UPDATER =====

    /**
     * Start automatic date/time updates every 60 seconds
     */
    private void startDateTimeUpdater() {
        updateDateTime(); // Update immediately

        dateTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            updateDateTime();
        }));
        dateTimeUpdater.setCycleCount(Animation.INDEFINITE);
        dateTimeUpdater.play();
    }

    /**
     * Stop the date/time updater (called on logout)
     */
    private void stopDateTimeUpdater() {
        if (dateTimeUpdater != null) {
            dateTimeUpdater.stop();
        }
    }

    /**
     * Update the date/time display
     */
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy  |  h:mm a");
        String formattedDateTime = now.format(formatter);
        view.updateDateTime(formattedDateTime);
    }

    // ===== NAVIGATION METHODS =====

    /**
     * Logout current customer and return to auth screen
     */
    public void logOut() {
        stopDateTimeUpdater();
        customer = null;
        navManager.logoutAndReturnToAuth();
    }

    /**
     * Switch to Browse Movies view
     */
    public void switchToBrowse() {
        view.changePanes("navBrowse");
        navManager.showBrowseMoviesView();
    }

    /**
     * Switch to My Bookings view
     */
    public void switchToMyBookings() {
        view.changePanes("navBookings");
        navManager.showMyBookingsView();
    }

    /**
     * Switch to Booking History view
     */

    /**
     * Switch to My Profile view
     */
    public void switchToMyProfile() {
        view.changePanes("navProfile");
        navManager.showMyProfileView();
    }
    // ===== GETTERS =====

    public Customer getCustomer() {
        return customer;
    }
}