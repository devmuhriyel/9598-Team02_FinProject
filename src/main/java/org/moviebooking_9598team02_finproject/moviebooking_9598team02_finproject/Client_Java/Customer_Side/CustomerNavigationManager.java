package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;

import java.io.IOException;

/**
 * CustomerNavigationManager - Manages customer home screen and all main sections
 * FIXED VERSION - Matches CustomerHome.fxml navigation structure
 */
public class CustomerNavigationManager {
    private Stage homeStage;
    private CustomerAuthNavManager authNavManager;
    private Customer customer;

    private Pane homePane;
    private CustomerHomeController homeController;
    private CustomerHomeView homeView;

    // Cache for main section panes
    private Pane browseMoviesPane;
    private Pane myBookingsPane;
    private Pane bookingHistoryPane;
    private Pane myProfilePane;
    private Pane settingsPane;

    // Sub-navigation managers (add as needed)
    // private CustomerMovieNavManager movieNavManager;
    // private CustomerBookingNavManager bookingNavManager;

    public CustomerNavigationManager(Stage homeStage, CustomerAuthNavManager authNavManager, Customer customer) {
        this.homeStage = homeStage;
        this.authNavManager = authNavManager;
        this.customer = customer;

        // Initialize CORBA connection if needed
        CustomerParentModel.initializeCORBAConnection(new String[]{});

        initializeHomeView();
    }

    // ===== INITIALIZE HOME VIEW =====

    private void initializeHomeView() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerHome.fxml")
            );
            homePane = homeLoader.load();
            homeView = homeLoader.getController();
            CustomerHomeModel homeModel = new CustomerHomeModel();
            homeController = new CustomerHomeController(homeView, homeModel, this, customer);

            Scene homeScene = new Scene(homePane, 1280, 720);
            homeStage.setScene(homeScene);
            homeStage.setTitle("FilmBook - " + customer.username);
            homeStage.show();

            System.out.println("✅ Customer home view initialized");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing customer home view: " + e.getMessage());
        }
    }

    // ===== SECTION NAVIGATION METHODS =====

    /**
     * Show Browse Movies view
     */
    public void showBrowseMoviesView() {
        if (browseMoviesPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerBrowseMovies.fxml")
                );
                browseMoviesPane = loader.load();
                System.out.println("✅ Browse Movies view loaded");

                // Initialize movie nav manager if this section has sub-views
                StackPane movieContainer = (StackPane) browseMoviesPane.lookup("#movieSubContainer");
                if (movieContainer != null) {
                    // TODO: Initialize CustomerMovieNavManager when implemented
                    // movieNavManager = new CustomerMovieNavManager(movieContainer, customer);
                }

            } catch (IOException e) {
                System.err.println("⚠️ Browse Movies FXML not found, creating placeholder");
                e.printStackTrace();
                browseMoviesPane = createPlaceholder("Browse Movies", "Explore available films and showtimes");
            }
        }
        loadContentPane(browseMoviesPane);
    }

    /**
     * Show My Bookings view (active bookings)
     */
    public void showMyBookingsView() {
        if (myBookingsPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerMyBookings.fxml")
                );
                myBookingsPane = loader.load();
                System.out.println("✅ My Bookings view loaded");

                // Initialize booking nav manager if this section has sub-views
                StackPane bookingContainer = (StackPane) myBookingsPane.lookup("#bookingSubContainer");
                if (bookingContainer != null) {
                    // TODO: Initialize CustomerBookingNavManager when implemented
                    // bookingNavManager = new CustomerBookingNavManager(bookingContainer, customer);
                }

            } catch (IOException e) {
                System.err.println("⚠️ My Bookings FXML not found, creating placeholder");
                e.printStackTrace();
                myBookingsPane = createPlaceholder("My Bookings", "View and manage your active bookings");
            }
        }
        loadContentPane(myBookingsPane);
    }

    /**
     * Show Booking History view (past bookings)
     */
    public void showBookingHistoryView() {
        if (bookingHistoryPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerBookingHistory.fxml")
                );
                bookingHistoryPane = loader.load();
                System.out.println("✅ Booking History view loaded");

            } catch (IOException e) {
                System.err.println("⚠️ Booking History FXML not found, creating placeholder");
                e.printStackTrace();
                bookingHistoryPane = createPlaceholder("Booking History", "View your past bookings and transactions");
            }
        }
        loadContentPane(bookingHistoryPane);
    }

    /**
     * Show My Profile view
     */
    public void showMyProfileView() {
        if (myProfilePane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerProfile.fxml")
                );
                myProfilePane = loader.load();
                System.out.println("✅ My Profile view loaded");

            } catch (IOException e) {
                System.err.println("⚠️ My Profile FXML not found, creating placeholder");
                e.printStackTrace();
                myProfilePane = createPlaceholder("My Profile", "Manage your account settings");
            }
        }
        loadContentPane(myProfilePane);
    }

    /**
     * Show Settings view
     */
    public void showSettingsView() {
        if (settingsPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerSettings.fxml")
                );
                settingsPane = loader.load();
                System.out.println("✅ Settings view loaded");

            } catch (IOException e) {
                System.err.println("⚠️ Settings FXML not found, creating placeholder");
                e.printStackTrace();
                settingsPane = createPlaceholder("Settings", "Customize your preferences");
            }
        }
        loadContentPane(settingsPane);
    }

    // ===== HELPER METHODS =====

    /**
     * Load a pane into the main content container
     */
    private void loadContentPane(Pane pane) {
        try {
            StackPane container = homeView.getPanesContainer();
            container.getChildren().clear();
            container.getChildren().add(pane);
        } catch (Exception e) {
            System.err.println("❌ Error loading content pane: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logout and return to authentication screen
     */
    public void logoutAndReturnToAuth() {
        try {
            homeStage.close();
            authNavManager.reopenAuthStage();
            System.out.println("✅ Customer logged out successfully");
        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a placeholder pane for sections not yet implemented
     */
    private Pane createPlaceholder(String title, String description) {
        Pane placeholder = new Pane();
        placeholder.setStyle("-fx-background-color: #1A1A1A;");

        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        titleLabel.setLayoutX(50);
        titleLabel.setLayoutY(50);

        // Description
        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
        descLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 16px;");
        descLabel.setLayoutX(50);
        descLabel.setLayoutY(100);

        // Coming Soon message
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("📝 Coming Soon...");
        statusLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px; -fx-font-style: italic;");
        statusLabel.setLayoutX(50);
        statusLabel.setLayoutY(140);

        placeholder.getChildren().addAll(titleLabel, descLabel, statusLabel);
        return placeholder;
    }

    // ===== GETTERS =====

    public Customer getCustomer() {
        return customer;
    }

    public CustomerHomeView getHomeView() {
        return homeView;
    }

    public CustomerHomeController getHomeController() {
        return homeController;
    }
}