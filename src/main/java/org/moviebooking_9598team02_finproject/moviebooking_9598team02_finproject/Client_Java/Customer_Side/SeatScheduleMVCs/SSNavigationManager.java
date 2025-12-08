package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class SSNavigationManager {

    private StackPane StackPane; // The root stack pane that contains all panes
    private Pane seatSelectionPane; // The root pane of the loaded FXML

    private CustomerSeatSelectionController seatSelectionController;
    private CustomerSeatSelectionModel seatSelectionModel;
    private CustomerSeatSelectionView seatSelectionView;

    private int currentCustomerId;
    private int currentScheduleId;

    public SSNavigationManager(StackPane StackPane) {
        this.StackPane = StackPane;
    }

    public SSNavigationManager(StackPane rootStackPane, int customerId, int scheduleId) {
        this.StackPane = rootStackPane;
        this.currentCustomerId = customerId;
        this.currentScheduleId = scheduleId;
    }

    private void initializeSeatSelectionView() throws IOException {
        // Create model
        seatSelectionModel = new CustomerSeatSelectionModel(currentCustomerId, currentScheduleId);

        // Load FXML - View will be injected by FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/CustomerSeatSelection.fxml")
        );

        // Load the FXML (this will create and inject the View)
        seatSelectionPane = loader.load();

        // Get the view instance that was created by FXML
        seatSelectionView = loader.getController();

        // Create controller with model, view, and navigation manager
        seatSelectionController = new CustomerSeatSelectionController(seatSelectionView, seatSelectionModel, this);
        // Manually initialize and load data for the current schedule
        seatSelectionController.init(currentScheduleId);
    }

    public Pane getSeatSelectionView() {
        if (seatSelectionPane == null) {
            try {
                initializeSeatSelectionView();
            } catch (IOException e) {
                e.printStackTrace();
                seatSelectionPane = new Pane(new javafx.scene.control.Label("Error loading Seat Selection View."));
            }
        }
        return seatSelectionPane;
    }

    public void showSeatSelectionView(int customerId, int scheduleId) {
        this.currentCustomerId = customerId;
        this.currentScheduleId = scheduleId;
        
        // Reset pane to force reload with new schedule
        seatSelectionPane = null;
        
        Pane pane = getSeatSelectionView();
        if (StackPane != null) {
            StackPane.getChildren().clear();
            StackPane.getChildren().add(pane);
        }
    }

    // Navigation methods
    public void goBack() {
        // Navigate back to previous view
        // This should be implemented based on your navigation structure
        if (StackPane != null && StackPane.getChildren().size() > 1) {
            StackPane.getChildren().remove(StackPane.getChildren().size() - 1);
        }
    }

    public void goHome() {
        // Navigate to home view
        // This should be implemented based on your navigation structure
        if (StackPane != null) {
            StackPane.getChildren().clear();
            // Add home view here
        }
    }

    public void onBookingSuccess(int bookingId) {
        goHome(); // This could navigate to a confirmation page or back to home
    }

    private StackPane findStackPaneById(javafx.scene.Node node, String id) {
        if (node instanceof StackPane && id.equals(node.getId())) {
            return (StackPane) node;
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                StackPane result = findStackPaneById(child, id);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
