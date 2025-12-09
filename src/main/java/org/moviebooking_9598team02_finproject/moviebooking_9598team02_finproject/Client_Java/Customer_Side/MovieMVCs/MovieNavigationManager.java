package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.MovieMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Customer;

import java.io.IOException;

public class MovieNavigationManager {

    private final int currentCustomerId;
    private Pane movieBrowsePane; // The root pane of the loaded FXML

    private CustomerMovieController movieController;
    private CustomerMovieModel movieModel;
    private CustomerMovieView movieView;

    // Reference to parent navigation (if needed)
    private Object parentNavigationManager;

    public MovieNavigationManager(Customer customer) {
        this.currentCustomerId = customer.customerId;
    }

    public MovieNavigationManager(int customerId) {
        this.currentCustomerId = customerId;
    }

    private void initializeMovieBrowseView() throws IOException {
        movieModel = new CustomerMovieModel(currentCustomerId);
        movieView = new CustomerMovieView();
        movieController = new CustomerMovieController(movieView, movieModel, this);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerMovieBrowse.fxml")
        );

        loader.setController(movieView);
        movieBrowsePane = loader.load();

        // Initialize the controller after the FXML is loaded
        movieController.init();
    }

    public Pane getMovieBrowseView() {
        if (movieBrowsePane == null) {
            try {
                initializeMovieBrowseView();
            } catch (IOException e) {
                e.printStackTrace();
                movieBrowsePane = new Pane(new javafx.scene.control.Label("Error loading Movie Browse View."));
            }
        }
        return movieBrowsePane;
    }

    // ===== NAVIGATION METHODS =====

    /**
     * Navigate to schedule selection for a specific movie
     */
    public void showScheduleSelection(int movieId) {
        // This would navigate to the schedule/seat selection view
        // Implementation depends on your application structure
        System.out.println("Navigating to schedule selection for movie ID: " + movieId);

        // Example: If you have a SSNavigationManager for schedule/seat selection
        // ssNavigationManager.showScheduleForMovie(movieId);
    }

    /**
     * Go back to previous view
     */
    public void goBack() {
        // Implementation depends on your navigation stack
        System.out.println("Going back to previous view");

        // Example: If you have a parent navigation manager
        // if (parentNavigationManager != null) {
        //     parentNavigationManager.goToPreviousView();
        // }
    }

    /**
     * Go to home/main menu
     */
    public void goHome() {
        // Implementation depends on your application structure
        System.out.println("Going to home view");

        // Example: If you have a main navigation manager
        // if (parentNavigationManager != null) {
        //     parentNavigationManager.showHomeView();
        // }
    }

    // ===== SETTERS FOR PARENT NAVIGATION =====

    public void setParentNavigationManager(Object parentNavManager) {
        this.parentNavigationManager = parentNavManager;
    }

    // ===== GETTERS =====

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