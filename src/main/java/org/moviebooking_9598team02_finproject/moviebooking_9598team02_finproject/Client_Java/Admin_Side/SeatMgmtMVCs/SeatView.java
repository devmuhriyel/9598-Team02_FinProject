package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.SeatMgmtMVCs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatView {
    @FXML
    private Label statusMessageLabel;
    @FXML
    private TextField priceInput;
    @FXML
    private Label currentSeatDetailsLabel;

    @FXML
    private Button btnA1;
    @FXML
    private Button btnA2;
    @FXML
    private Button btnA3;
    @FXML
    private Button btnA4;
    @FXML
    private Button btnA5;
    @FXML
    private Button btnA6;
    @FXML
    private Button btnA7;
    @FXML
    private Button btnA8;
    @FXML
    private Button btnA9;
    @FXML
    private Button btnA10;
    @FXML
    private Button btnA11;

    @FXML
    private Button btnB1;
    @FXML
    private Button btnB2;
    @FXML
    private Button btnB3;
    @FXML
    private Button btnB4;
    @FXML
    private Button btnB5;
    @FXML
    private Button btnB6;
    @FXML
    private Button btnB7;
    @FXML
    private Button btnB8;
    @FXML
    private Button btnB9;
    @FXML
    private Button btnB10;
    @FXML
    private Button btnB11;

    @FXML
    private Button btnC1;
    @FXML
    private Button btnC2;
    @FXML
    private Button btnC3;
    @FXML
    private Button btnC4;
    @FXML
    private Button btnC5;
    @FXML
    private Button btnC6;
    @FXML
    private Button btnC7;
    @FXML
    private Button btnC8;
    @FXML
    private Button btnC9;
    @FXML
    private Button btnC10;
    @FXML
    private Button btnC11;


    @FXML
    private Button btnSetStandard;
    @FXML
    private Button btnSetVIP;
    @FXML
    private Button btnSetMaintenance;
    @FXML
    private Button btnClearSelection;
    @FXML
    private Button btnPriceUpdate;


    private final List<Button> selectedButtons = new ArrayList<>();
    private static final String SELECTED_STYLE = "-fx-border-color: #007ACC; -fx-border-width: 3; -fx-border-radius: 6; -fx-effect: dropshadow(three-pass-box, #007ACC, 10, 0.5, 0, 0);"; //blue border
    private static final String REGULAR_STYLE = "-fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;"; //white
    private static final String VIP_STYLE = "-fx-background-color: #FFD700; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;"; //gold
    private static final String MAINTENANCE_STYLE = "-fx-background-color: #808080; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-min-width: 60;"; //grey

    private final Map<String, Button> seatButtonMap = new HashMap<>();

    private Runnable standardTypeAction;
    private Runnable vipTypeAction;
    private Runnable maintenanceAction;
    private Runnable clearSelectionAction;
    private EventHandler<ActionEvent> priceUpdateAction;

    @FXML
    public void initialize() {
        for (Button button : getAllSeatButtons()) {
            if (button != null) {
                seatButtonMap.put(button.getText(), button);
            }
        }
    }

    // ==== GETTERS ====
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

    public Label getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public Label getCurrentSeatDetailsLabel() {
        return currentSeatDetailsLabel;
    }

    public TextField getPriceInput() {
        return priceInput;
    }

    public Button getSeatButton(String seatName) {
        return seatButtonMap.get(seatName);
    }

    // ==== SETTERS ====
    public void setStandardType(Runnable action) {
        this.standardTypeAction = action;
        if (btnSetStandard != null) btnSetStandard.setOnAction(e -> action.run());
    }

    public void setVipType(Runnable action) {
        this.vipTypeAction = action;
        if (btnSetVIP != null) btnSetVIP.setOnAction(e -> action.run());
    }

    public void setMaintenance(Runnable action) {
        this.maintenanceAction = action;
        if (btnSetMaintenance != null) btnSetMaintenance.setOnAction(e -> action.run());
    }

    public void setClearSelection(Runnable action) {
        this.clearSelectionAction = action;
        if (btnClearSelection != null) btnClearSelection.setOnAction(e -> action.run());
    }

    public void setPriceUpdateClick(EventHandler<ActionEvent> action) {
        this.priceUpdateAction = action;
        if (btnPriceUpdate != null) btnPriceUpdate.setOnAction(action);
    }

    //For Styles
    public void setSeatSelected(Button button) {
        button.setStyle(button.getStyle() + " " + SELECTED_STYLE);
        selectedButtons.add(button);
    }

    public void deselectSeat(Button button) {
        String newStyle = button.getStyle().replace(SELECTED_STYLE, "");
        button.setStyle(newStyle.trim());
        selectedButtons.remove(button);
    }

    public void updateSeatStyle(String seatName, String seatType) {
        Button button = seatButtonMap.get(seatName);
        if (button == null) return;
        boolean isSelected = button.getStyle().contains(SELECTED_STYLE);
        String cleanStyle = button.getStyle()
                .replace(SELECTED_STYLE, "")
                .replace(REGULAR_STYLE, "").replace(VIP_STYLE, "").replace(MAINTENANCE_STYLE, "").trim();

        String newTypeStyle;

        if ("vip".equalsIgnoreCase(seatType)) {
            newTypeStyle = VIP_STYLE;
        } else if ("maintenance".equalsIgnoreCase(seatType)) {
            newTypeStyle = MAINTENANCE_STYLE;
        } else if ("regular".equalsIgnoreCase(seatType)) {
            newTypeStyle = REGULAR_STYLE;
        } else {
            newTypeStyle = REGULAR_STYLE;
        }
        button.setStyle(cleanStyle + " " + newTypeStyle);
        if (isSelected) {
            setSeatSelected(button);
        }
    }
}