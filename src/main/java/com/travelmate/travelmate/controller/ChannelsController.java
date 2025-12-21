package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.ChannelList;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ChannelsController {

    // --- SIDEBAR FIX: Inject the controller ---
    @FXML private SidebarController sidebarController;

    @FXML private VBox profilePopup;
    @FXML private Circle popupProfileImage;
    @FXML private Label popupProfileName;
    @FXML private Label popupProfileLevel;
    @FXML private Label popupProfileBio;

    @FXML private BorderPane mainContainer;

    @FXML private VBox tripDetailsPopup;
    @FXML private Label detailDestinationLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailDescLabel;
    @FXML private Label detailBudgetLabel;
    @FXML private Label detailMatesLabel;
    @FXML private Button popupJoinButton;

    @FXML private VBox citySelectionView;
    @FXML private FlowPane cityGrid;
    @FXML private TextField searchField;

    @FXML private VBox channelDetailView;
    @FXML private Label channelTitleLabel;
    @FXML private VBox channelTripsContainer;

    public void initialize() {
        // --- SIDEBAR FIX: Set Active Page ---
        if (sidebarController != null) {
            sidebarController.setActivePage("Channels");
        }

        loadCityButtons();
    }
    private void openTripDetailsPopup(Trip trip) {
        if (tripDetailsPopup == null) return;

        if (detailDestinationLabel != null) detailDestinationLabel.setText(trip.getDestinationName());
        if (detailDateLabel != null) detailDateLabel.setText(trip.getDepartureDate() != null ? trip.getDepartureDate().toString() : "TBD");
        if (detailBudgetLabel != null) detailBudgetLabel.setText(trip.getAverageBudget() + " $");
        if (detailDescLabel != null) detailDescLabel.setText(trip.getAdditionalNotes() != null ? trip.getAdditionalNotes() : "No description.");
        int currentMates = trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0;
        if (detailMatesLabel != null) detailMatesLabel.setText(currentMates + "/" + trip.getMateCount());
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        tripDetailsPopup.setVisible(true);
        tripDetailsPopup.toFront();
    }

    @FXML
    public void closeTripDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (tripDetailsPopup != null) tripDetailsPopup.setVisible(false);
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
            // Updated style to match your theme more closely if needed, keeping your logic
            btn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-border-color: #1E3A5F; -fx-border-width: 2; -fx-border-radius: 15; -fx-cursor: hand;");
            btn.setFont(Font.font("System", FontWeight.BOLD, 18));
            btn.setTextFill(Color.web("#1E3A5F"));

            btn.setOnAction(e -> {
                try {
                    openChannel(city);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            if (cityGrid != null) {
                cityGrid.getChildren().add(btn);
            }
        }
    }

    public void openChannel(String cityName) throws ExecutionException, InterruptedException {
        if (citySelectionView != null) citySelectionView.setVisible(false);
        if (channelDetailView != null) channelDetailView.setVisible(true);
        if (channelTitleLabel != null) channelTitleLabel.setText("Travel Mate " + cityName);

        loadTripsForCity(cityName);
    }

    @FXML
    public void handleBackToSelection() {
        if (channelDetailView != null) channelDetailView.setVisible(false);
        if (citySelectionView != null) citySelectionView.setVisible(true);
    }
    public void openSpecificChannel(String cityName) throws ExecutionException, InterruptedException {
        if (citySelectionView != null) citySelectionView.setVisible(false);
        if (channelDetailView != null) channelDetailView.setVisible(true);
        if (channelTitleLabel != null) {
            channelTitleLabel.setText(cityName);
        }
        if (channelTripsContainer != null) {
            channelTripsContainer.getChildren().clear();
            loadTripsForCity(cityName);
        }
    }
    private void loadTripsForCity(String city) {
        if (channelTripsContainer == null) return;
        channelTripsContainer.getChildren().clear(); // Önce temizle

        try {
            Channel speChannel = ChannelList.getChannel(city);
            if (speChannel == null && city != null && city.length() > 1) {
                String formattedName = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();
                speChannel = ChannelList.getChannel(formattedName);
            }
            if (speChannel == null) {
                System.out.println("UYARI: " + city + " kanalı bulunamadı veya henüz yüklenmedi.");
                Label emptyLbl = new Label("No trips found for this city yet.");
                emptyLbl.setStyle("-fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold;");
                channelTripsContainer.getChildren().add(emptyLbl);
                return;
            }
            ArrayList<String> reqs = speChannel.getTripRequests();
            if (reqs != null && !reqs.isEmpty()) {
                for (String req : reqs) {
                    Trip t = TripList.getTrip(req);
                    if (t != null) {
                        User u = UserList.getUser(t.getUser());
                        if (u != null) {
                            addTripCard(t, u);
                        }
                    }
                }
                // ...
            } else {
                Label emptyLbl = new Label("No trips active in this channel.");
                emptyLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");
                channelTripsContainer.getChildren().add(emptyLbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Veri yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    private void addTripCard(Trip trip, User owner) {
        HBox card = new HBox();
        card.setPrefHeight(180);
        card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(5, Color.web("#00000033")));
        card.setPadding(new Insets(15));

        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // --- Üst Kısım: Profil Resmi ve İsim ---
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(25, Color.LIGHTGRAY);
        setCircleImage(profilePic, owner.getProfile() != null ? owner.getProfile().getProfilePictureUrl() : null);

        VBox nameBox = new VBox();
        Label nameLbl = new Label(owner.getUsername());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label lvlLbl = new Label("Lvl. " + owner.getLevel());
        lvlLbl.setTextFill(Color.GRAY);
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-cursor: hand;");
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, owner.getId()));

        topRow.getChildren().addAll(profilePic, nameBox, r, viewProfileBtn);

        // --- Orta Kısım: Detaylar ---
        Label detailLbl = new Label("Departuring from: " + trip.getDepartureLocation() + "\nDates: " + trip.getDepartureDate());
        detailLbl.setFont(Font.font(14));

        // --- Alt Kısım: Skor ve Butonlar ---
        HBox bottomRow = new HBox(20);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        int mateCount = trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0;
        Label mateLbl = new Label(mateCount + "/" + trip.getMateCount() + " mates");
        mateLbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        Region r2 = new Region(); HBox.setHgrow(r2, Priority.ALWAYS);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        // **YENİ: View Details Butonu** (Popup'ı açar)
        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: white; -fx-border-color: #1E3A5F; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> openTripDetailsPopup(trip));

        // Join Butonu (Direkt katılmaz, popup açıp oradan katılmak daha mantıklı ama burada da kalabilir)
        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
        // joinBtn.setOnAction(...); // Join mantığı eklenebilir

        actionBox.getChildren().addAll(detailsBtn, joinBtn);

        infoBox.getChildren().addAll(topRow, detailLbl, bottomRow, actionBox);
        bottomRow.getChildren().addAll(mateLbl, r2, actionBox); // Düzenleme: mateLbl ve butonlar aynı satırda olsun diye

        card.getChildren().add(infoBox);
        channelTripsContainer.getChildren().add(card);
    }

    private void setCircleImage(Circle targetCircle, String name) {
        try {
            // Using a generic logic to avoid crashes if image missing
            String path = "/images/" + name + ".png";
            // If user1 is missing, fallback to logo
            if (getClass().getResource(path) == null) {
                path = "/images/logoBlue.png";
            }
            if (getClass().getResource(path) != null) {
                targetCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
            }
        } catch (Exception e) {}
    }
    private void switchToOtherProfile(javafx.event.ActionEvent event, String userID) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
            javafx.scene.Parent root = loader.load();
            OtherProfileController controller = loader.getController();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene currentScene = source.getScene();
            controller.setProfileData(currentScene, userID);
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    public void searchAndSelectCity(String cityName) {
        if (searchField != null) {
            searchField.setText(cityName);
        }
    }
}