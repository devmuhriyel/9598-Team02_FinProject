package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side.SeatScheduleMVCs;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerSeatSelectionView implements Initializable {

    // FXML injected fields
    @FXML private StackPane rootContentStack;
    @FXML private VBox seatView;
    @FXML private GridPane seatGridPane;
    @FXML private Label statusMessageLabel;
    @FXML private Label currentSeatDetailsLabel;
    @FXML private Button cancelButton;
    @FXML private Button homeButton;
    @FXML private Button bookButton;

    // ===== METHODS =====
    public void updateSelectedSeats(List<String> selectedSeats) {
        if (statusMessageLabel == null) return;
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            statusMessageLabel.setText("No seats selected");
        } else {
            statusMessageLabel.setText("Selected: " + String.join(", ", selectedSeats));
        }
    }

    // ===== GETTERS =====
    public VBox getSeatView() {
        return seatView;
    }

    public GridPane getSeatGridPane() {
        return seatGridPane;
    }

    public Label getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public Label getCurrentSeatDetailsLabel() {
        return currentSeatDetailsLabel;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getHomeButton() {
        return homeButton;
    }

    public Button getBookButton() {
        return bookButton;
    }

    // ===== DESIGN HELPERS (seat styles) =====
    public void styleSeatAvailable(Button btn) {
        if (btn == null) return;
        btn.setStyle("-fx-background-color: WHITE; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;");
    }

    public void styleSeatTaken(Button btn) {
        if (btn == null) return;
        btn.setStyle("-fx-background-color: #00CC66; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;");
    }

    public void styleSeatSelected(Button btn) {
        if (btn == null) return;
        btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // FXML injection happens automatically before this method is called
    }
}
