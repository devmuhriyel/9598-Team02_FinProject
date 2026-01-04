package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.Login.CustomerLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs.MovieNavigationManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.BookingsMVCs.CustomerBookingNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MyProfileMVCs.ProfileNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.SessionValidator;
import java.io.IOException;

/**
 * ✅ UPDATED: CustomerNavigationManager with concurrent login detection
 */
public class CustomerNavigationManager {
    private Stage homeStage;
    private CustomerAuthNavManager authNavManager;
    private Customer customer;
    private String sessionToken; // ✅ Now properly stored

    private Pane homePane;
    private CustomerHomeController homeController;
    private CustomerHomeView homeView;

    // Cache for main section panes
    private Pane browseMoviesPane;
    private Pane myBookingsPane;
    private Pane bookingHistoryPane;
    private Pane myProfilePane;
    private Pane settingsPane;
    private SessionValidator sessionValidator;

    // Sub-navigation managers
    private MovieNavigationManager movieNavManager;
    private CustomerBookingNavManager bookingNavManager;
    private ProfileNavManager profileNavManager;

    /**
     * ✅ UPDATED: Now accepts sessionToken parameter
     */
    public CustomerNavigationManager(Stage homeStage, CustomerAuthNavManager authNavManager,
                                     Customer customer, String sessionToken) {
        this.homeStage = homeStage;
        this.authNavManager = authNavManager;
        this.customer = customer;
        this.sessionToken = sessionToken; // ✅ Properly store the token

        System.out.println("🔑 Session token stored: " +
                (sessionToken != null ? sessionToken.substring(0, 8) + "..." : "NULL"));

        // Initialize CORBA connection
        CustomerParentModel.initializeCORBAConnection(new String[]{});

        initializeHomeView();
        startSessionValidation();
        homeStage.setOnCloseRequest(event -> {
            cleanup();
        });
    }

    // ===== INITIALIZE HOME VIEW =====

    private void initializeHomeView() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerHome.fxml")
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

    public void showBrowseMoviesView() {
        if (browseMoviesPane == null) {
            try {
                movieNavManager = new MovieNavigationManager(customer);
                browseMoviesPane = movieNavManager.getMovieBrowseView();

                if (browseMoviesPane == null) {
                    System.err.println("ERROR: Movie Browse returned null pane!");
                    browseMoviesPane = createPlaceholder("Browse Movies", "Error Loading");
                } else {
                    System.out.println("✅ Browse Movies loaded successfully");
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR initializing Browse Movies:");
                e.printStackTrace();
                browseMoviesPane = createPlaceholder("Browse Movies", "Error Loading");
            }
        }
        loadContentPane(browseMoviesPane);
    }

    public void showMyBookingsView() {
        if (myBookingsPane == null) {
            try {
                bookingNavManager = new CustomerBookingNavManager(customer);
                myBookingsPane = bookingNavManager.getMyBookingsView();

                if (myBookingsPane == null) {
                    System.err.println("ERROR: My Bookings returned null pane!");
                    myBookingsPane = createPlaceholder("My Bookings", "Error Loading View");
                } else {
                    System.out.println("✅ My Bookings loaded successfully");
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR initializing My Bookings:");
                e.printStackTrace();
                myBookingsPane = createPlaceholder("My Bookings", "Error: " + e.getMessage());
            }
        }

        if (homeView != null && homeView.getPanesContainer() != null) {
            homeView.getPanesContainer().getChildren().setAll(myBookingsPane);
        }
    }

    public void showMyProfileView() {
        if (myProfilePane == null) {
            try {
                profileNavManager = new ProfileNavManager(customer);
                myProfilePane = profileNavManager.getMyProfileView();

                if (myProfilePane == null) {
                    System.err.println("ERROR: My Profile returned null pane!");
                    myProfilePane = createPlaceholder("My Profile", "Error Loading View");
                } else {
                    System.out.println("✅ My Profile loaded successfully");
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR initializing My Profile:");
                e.printStackTrace();
                myBookingsPane = createPlaceholder("My Profile", "Error: " + e.getMessage());
            }
        }
        loadContentPane(myProfilePane);
    }

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
     * ✅ UPDATED: Logout with proper session token
     */
    public void logoutAndReturnToAuth() {
        cleanup();

        try {
            if (CustomerParentModel.clientServices != null && sessionToken != null) {
                CustomerParentModel.clientServices.logoutCustomer(
                        customer.customerId,
                        sessionToken // ✅ Use the stored token
                );
                System.out.println("✅ Server session cleared for customer: " + customer.customerId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error during server logout: " + e.getMessage());
        }

        try {
            homeStage.close();
            authNavManager.reopenAuthStage();
            System.out.println("✅ Customer logged out successfully");
        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Pane createPlaceholder(String title, String description) {
        Pane placeholder = new Pane();
        placeholder.setStyle("-fx-background-color: #1A1A1A;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        titleLabel.setLayoutX(50);
        titleLabel.setLayoutY(50);

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
        descLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 16px;");
        descLabel.setLayoutX(50);
        descLabel.setLayoutY(100);

        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("🔨 Coming Soon...");
        statusLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px; -fx-font-style: italic;");
        statusLabel.setLayoutX(50);
        statusLabel.setLayoutY(140);

        placeholder.getChildren().addAll(titleLabel, descLabel, statusLabel);
        return placeholder;
    }

    /**
     * ✅ UPDATED: Session validation with concurrent login detection
     */
    private void startSessionValidation() {
        sessionValidator = new SessionValidator(
                customer.customerId,
                "customer",
                new SessionValidator.SessionValidationCallback() {
                    @Override
                    public boolean validateSession() {
                        CustomerLoginModel tempModel = new CustomerLoginModel();
                        return tempModel.validateSession(customer.customerId);
                    }

                    @Override
                    public void onSessionInvalidated() {
                        handleSessionExpired();
                    }
                }
        );

        sessionValidator.start();
        System.out.println("🔄 Session validation started for customer: " + customer.customerId);
    }

    /**
     * ✅ UPDATED: Shows alert for concurrent login
     */
    private void handleSessionExpired() {
        System.out.println("🚨 Customer session expired: " + customer.customerId);
        cleanup();

        Platform.runLater(() -> {
            try {
                // Show alert dialog
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Session Terminated");
                alert.setHeaderText("You have been logged out");
                alert.setContentText("Your account has been logged in from another location. " +
                        "You have been automatically logged out for security reasons.");
                alert.showAndWait();

                // Close home stage
                homeStage.close();

                // Reopen auth stage with message
                authNavManager.reopenAuthStage(
                        "Your account was logged in from another location. Please log in again."
                );

            } catch (Exception e) {
                System.err.println("❌ Error during forced logout: " + e.getMessage());
                e.printStackTrace();
                if (homeStage != null) {
                    homeStage.close();
                }
            }
        });
    }

    private void cleanup() {
        if (sessionValidator != null && sessionValidator.isRunning()) {
            sessionValidator.stop();
            System.out.println("🛑 Session validator stopped for customer: " + customer.customerId);
        }
        
        // Call logout on server to invalidate session
        if (customer != null && CustomerParentModel.clientServices != null) {
            try {
                CustomerParentModel.clientServices.logoutCustomer(customer.customerId, sessionToken);
                System.out.println("✅ Customer " + customer.customerId + " logged out from server");
            } catch (Exception e) {
                System.err.println("⚠️ Error calling logout on server: " + e.getMessage());
            }
        }
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

    public MovieNavigationManager getMovieNavManager() {
        return movieNavManager;
    }
}