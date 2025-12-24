package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class PostTripController {
    @FXML private Label statusLabel;
    @FXML private TextField destinationField;
    @FXML private Spinner<Integer> daysSpinner;
    @FXML private TextField departureField;
    @FXML private TextField budgetField;
    @FXML private ChoiceBox<String> currencyChoiceBox;
    @FXML private Spinner<Integer> mateCountSpinner;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesArea;
    @FXML private TextField itinerariesField;
    @FXML private SidebarController sidebarController;

    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActivePage("PostTrip");
        }

        currencyChoiceBox.getItems().addAll("$", "€", "₺", "£");
        currencyChoiceBox.setValue("$");

        daysSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 4));
        mateCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
    }

    @FXML
    public void handleSubmit(ActionEvent event) throws ExecutionException, InterruptedException {
        String dest = destinationField.getText();
        Integer days = daysSpinner.getValue();
        String from = departureField.getText();
        String budget = budgetField.getText() + " " + currencyChoiceBox.getValue();
        Integer mates = mateCountSpinner.getValue();
        LocalDate date = datePicker.getValue();
        String notes = notesArea.getText();
        String itinerary = itinerariesField.getText();
            if (!destinationField.getText().isEmpty()) {
                statusLabel.setText("Trip Request Created Successfully!");
                statusLabel.setTextFill(Color.web("#1E3A5F"));
                statusLabel.setVisible(true);
            } else {
                statusLabel.setText("Please fill in the destination!");
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
            }

        if (dest.isEmpty() || from.isEmpty() || date == null) {
            return;
        }
        LocalDate endDate = datePicker.getValue().plusDays(days);
        Date endDate_ = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String id = "" + System.currentTimeMillis() + "-" + endDate_.getTime();
        Trip trip = new Trip(id, destinationField.getText(), departureField.getText(), days, Integer.parseInt(budgetField.getText()), currencyChoiceBox.getValue(), datePicker.getValue(), endDate, UserSession.getCurrentUser().getId(), itinerary, mates, notes);

    }
}