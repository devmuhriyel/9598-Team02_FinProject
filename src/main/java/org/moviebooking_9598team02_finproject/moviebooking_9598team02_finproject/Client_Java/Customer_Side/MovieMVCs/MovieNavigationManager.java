package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.PaymentMVCs.PaymentView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs.CustomerSeatSelectionController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs.CustomerSeatSelectionModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs.CustomerSeatSelectionView;

import java.io.IOException;

public class MovieNavigationManager {

    private Customer customer;
    private Pane movieBrowsePane;
    private Pane scheduleSelectionPane;
    private Pane seatSelectionPane;

    private CustomerMovieController movieController;
    private CustomerMovieModel movieModel;
    private CustomerMovieView movieView;

    // Seat selection components
    private Stage seatSelectionStage;
    private CustomerSeatSelectionController seatController;
    private CustomerSeatSelectionModel seatModel;
    private CustomerSeatSelectionView seatView;

    public MovieNavigationManager(Customer customer) {
        this.customer = customer;
    }

    /**
     * Initialize and return the movie browse view with MVC wiring
     */
    public Pane getMovieBrowseView() {
        if (movieBrowsePane == null) {
            try {
                System.out.println("🎬 Initializing Movie Browse MVC...");

                // 1. Load FXML FIRST (this creates the View)
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerBrowseMovies.fxml")
                );

                movieBrowsePane = loader.load();
                System.out.println("✅ FXML loaded");

                // 2. Get the View from FXML loader
                movieView = loader.getController();
                System.out.println("✅ View obtained from FXML");

                // 3. Create Model
                movieModel = new CustomerMovieModel();
                System.out.println("✅ Model created");

                // 4. Create Controller and pass View + Model + this manager
                movieController = new CustomerMovieController(movieView, movieModel, this);
                System.out.println("✅ Controller created");

                // 5. NOW initialize the controller (wires buttons + loads data)
                movieController.init();
                System.out.println("✅ Controller initialized - all buttons wired!");

            } catch (IOException e) {
                System.err.println("❌ ERROR loading Movie Browse:");
                e.printStackTrace();
                movieBrowsePane = createErrorPane("Error loading Movie Browse View: " + e.getMessage());
            }
        }
        return movieBrowsePane;
    }

    /**
     * Show schedule selection for a specific movie (currently not used)
     */
    public void showScheduleSelection(int movieId) {
        System.out.println("📅 Showing schedule selection for movie ID: " + movieId);

        // TODO: Implement schedule selection view if needed
        // For now, just show a message
        if (movieView != null) {
            movieView.showStatusMessage("Schedule selection coming soon for movie ID: " + movieId, false);
        }
    }

    /**
     * Show seat selection for a specific schedule - FIXED VERSION
     */
    public void showSeatSelection(int scheduleId) {
        System.out.println("💺 Opening seat selection for schedule ID: " + scheduleId);

        try {
            // Create new stage for seat selection (modal window)
            seatSelectionStage = new Stage();
            seatSelectionStage.setTitle("Select Your Seats - Schedule #" + scheduleId);
            seatSelectionStage.initModality(Modality.APPLICATION_MODAL);

            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerSeatSelection.fxml")
            );

            Pane seatPane = loader.load();
            seatView = loader.getController();

            // Create Model
            seatModel = new CustomerSeatSelectionModel(scheduleId);

            // Create Controller (pass null for navigation manager since we're using modal window)
            seatController = new CustomerSeatSelectionController(seatView, seatModel, this);

            // Initialize controller with schedule data
            seatController.init(scheduleId);

            // Setup stage
            Scene scene = new Scene(seatPane, 1280, 720);
            seatSelectionStage.setScene(scene);
            seatSelectionStage.show();

            System.out.println("✅ Seat selection window opened successfully!");

        } catch (Exception e) {
            System.err.println("❌ ERROR opening seat selection:");
            e.printStackTrace();
            if (movieView != null) {
                movieView.showStatusMessage("Error opening seat selection: " + e.getMessage(), true);
            }
        }
    }

    /**
     * Show payment window after booking
     */
    /**
     * Show payment window after booking
     */
    public void showPayment(int bookingId, double totalAmount) {
        System.out.println("💳 Opening payment for booking ID: " + bookingId);

        try {
            if (seatSelectionStage != null) {
                seatSelectionStage.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Customer/CustomerPayment.fxml"));
            Pane paymentPane = loader.load();

            PaymentView view = loader.getController();
            PaymentModel model = new PaymentModel();
            PaymentController controller = new PaymentController(view, model);

            // ✅ FIXED: Passing 4 Arguments (Matches PaymentController)
            controller.setPaymentData(customer.customerId, customer.fullName, bookingId, totalAmount);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Payment - Booking #" + bookingId);
            stage.setScene(new Scene(paymentPane));
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ ERROR opening payment:");
            e.printStackTrace();
        }
    }

    /**
     * Return to customer home
     */
    public void returnToHome() {
        System.out.println("🏠 Returning to customer home...");
        // This should be handled by CustomerNavigationManager
        // For now, just log
    }

    /**
     * Create an error pane when loading fails
     */
    private Pane createErrorPane(String errorMessage) {
        Pane errorPane = new Pane();
        errorPane.setStyle("-fx-background-color: #1A1A1A;");

        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("❌ " + errorMessage);
        errorLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16px; -fx-font-weight: bold;");
        errorLabel.setLayoutX(50);
        errorLabel.setLayoutY(50);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(600);

        errorPane.getChildren().add(errorLabel);
        return errorPane;
    }

    // ===== GETTERS =====

    public Customer getCustomer() {
        return customer;
    }

    public CustomerMovieController getMovieController() {
        return movieController;
    }

    public CustomerMovieView getMovieView() {
        return movieView;
    }

    public CustomerMovieModel getMovieModel() {
        return movieModel;
    }
}