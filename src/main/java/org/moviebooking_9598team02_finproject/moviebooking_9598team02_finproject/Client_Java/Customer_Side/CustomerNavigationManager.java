package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.AuthenticationMVCs.CustomerAuthNavManager;

import java.io.IOException;

public class CustomerNavigationManager {
    private Stage homeStage;
    private CustomerAuthNavManager authNavManager;
    private Customer customer;

    private Pane homePane;
    private CustomerHomeController homeController;
    private CustomerHomeView homeView;

    // Cache for main section panes
    private Pane dashboardPane;
    private Pane movieMgmtPane;
    private Pane seatMgmtPane;
    private Pane bookingMgmtPane;
    private Pane customerMgmtPane;

    // Sub-navigation managers
    // Add others as needed

    public CustomerNavigationManager(Stage homeStage, CustomerAuthNavManager authNavManager, Customer customer) {
        this.homeStage = homeStage;
        this.authNavManager = authNavManager;
        this.customer = customer;
        CustomerParentModel.initializeCORBAConnection(new String[]{});

        initializeHomeView();
    }

    private void initializeHomeView() {
        try {
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerHome.fxml"));
            homePane = homeLoader.load();
            homeView = homeLoader.getController();
            CustomerHomeModel homeModel = new CustomerHomeModel();
            homeController = new CustomerHomeController(homeView, homeModel, this, customer);

            Scene homeScene = new Scene(homePane, 1280, 720);
            homeStage.setScene(homeScene);
            homeStage.setTitle("FilmBook Customer - " + customer.username);
            homeStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods called by CustomerHomeController to load main sections
    public void showDashboardView() {
        if (dashboardPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerDashboard.fxml"));
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerMovieMgmt.fxml"));
                movieMgmtPane = loader.load();

                // Initialize movie nav manager if this section has sub-views
                StackPane movieContainer = (StackPane) movieMgmtPane.lookup("#movieSubContainer");
                if (movieContainer != null) {
                    //Put stack pane of Movie Mgmt MVCs here
                }

            } catch (IOException e) {
                e.printStackTrace();
                movieMgmtPane = createPlaceholder("Movie Management");
            }
        }
        loadContentPane(movieMgmtPane);
    }

    public void showSeatMgmtView() {
        if (seatMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerSeatMgmt.fxml"));
                seatMgmtPane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                seatMgmtPane = createPlaceholder("Seat Management");
            }
        }
        loadContentPane(seatMgmtPane);
    }

    public void showBookingMgmtView() {
        if (bookingMgmtPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerBookingMgmt.fxml"));
                bookingMgmtPane = loader.load();

                // Initialize booking nav manager if this section has sub-views
                StackPane bookingContainer = (StackPane) bookingMgmtPane.lookup("#bookingSubContainer");
                if (bookingContainer != null) {
                    //Put stack pane of Booking MVCs here
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerCustomerMgmt.fxml"));
                customerMgmtPane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                customerMgmtPane = createPlaceholder("Customer Management");
            }
        }
        loadContentPane(customerMgmtPane);
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