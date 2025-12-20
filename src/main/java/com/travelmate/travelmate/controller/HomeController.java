package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
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

import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML private BorderPane mainContainer;
    @FXML private VBox helpPopup;
    @FXML private VBox leaderboardContainer;
    @FXML private VBox tripsContainer;

    @FXML private TextField searchField;

    @FXML private ImageView promotedCitiesCityImage;
    @FXML private Label promotedCityNameLabel;
    @FXML private Label averageBudgetLabel;
    @FXML private ProgressBar compatibilityScoreBar;
    @FXML private Label compalibilityScoreLabel;

    private int currentCityIndex = 0;
    private final List<PromotedCityData> promotedCities = new ArrayList<>();

    @FXML private VBox detailsPopup;
    @FXML private Circle detailsProfilePic;
    @FXML private Label detailsOwnerName;
    @FXML private Label detailsDescription;
    @FXML private TextArea messageInputArea;

    private User currentUser;

    public void initialize() {
        System.out.println("Home Sayfası Başlatılıyor...");
        currentUser = UserSession.getCurrentUser();

        setupLeaderboard();

        promotedCities.add(new PromotedCityData("Rome", 800, 70));
        promotedCities.add(new PromotedCityData("Paris", 1200, 85));
        promotedCities.add(new PromotedCityData("Tokyo", 2500, 90));
        promotedCities.add(new PromotedCityData("Amsterdam", 1100, 82));
        promotedCities.add(new PromotedCityData("Barcelona", 950, 75));

        loadPromotedCity(currentCityIndex);

        if (tripsContainer != null) {
            tripsContainer.getChildren().clear();
            addTripCard("Ahmet Arda", 38, "user1", "Istanbul", "12-01-2026", 4, 0, 2, 500, 68, "Budapest",
                    "Hi, I am looking for two travel mates for my trip to Budapest! I want to stay in a hotel with 3 or 4 stars.");
            addTripCard("Zeynep Kaya", 28, "user1", "Bursa", "10-05-2026", 6, 1, 2, 900, 75, "Rome",
                    "Ciao! Planning a cultural trip to Rome. Pizza, pasta, and history!");
            addTripCard("Mert Demir", 41, "user1", "Izmir", "20-06-2026", 4, 1, 2, 1100, 82, "Amsterdam",
                    "Bisiklet turu ve kanal gezisi planlıyorum. Kafa dengi birini arıyorum.");
        }

        if(searchField != null) {
            searchField.setOnAction(event -> System.out.println("Aranan: " + searchField.getText()));
        }
    }

    @FXML
    public void handleViewDetailsButton(ActionEvent event) {
        openDetailsPopup("Ahmet Arda Karagöz", "user1", "This is a default description for the static card.");
    }

    @FXML
    public void handleNextPromoted(ActionEvent event) {
        currentCityIndex++;
        if (currentCityIndex >= promotedCities.size()) currentCityIndex = 0;
        loadPromotedCity(currentCityIndex);
    }

    @FXML
    public void handlePrevPromoted(ActionEvent event) {
        currentCityIndex--;
        if (currentCityIndex < 0) currentCityIndex = promotedCities.size() - 1;
        loadPromotedCity(currentCityIndex);
    }

    private void loadPromotedCity(int index) {
        PromotedCityData city = promotedCities.get(index);
        updatePromotedCity(city.name, city.budget, city.score);
    }

    private static class PromotedCityData {
        String name; int budget; int score;
        public PromotedCityData(String name, int budget, int score) {
            this.name = name; this.budget = budget; this.score = score;
        }
    }

    public void updatePromotedCity(String cityName, int budget, int score) {
        if (promotedCityNameLabel != null) promotedCityNameLabel.setText(cityName.toUpperCase());
        if (averageBudgetLabel != null) averageBudgetLabel.setText("Average Budget: " + budget + "$");
        if (compalibilityScoreLabel != null) compalibilityScoreLabel.setText("%" + score);
        if (compatibilityScoreBar != null) compatibilityScoreBar.setProgress(score / 100.0);
        setSmartImage(promotedCitiesCityImage, cityName);
    }

    private void addTripCard(String username, int lvl, String userImg, String from, String date, int days,
                             int found, int totalMate, int budget, int score, String destCity, String description) {
        HBox card = new HBox();
        card.setPrefHeight(220); card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(10, Color.web("#00000033")));

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15, 10, 15, 20));
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox topRow = new HBox(10); topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(22, Color.LIGHTGRAY); profilePic.setStroke(Color.BLACK);
        setCircleImage(profilePic, userImg);

        VBox nameBox = new VBox();
        nameBox.getChildren().addAll(createBoldLabel(username, 16), createGrayLabel("Lvl. " + lvl, 14));

        Region r1 = new Region(); HBox.setHgrow(r1, Priority.ALWAYS);
        Button viewProfileBtn = createStyledButton("View Profile", 13);
        topRow.getChildren().addAll(profilePic, nameBox, r1, viewProfileBtn);

        Label infoLbl = new Label("Departuring from: " + from + " at " + date + " for " + days + " Days!");
        infoLbl.setFont(Font.font(16));

        HBox midRow = new HBox(20); midRow.setAlignment(Pos.CENTER_LEFT);
        midRow.getChildren().addAll(createBoldLabel(found + "/" + totalMate + " mate found", 16), new Region(), createBoldLabel(budget + " $", 24));
        ((Region) midRow.getChildren().get(1)).prefWidthProperty().bind(infoBox.widthProperty().divide(3));

        HBox scoreRow = new HBox(10); scoreRow.setAlignment(Pos.CENTER_LEFT);
        ProgressBar pBar = new ProgressBar(score / 100.0); pBar.setPrefWidth(120); pBar.setStyle("-fx-accent: #1E3A5F;");
        scoreRow.getChildren().addAll(new Label("Compatibility Score: %" + score), pBar);

        HBox bottomRow = new HBox(15); bottomRow.setAlignment(Pos.CENTER);
        Button channelBtn = createStyledButton("View Channel", 14);
        Label cityLbl = createBoldLabel(destCity.toUpperCase(), 28); cityLbl.setTextFill(Color.web("#1e3a5f"));
        Button detailsBtn = createStyledButton("View Details", 14);

        detailsBtn.setOnAction(e -> openDetailsPopup(username, userImg, description));

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        bottomRow.getChildren().addAll(channelBtn, s1, cityLbl, s2, detailsBtn);

        infoBox.getChildren().addAll(topRow, infoLbl, midRow, scoreRow, bottomRow);

        StackPane imagePane = new StackPane();
        imagePane.setMinSize(280, 214); imagePane.setMaxSize(280, 214);
        imagePane.setStyle("-fx-background-color: #a4c2f2; -fx-background-radius: 20;");
        ImageView cityImg = new ImageView();
        cityImg.setFitWidth(420); cityImg.setFitHeight(320); cityImg.setPreserveRatio(true);
        setSmartImage(cityImg, destCity);
        Rectangle clip = new Rectangle(280, 214); clip.setArcWidth(20); clip.setArcHeight(20);
        imagePane.setClip(clip);
        imagePane.getChildren().add(cityImg);

        card.getChildren().addAll(infoBox, imagePane);
        if(tripsContainer != null) tripsContainer.getChildren().add(card);
    }

    private Label createBoldLabel(String text, int size) {
        Label l = new Label(text); l.setFont(Font.font("System", FontWeight.BOLD, size)); return l;
    }
    private Label createGrayLabel(String text, int size) {
        Label l = new Label(text); l.setTextFill(Color.web("#5e5e5e")); l.setFont(Font.font("System", javafx.scene.text.FontPosture.ITALIC, size)); return l;
    }
    private Button createStyledButton(String text, int fontSize) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 20;");
        btn.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        return btn;
    }

    private void openDetailsPopup(String ownerName, String imgName, String description) {
        if (detailsPopup == null) return;
        if (detailsOwnerName != null) detailsOwnerName.setText(ownerName);
        if (detailsProfilePic != null) setCircleImage(detailsProfilePic, imgName);
        if (detailsDescription != null) detailsDescription.setText(description);
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        detailsPopup.setVisible(true);
    }

    @FXML public void closeDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (detailsPopup != null) detailsPopup.setVisible(false);
    }

    @FXML public void handleSendRequestButton(ActionEvent event) {
        if(messageInputArea != null) {
            System.out.println("Mesaj: " + messageInputArea.getText());
            messageInputArea.clear();
        }
        closeDetailsPopup();
    }

    private void setSmartImage(ImageView targetView, String name) {
        if (targetView == null) return;
        Image img = findImage(name);
        if (img != null) targetView.setImage(img);
    }

    private void setCircleImage(Circle targetCircle, String name) {
        if (targetCircle == null) return;
        Image img = findImage(name);
        if (img != null) targetCircle.setFill(new ImagePattern(img));
    }

    private Image findImage(String baseName) {
        String[] extensions = {".png", ".jpg", ".jpeg"};
        for (String ext : extensions) {
            try {
                String path = "/images/" + baseName.toLowerCase() + ext;
                if (getClass().getResource(path) != null) return new Image(getClass().getResourceAsStream(path));
            } catch (Exception e) {}
        }
        return null;
    }

    private void setupLeaderboard() {
        if (leaderboardContainer == null) return;
        leaderboardContainer.getChildren().clear();
        addLeaderboardRow("1. placidezigira", "350");
        addLeaderboardRow("2. ardakaragoz", "320");
        addLeaderboardRow("3. mkeremakturkoglu", "290");
        addLeaderboardRow("4. jhonduran10", "250");
        addLeaderboardRow("5. ismailyuksek", "200");
    }

    private void addLeaderboardRow(String name, String score) {
        HBox row = new HBox(); row.setSpacing(10);
        Label nameLabel = new Label(name); nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label scoreLabel = new Label(score); scoreLabel.setStyle("-fx-text-fill: #1E3A5F; -fx-font-weight: bold;");
        row.getChildren().addAll(nameLabel, spacer, scoreLabel);
        leaderboardContainer.getChildren().add(row);
    }

    @FXML public void handleHelpButton() {
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        if (helpPopup != null) helpPopup.setVisible(true);
    }
    @FXML public void closeHelpPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (helpPopup != null) helpPopup.setVisible(false);
    }
    @FXML public void handleHomeButton(ActionEvent event) {
    }
    @FXML public void handleViewProfileButton(ActionEvent event) {}
    @FXML public void handleViewChannelButton(ActionEvent event) {}
}