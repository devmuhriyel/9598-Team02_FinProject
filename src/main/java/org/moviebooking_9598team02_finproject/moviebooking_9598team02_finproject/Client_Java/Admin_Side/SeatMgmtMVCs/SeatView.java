package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;

public class SeatView {

    @FXML private GridPane seatGridPane;
    @FXML private Label statusMessageLabel;
    @FXML private TextField priceInput;

    // Row A
    @FXML private Button btnA1; @FXML private Button btnA2; @FXML private Button btnA3;
    @FXML private Button btnA4; @FXML private Button btnA5; @FXML private Button btnA6;
    @FXML private Button btnA7; @FXML private Button btnA8; @FXML private Button btnA9;
    @FXML private Button btnA10; @FXML private Button btnA11;

    // Row B
    @FXML private Button btnB1; @FXML private Button btnB2; @FXML private Button btnB3;
    @FXML private Button btnB4; @FXML private Button btnB5; @FXML private Button btnB6;
    @FXML private Button btnB7; @FXML private Button btnB8; @FXML private Button btnB9;
    @FXML private Button btnB10; @FXML private Button btnB11;

    // Row C
    @FXML private Button btnC1; @FXML private Button btnC2; @FXML private Button btnC3;
    @FXML private Button btnC4; @FXML private Button btnC5; @FXML private Button btnC6;
    @FXML private Button btnC7; @FXML private Button btnC8; @FXML private Button btnC9;
    @FXML private Button btnC10; @FXML private Button btnC11;

    // ===== GETTERS =====

    public List<Button> getAllSeatButtons() {
        List<Button> buttons = new ArrayList<>();
        Button[] allButtons = {
                btnA1, btnA2, btnA3, btnA4, btnA5, btnA6, btnA7, btnA8, btnA9, btnA10, btnA11,
                btnB1, btnB2, btnB3, btnB4, btnB5, btnB6, btnB7, btnB8, btnB9, btnB10, btnB11,
                btnC1, btnC2, btnC3, btnC4, btnC5, btnC6, btnC7, btnC8, btnC9, btnC10, btnC11
        };
        for (Button button : allButtons) {
            if (button != null) {
                buttons.add(button);
            }
        }
        return buttons;
    }

    //Row A
    public Button getBtnA1() { return btnA1; }
    public Button getBtnA2() { return btnA2; }
    public Button getBtnA3() { return btnA3; }
    public Button getBtnA4() { return btnA4; }
    public Button getBtnA5() { return btnA5; }
    public Button getBtnA6() { return btnA6; }
    public Button getBtnA7() { return btnA7; }
    public Button getBtnA8() { return btnA8; }
    public Button getBtnA9() { return btnA9; }
    public Button getBtnA10() { return btnA10; }
    public Button getBtnA11() { return btnA11; }

    //Row B
    public Button getBtnB1() { return btnB1; }
    public Button getBtnB2() { return btnB2; }
    public Button getBtnB3() { return btnB3; }
    public Button getBtnB4() { return btnB4; }
    public Button getBtnB5() { return btnB5; }
    public Button getBtnB6() { return btnB6; }
    public Button getBtnB7() { return btnB7; }
    public Button getBtnB8() { return btnB8; }
    public Button getBtnB9() { return btnB9; }
    public Button getBtnB10() { return btnB10; }
    public Button getBtnB11() { return btnB11; }

    //Row C
    public Button getBtnC1() { return btnC1; }
    public Button getBtnC2() { return btnC2; }
    public Button getBtnC3() { return btnC3; }
    public Button getBtnC4() { return btnC4; }
    public Button getBtnC5() { return btnC5; }
    public Button getBtnC6() { return btnC6; }
    public Button getBtnC7() { return btnC7; }
    public Button getBtnC8() { return btnC8; }
    public Button getBtnC9() { return btnC9; }
    public Button getBtnC10() { return btnC10; }
    public Button getBtnC11() { return btnC11; }

    public GridPane getSeatGridPane() { return seatGridPane; }
    public Label getStatusMessageLabel() { return statusMessageLabel; }
    public TextField getPriceInput() { return priceInput; }

    // ===== METHODS FOR CONTROLLER TO UPDATE THE UI =====

    public void updateSeatVisual(Button button, String styleString) {
        if (button != null) {
            button.setStyle(styleString);
        }
    }

    public void showStatusMessage(String message, boolean isError) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle(isError ? "-fx-text-fill: #ff2525; -fx-font-weight: bold;" : "-fx-text-fill: #00CC66;");
            statusMessageLabel.setVisible(true);
        }
    }

    public void clearStatusMessage() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
            statusMessageLabel.setVisible(false);
        }
    }
}