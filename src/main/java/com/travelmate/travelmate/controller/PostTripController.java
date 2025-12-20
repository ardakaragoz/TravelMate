package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class PostTripController {

    @FXML private TextField destinationField;
    @FXML private Spinner<Integer> daysSpinner;
    @FXML private TextField departureField;
    @FXML private TextField budgetField;
    @FXML private ChoiceBox<String> currencyChoiceBox;
    @FXML private Spinner<Integer> mateCountSpinner;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesArea;
    @FXML private TextField itinerariesField;

    public void initialize() {
        currencyChoiceBox.getItems().addAll("$", "€", "₺", "£");
        currencyChoiceBox.setValue("$");

     //(Gün: 1-365, Arkadaş: 1-10)
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

        if (dest.isEmpty() || from.isEmpty() || date == null) {
            System.out.println("Hata: Lütfen zorunlu alanları doldurun!");
            return;
        }

        Trip trip = new Trip("" + System.currentTimeMillis(), destinationField.getText(), departureField.getText(), days, Integer.parseInt(budgetField.getText()), currencyChoiceBox.getValue(), datePicker.getValue(), datePicker.getValue().plusDays(4), UserSession.getCurrentUser(), itinerary);
    }
}