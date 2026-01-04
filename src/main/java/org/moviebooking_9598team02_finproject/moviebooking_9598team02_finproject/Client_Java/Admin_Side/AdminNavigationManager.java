package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AddtlFeaturesMVCs.AdminDashboardNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.Login.AdminLoginModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs.AdminBookingNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AccountNavigationManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs.AdminMMNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs.AdminSMNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.PaymentMVCs.AdminPaymentNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs.AdminGenreManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.SessionValidator;

import java.io.IOException;

/**
 * ✅ UPDATED: AdminNavigationManager with concurrent login detection
 */
public class AdminNavigationManager {
    private Stage homeStage;
    private AdminAuthNavManager authNavManager;
    private Admin admin;
    private String sessionToken; // ✅ Now properly stored

    private Pane homePane;
    private AdminHomeController homeController;
    private AdminHomeView homeView;

    // Cache for main section panes
    private Pane dashboardPane;
    private Pane movieMgmtPane;
    private Pane bookingMgmtPane;
    private Pane customerMgmtPane;
    private Pane adminMgmtPane;
    private Pane paymentMgmtPane;

    // Sub-navigation managers
    private AdminBookingNavManager bookingNavManager;
    private AdminMMNavManager movieNavManager;
    private AdminSMNavManager seatNavManager;
    private AccountNavigationManager accNavManager;
    private AdminPaymentNavManager paymentNavManager;
    private AdminDashboardNavManager dashboardNavManager;
    private AdminGenreManager genreManager;

    private SessionValidator sessionValidator;

    /**
     * ✅ UPDATED: Now accepts sessionToken parameter
     */
    public AdminNavigationManager(Stage homeStage, AdminAuthNavManager authNavManager,
                                  Admin admin, String sessionToken) {
        this.homeStage = homeStage;
        this.authNavManager = authNavManager;
        this.admin = admin;
        this.sessionToken = sessionToken; // ✅ Properly store the token

        System.out.println("🔑 Session token stored: " +
                (sessionToken != null ? sessionToken.substring(0, 8) + "..." : "NULL"));

        AdminParentModel.initializeCORBAConnection(new String[]{});

        this.seatNavManager = new AdminSMNavManager(admin);
        this.paymentNavManager = new AdminPaymentNavManager(admin);
        this.bookingNavManager = new AdminBookingNavManager(admin);
        this.dashboardNavManager = new AdminDashboardNavManager(admin);

        startSessionValidation();

        initializeHomeView();
        showDashboardView();

        homeStage.setOnCloseRequest(event -> {
            cleanup();
        });
    }

    private void initializeHomeView() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AdminHome.fxml"));
            homePane = homeLoader.load();
            homeView = homeLoader.getController();
            AdminHomeModel homeModel = new AdminHomeModel();
            homeController = new AdminHomeController(homeView, homeModel, this, admin);

            Scene homeScene = new Scene(homePane, 1280, 720);
            homeStage.setScene(homeScene);
            homeStage.setTitle("FilmBook Admin - " + admin.username);
            homeStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ UPDATED: Session validation with concurrent login detection
     */
    private void startSessionValidation() {
        sessionValidator = new SessionValidator(
                admin.adminId,
                "admin",
                new SessionValidator.SessionValidationCallback() {
                    @Override
                    public boolean validateSession() {
                        AdminParentModel tempModel = new AdminHomeModel();
                        return tempModel.validateSession(admin.adminId);
                    }

                    @Override
                    public void onSessionInvalidated() {
                        handleSessionExpired();
                    }
                }
        );

        sessionValidator.start();
        System.out.println("🔄 Session validation started for admin: " + admin.adminId);
    }

    /**
     * ✅ UPDATED: Shows alert for concurrent login
     */
    private void handleSessionExpired() {
        System.out.println("🚨 Admin session expired: " + admin.adminId);
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
            System.out.println("🛑 Session validator stopped for admin: " + admin.adminId);
        }
    }

    // === NAVIGATION METHODS (UNCHANGED) ===

    public void showDashboardView() {
        Pane pane = dashboardNavManager.getDashboardView();
        loadContentPane(pane);
    }

    public void showMovieMgmtView() {
        if (movieMgmtPane == null) {
            try {
                movieNavManager = new AdminMMNavManager(AdminParentModel.adminServices, admin);
                movieMgmtPane = movieNavManager.getMovieMgmtView();

                if (movieMgmtPane == null) {
                    System.err.println("ERROR: Movie Management returned null pane!");
                    movieMgmtPane = createPlaceholder("Movie Management - Error Loading");
                } else {
                    System.out.println("✅ Movie Management loaded successfully");
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR initializing Movie Management:");
                e.printStackTrace();
                movieMgmtPane = createPlaceholder("Movie Management - Error Loading");
            }
        }
        loadContentPane(movieMgmtPane);
    }

    public void showSeatMgmtView() {
        Pane pane = seatNavManager.getSeatMgmtView();
        loadContentPane(pane);
    }

    public void showBookingMgmtView() {
        if (bookingNavManager == null) {
            bookingNavManager = new AdminBookingNavManager(admin);
        }

        Pane bookingViewContent = bookingNavManager.getBookingMgmtView();

        if (bookingViewContent == null) {
            System.err.println("ERROR: Booking Mgmt View returned null!");
            bookingViewContent = createPlaceholder("Booking Management - Error Loading");
        }

        loadContentPane(bookingViewContent);
    }

    public void showCustomerMgmtView() {
        if (customerMgmtPane == null) {
            AccountNavigationManager customerNavManager = new AccountNavigationManager(admin.adminId, false);
            customerMgmtPane = customerNavManager.getCustomerPane();

            if (customerMgmtPane == null) {
                System.err.println("ERROR: Customer Management returned null pane!");
                customerMgmtPane = createPlaceholder("Customer Management - Error Loading");
            } else {
                System.out.println("✅ Customer Management loaded successfully");
            }
        }
        loadContentPane(customerMgmtPane);
    }

    public void showGenreMgmtView(){
        if (genreManager == null) {
            genreManager = new AdminGenreManager(admin.adminId);
        }
        Pane genreViewContent = genreManager.getGenrePane();
        if (genreViewContent == null) {
            System.err.println("ERROR: Genre Management returned null pane!");
            genreViewContent = createPlaceholder("Genre Management - Error Loading");
        } else {
            System.out.println("✅ Genre Management loaded successfully");
        }
        loadContentPane(genreViewContent);
    }

    public void showAdminMgmtView(){
        if (adminMgmtPane == null) {
            accNavManager = new AccountNavigationManager(admin.adminId);
            adminMgmtPane = accNavManager.getAdminPane();

            if (adminMgmtPane == null) {
                System.err.println("ERROR: AdminAccountManager returned null pane!");
                adminMgmtPane = createPlaceholder("Admin Management - Error Loading");
            } else {
                System.out.println("✅ Admin Management loaded successfully");
            }
        }
        loadContentPane(adminMgmtPane);
    }

    public void showPaymentMgmtView() {
        Pane paymentViewContent = paymentNavManager.getPaymentMgmtView();
        loadContentPane(paymentViewContent);
    }

    private void loadContentPane(Pane pane) {
        StackPane container = homeView.getPanesContainer();
        container.getChildren().clear();
        container.getChildren().add(pane);
    }

    /**
     * ✅ UPDATED: Logout with proper session token
     */
    public void logoutAndReturnToAuth() {
        cleanup();

        try {
            if (AdminParentModel.adminServices != null && sessionToken != null) {
                AdminParentModel.adminServices.logoutAdmin(
                        admin.adminId,
                        sessionToken // ✅ Use the stored token
                );
                System.out.println("✅ Server session cleared for admin: " + admin.adminId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error during server logout: " + e.getMessage());
        }

        homeStage.close();
        authNavManager.reopenAuthStage();
    }

    private Pane createPlaceholder(String text) {
        Pane placeholder = new Pane();
        placeholder.setStyle("-fx-background-color: #1A1A1A;");
        javafx.scene.control.Label label = new javafx.scene.control.Label(text + " - Coming Soon");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        label.setLayoutX(50);
        label.setLayoutY(50);
        placeholder.getChildren().add(label);
        return placeholder;
    }
}