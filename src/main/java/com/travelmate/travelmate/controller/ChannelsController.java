package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChannelsController {

    @FXML private VBox profilePopup;
    @FXML private Circle popupProfileImage;
    @FXML private Label popupProfileName;
    @FXML private Label popupProfileLevel;
    @FXML private Label popupProfileBio;

    @FXML private BorderPane mainContainer;

    @FXML private VBox citySelectionView;
    @FXML private FlowPane cityGrid;
    @FXML private TextField searchField;

    @FXML private VBox channelDetailView;
    @FXML private Label channelTitleLabel;
    @FXML private VBox channelTripsContainer;

    public void initialize() {
        loadCityButtons();
    }
    private void openProfilePopup(String username, String imgName, int lvl) {
        if (profilePopup == null) return;

        popupProfileName.setText(username);
        popupProfileLevel.setText("Lvl. " + lvl);
        popupProfileBio.setText("Hi! I am " + username + ". I love exploring new cities and meeting new people. Let's travel together!");
        setCircleImage(popupProfileImage, imgName);

        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        profilePopup.setVisible(true);
    }

    @FXML
    public void closeProfilePopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (profilePopup != null) profilePopup.setVisible(false);
    }
    private void loadCityButtons() {
        String[] cities = {"London", "Barcelona", "Rio de Janeiro", "Tokyo", "Rome", "New York", "Sydney", "Paris"};

        for (String city : cities) {
            Button btn = new Button(city);
            btn.setPrefSize(180, 100);
            btn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-border-color: #1E3A5F; -fx-border-width: 2; -fx-border-radius: 15; -fx-cursor: hand;");
            btn.setFont(Font.font("System", FontWeight.BOLD, 18));
            btn.setTextFill(Color.web("#1E3A5F"));

            btn.setOnAction(e -> openChannel(city));

            cityGrid.getChildren().add(btn);
        }
    }

    public void openChannel(String cityName) {
        citySelectionView.setVisible(false);
        channelDetailView.setVisible(true);
        channelTitleLabel.setText("Travel Mate " + cityName);

        loadTripsForCity(cityName);
    }

    @FXML
    public void handleBackToSelection() {
        channelDetailView.setVisible(false);
        citySelectionView.setVisible(true);
    }

    private void loadTripsForCity(String city) {
        channelTripsContainer.getChildren().clear();

        if (city.equals("London")) {
            addTripCard("Ahmet Arda Karagöz", 38, "user1", "Kayseri", "12-16 Nov 2025", 4, 0, 2, 68);
            addTripCard("Atakan Polat", 44, "user1", "Kigali", "1-4 July 2026", 4, 1, 2, 38);
        } else {
            addTripCard("User for " + city, 20, "user1", "Istanbul", "10-20 Aug 2025", 10, 1, 3, 85);
        }
    }

    private void addTripCard(String username, int lvl, String userImg, String from, String date, int days,
                             int found, int totalMate, int score) {

        HBox card = new HBox();
        card.setPrefHeight(180);
        card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(5, Color.web("#00000033")));
        card.setPadding(new Insets(15));

        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(25, Color.LIGHTGRAY);
        setCircleImage(profilePic, userImg);

        VBox nameBox = new VBox();
        Label nameLbl = new Label(username);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label lvlLbl = new Label("Lvl. " + lvl);
        lvlLbl.setTextFill(Color.GRAY);
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-cursor: hand;");
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, username, userImg, lvl));

        topRow.getChildren().addAll(profilePic, nameBox, r, viewProfileBtn);

        Label detailLbl = new Label("Departuring from: " + from + "\nDates: " + date);
        detailLbl.setFont(Font.font(14));

        HBox bottomRow = new HBox(20);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label mateLbl = new Label(found + "/" + totalMate + " mate found");
        mateLbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        Region r2 = new Region(); HBox.setHgrow(r2, Priority.ALWAYS);

        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        Label scoreTxt = new Label("Compatibility Score: %" + score);
        ProgressBar pBar = new ProgressBar(score / 100.0);
        pBar.setPrefWidth(100);
        pBar.setStyle("-fx-accent: #1E3A5F;");
        scoreBox.getChildren().addAll(scoreTxt, pBar);

        bottomRow.getChildren().addAll(mateLbl, r2, scoreBox);
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-font-weight: bold;");
        Button chatBtn = new Button("Chat");
        chatBtn.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 15; -fx-background-radius: 15;");

        actionBox.getChildren().addAll(joinBtn, chatBtn);

        infoBox.getChildren().addAll(topRow, detailLbl, bottomRow, actionBox);
        card.getChildren().add(infoBox);

        channelTripsContainer.getChildren().add(card);
    }

    private void setCircleImage(Circle targetCircle, String name) {
        try {
            String path = "/images/" + name + ".png";
            if (getClass().getResource(path) != null) {
                targetCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
            }
        } catch (Exception e) {}
    }
    private void switchToOtherProfile(javafx.event.ActionEvent event, String username, String imgName, int lvl) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
            javafx.scene.Parent root = loader.load();
            OtherProfileController controller = loader.getController();
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            controller.setProfileData(currentScene, username, username, lvl, imgName);
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.getScene().setRoot(root);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}