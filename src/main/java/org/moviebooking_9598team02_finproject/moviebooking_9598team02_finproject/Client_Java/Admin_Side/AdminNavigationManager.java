package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Admin;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AuthenticationMVCs.AdminAuthNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.BookingsMVCs.AdminBookingNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.MovieMVCs.AdminMMNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs.AdminSMNavManager;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingAdmin.AdminService;


import java.io.IOException;

public class AdminNavigationManager {
    private Stage homeStage;
    private AdminAuthNavManager authNavManager;
    private Admin admin;

    private Pane homePane;
    private AdminHomeController homeController;
    private AdminHomeView homeView;

    // Cache for main section panes
    private Pane dashboardPane;
    private Pane movieMgmtPane;
    private Pane seatMgmtPane;
    private Pane bookingMgmtPane;
    private Pane customerMgmtPane;
    private Pane genreMgmtPane;

    // Sub-navigation managers
    private AdminBookingNavManager bookingNavManager;
    private AdminMMNavManager movieNavManager;
    private AdminSMNavManager seatNavManager;

    // Add others as needed
    private AdminService adminService;

    public AdminNavigationManager(Stage homeStage, AdminAuthNavManager authNavManager, Admin admin) {
        this.homeStage = homeStage;
        this.authNavManager = authNavManager;
        this.admin = admin;
        this.adminService = adminService;

        this.seatNavManager = new AdminSMNavManager(adminService, admin);

        initializeHomeView();
    }

    private void initializeHomeView() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminHome.fxml"));
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

    // Methods called by AdminHomeController to load main sections
    public void showDashboardView() {
        if (dashboardPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminDashboard.fxml"));
                dashboardPane = loader.load();
                // Initialize controller if needed
            } catch (IOException e) {
                e.printStackTrace();
                dashboardPane = createPlaceholder("Dashboard");
            }
        }
        loadContentPane(dashboardPane);
    }

    public void showMovieMgmtView() {
        if (movieMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminMovieMgmt.fxml"));
                movieMgmtPane = loader.load();

                // Initialize movie nav manager if this section has sub-views
                StackPane movieContainer = (StackPane) movieMgmtPane.lookup("#movieSubContainer");
                if (movieContainer != null) {
                    //Put stack pane of Movie Mgmt MVCs here
//                    movieNavManager = new AdminMMNavManager(movieContainer);
                }

            } catch (IOException e) {
                e.printStackTrace();
                movieMgmtPane = createPlaceholder("Movie Management");
            }
        }
        loadContentPane(movieMgmtPane);
    }

    public void showSeatMgmtView() {
        Pane seatViewContent = seatNavManager.getSeatMgmtView();
        loadContentPane(seatViewContent);
    }

    public void showBookingMgmtView() {
        if (bookingMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminBookingMgmt.fxml"));
                bookingMgmtPane = loader.load();

                // Initialize booking nav manager if this section has sub-views
                StackPane bookingContainer = (StackPane) bookingMgmtPane.lookup("#bookingSubContainer");
                if (bookingContainer != null) {
                    //Put stack pane of Booking MVCs here
//                    bookingNavManager = new AdminBookingNavManager(bookingContainer);
                }

            } catch (IOException e) {
                e.printStackTrace();
                bookingMgmtPane = createPlaceholder("Booking Management");
            }
        }
        loadContentPane(bookingMgmtPane);
    }

    public void showCustomerMgmtView() {
        if (customerMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/AdminCustomerMgmt.fxml"));
                customerMgmtPane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                customerMgmtPane = createPlaceholder("Customer Management");
            }
        }
        loadContentPane(customerMgmtPane);
    }

    public void showGenreMgmtView(){
        if (genreMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/GenreManagement.fxml"));
                genreMgmtPane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                genreMgmtPane = createPlaceholder("Genre Management");
            }
        }
        loadContentPane(genreMgmtPane);
    }

    private void loadContentPane(Pane pane) {
        StackPane container = homeView.getPanesContainer();
        container.getChildren().clear();
        container.getChildren().add(pane);
    }

    public void logoutAndReturnToAuth() {
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