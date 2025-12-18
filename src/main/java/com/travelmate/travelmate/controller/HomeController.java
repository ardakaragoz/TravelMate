package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private BorderPane mainContainer;
    @FXML private VBox helpPopup;
    @FXML private VBox leaderboardContainer;
    @FXML
    public void initialize() {
        addLeaderboardRow("1. placidezigira", "350");
        addLeaderboardRow("2. ardakaragoz", "320");
        addLeaderboardRow("3. mkeremakturkoglu", "290");
        addLeaderboardRow("4. jhonduran10", "250");
        addLeaderboardRow("5. ismailyuksek", "200");
    }

    private void addLeaderboardRow(String name, String score) {
        HBox row = new HBox();
        row.setSpacing(10);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label scoreLabel = new Label(score);
        scoreLabel.setStyle("-fx-text-fill: #1E3A5F; -fx-font-weight: bold;");
        row.getChildren().addAll(nameLabel, spacer, scoreLabel);
        leaderboardContainer.getChildren().add(row);
    }

    @FXML
    public void handleHelpButton() {
        mainContainer.setEffect(new GaussianBlur(10));
        helpPopup.setVisible(true);
    }

    @FXML
    public void closeHelpPopup() {
        mainContainer.setEffect(null);
        helpPopup.setVisible(false);
    }

    @FXML
    public void handleHomeButton(ActionEvent event) {
        System.out.println("Zaten Ana Sayfadasınız.");
    }
}