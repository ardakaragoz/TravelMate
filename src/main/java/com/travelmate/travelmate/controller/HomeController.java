package com.travelmate.travelmate.controller;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.City;
import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.CityList;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private final List<City> promotedCities = new ArrayList<>();


    @FXML private VBox detailsPopup;
    @FXML private Circle detailsProfilePic;
    @FXML private Label detailsOwnerName;
    @FXML private Label detailsDescription;
    @FXML private TextArea messageInputArea;

    @FXML private VBox profilePopup;
    @FXML private Circle popupProfileImage;
    @FXML private Label popupProfileName;
    @FXML private Label popupProfileLevel;
    @FXML private Label popupProfileBio;

    private User currentUser;

    public void initialize() throws ExecutionException, InterruptedException {
        System.out.println("Home Sayfası Başlatılıyor...");
        currentUser = UserSession.getCurrentUser();

        setupLeaderboard();

        // --- SLAYT VERİLERİNİ HAZIRLA ---
        promotedCities.add(CityList.getCity("Amsterdam"));
        promotedCities.add(CityList.getCity("Berlin"));
        promotedCities.add(CityList.getCity("Barcelona"));
        promotedCities.add(CityList.getCity("Prague"));
        promotedCities.add(CityList.getCity("London"));

        // İlk Şehri Yükle (Index 0: Rome)
        loadPromotedCity(currentCityIndex);

        if (tripsContainer != null) {
            loadRandomTrips();
        }
    }

    private void loadRandomTrips() {
        // UI thread'i kilitlememek için arka planda çalıştırıyoruz
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Tüm gezileri çek (Performans notu: Veri çoksa .limit(50) kullanıp içinden 10 seçebilirsin)
                List<QueryDocumentSnapshot> documents = FirestoreClient.getFirestore()
                        .collection("trips")
                        .get().get().getDocuments();

                List<Trip> allTrips = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documents) {
                    Trip trip = new Trip(doc.getId());
                    allTrips.add(trip);
                }

                Collections.shuffle(allTrips);

                List<Trip> randomTrips = allTrips.subList(0, Math.min(allTrips.size(), 10));

                for (Trip trip : randomTrips) {
                    User owner = null;
                    try {
                        owner = trip.getUser();
                    } catch (Exception e) {
                        continue;
                    }

                    final User finalOwner = owner;

                    // 5. UI güncelleme (JavaFX Thread)
                    Platform.runLater(() -> {
                        // Mevcut katılımcı sayısı
                        int foundMates = trip.getMateCount();


                        int compatibility = 50;
                        try {
                            //compatibility = currentUser.calculateCompatibility(new City());
                        } catch (Exception e) {}

                        addTripCard(
                                finalOwner.getUsername(),      // Username
                                1,                             // Level (User modelinde varsa oradan al)
                                finalOwner.getUsername(),      // User Img (İsimden buluyor kodun)
                                trip.getDepartureLocation(),                // Nereden
                                trip.getDepartureDate().toString(),                // Tarih
                                trip.getDays(),                // Gün
                                trip.getJoinedMates().size(),                    // Bulunan
                                foundMates,          // Toplam kişi
                                trip.getAverageBudget(),              // Bütçe
                                compatibility,                 // Score
                                trip.getDestinationName(),         // Hedef Şehir
                                trip.getAdditionalNotes()          // Açıklama
                        );
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    // ==========================================================
    //          PROMOTED CITY SLAYT MANTIĞI 🎠
    // ==========================================================



    private void loadPromotedCity(int index) throws ExecutionException, InterruptedException {
        City city = promotedCities.get(index);
        int compatibility = currentUser.calculateCompatibility(city);
        updatePromotedCity(city.getName(), compatibility);
    }



    // ==========================================================
    //          DİĞER METOTLAR (AYNEN KALIYOR)
    // ==========================================================

    public void updatePromotedCity(String cityName, int score) {
        if (promotedCityNameLabel != null) promotedCityNameLabel.setText(cityName.toUpperCase());
        if (compalibilityScoreLabel != null) compalibilityScoreLabel.setText("%" + score);
        if (compatibilityScoreBar != null) compatibilityScoreBar.setProgress(score / 100.0);
        setSmartImage(promotedCitiesCityImage, cityName);
    }

    private void switchToChannel(javafx.event.ActionEvent event, String cityName) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Channels.fxml"));
            javafx.scene.Parent root = loader.load();
            ChannelsController controller = loader.getController();
            controller.openChannel(cityName);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
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
        nameBox.setMinWidth(Region.USE_PREF_SIZE);
        Label nameLbl = new Label(username);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setTextFill(Color.BLACK);
        Label lvlLbl = new Label("Lvl. " + lvl);
        lvlLbl.setTextFill(Color.web("#5e5e5e"));
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r1 = new Region(); HBox.setHgrow(r1, Priority.ALWAYS);

        Button viewProfileBtn = createStyledButton("View Profile", 13);

        viewProfileBtn.setOnAction(e -> openProfilePopup(username, userImg, lvl));

        topRow.getChildren().addAll(profilePic, nameBox, r1, viewProfileBtn);

        Label infoLbl = new Label("Departuring from: " + from + " at " + date + " for " + days + " Days!");
        infoLbl.setFont(Font.font(16));

        HBox midRow = new HBox(20); midRow.setAlignment(Pos.CENTER_LEFT);
        Label mateLbl = new Label(found + "/" + totalMate + " mate found");
        mateLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label budLbl = new Label(budget + " $");
        budLbl.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region r2 = new Region(); HBox.setHgrow(r2, Priority.ALWAYS);
        midRow.getChildren().addAll(mateLbl, r2, budLbl);

        HBox scoreRow = new HBox(10); scoreRow.setAlignment(Pos.CENTER_LEFT);
        ProgressBar pBar = new ProgressBar(score / 100.0); pBar.setPrefWidth(120); pBar.setStyle("-fx-accent: #1E3A5F;");
        scoreRow.getChildren().addAll(new Label("Compatibility Score: %" + score), pBar);

        HBox bottomRow = new HBox(15); bottomRow.setAlignment(Pos.CENTER);
        Button channelBtn = createStyledButton("View Channel", 14);
        channelBtn.setOnAction(e -> switchToChannel(e, destCity));
        Label cityLbl = new Label(destCity.toUpperCase());
        cityLbl.setFont(Font.font("System", FontWeight.BOLD, 28));
        cityLbl.setTextFill(Color.web("#1e3a5f"));
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

    @FXML public void handleNextPromoted(ActionEvent event) throws ExecutionException, InterruptedException {
        currentCityIndex++;
        if (currentCityIndex >= promotedCities.size()) currentCityIndex = 0;
        loadPromotedCity(currentCityIndex);
    }
    @FXML public void handlePrevPromoted(ActionEvent event) throws ExecutionException, InterruptedException {
        currentCityIndex--;
        if (currentCityIndex < 0) currentCityIndex = promotedCities.size() - 1;
        loadPromotedCity(currentCityIndex);
    }


    public void updatePromotedCity(String cityName, int budget, int score) {
        if (promotedCityNameLabel != null) promotedCityNameLabel.setText(cityName.toUpperCase());
        if (averageBudgetLabel != null) averageBudgetLabel.setText("Average Budget: " + budget + "$");
        if (compalibilityScoreLabel != null) compalibilityScoreLabel.setText("%" + score);
        if (compatibilityScoreBar != null) compatibilityScoreBar.setProgress(score / 100.0);
        setSmartImage(promotedCitiesCityImage, cityName);
    }
    private void openDetailsPopup(String ownerName, String imgName, String description) {
        if (detailsPopup == null) return;
        if (detailsOwnerName != null) detailsOwnerName.setText(ownerName);
        if (detailsProfilePic != null) setCircleImage(detailsProfilePic, imgName);
        if (detailsDescription != null) detailsDescription.setText(description);
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        detailsPopup.setVisible(true);
    }
    @FXML public void handleSendRequestButton(ActionEvent event) {
        if(messageInputArea != null) {
            messageInputArea.clear();
        }
        closeDetailsPopup();
    }
    private Button createStyledButton(String text, int fontSize) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 20;");
        btn.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        return btn;
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
        baseName = baseName.toLowerCase().replaceAll("ı", "i");
            try {
                String path = "/images/city photos/" + baseName.toLowerCase() + ".jpg";
                if (getClass().getResource(path) != null) return new Image(getClass().getResourceAsStream(path));
            } catch (Exception e) {}

        return null;
    }
    private void setupLeaderboard() {
        if (leaderboardContainer == null) return;

        // Yükleniyor... yazısı ekleyebilirsin istersen
        // leaderboardContainer.getChildren().add(new Label("Yükleniyor..."));

        Firestore db = FirebaseService.getFirestore();

        // Veritabanı Sorgusu:
        // 1. "users" koleksiyonuna git
        // 2. "levelPoint" değerine göre AZALAN (En yüksekten en düşüğe) sırala
        // 3. Sadece ilk 5 kişiyi al (Limit) - Performans için çok önemli!
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .orderBy("levelPoint", Query.Direction.DESCENDING)
                .limit(5)
                .get();

        // Asenkron işlem (Arka planda çalışır, arayüzü dondurmaz)
        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // UI güncellemeleri MUTLAKA Platform.runLater içinde olmalıdır
                Platform.runLater(() -> {
                    leaderboardContainer.getChildren().clear(); // Varsa eski listeyi temizle

                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Veriyi güvenli bir şekilde çek
                        String username = doc.getString("username");
                        if (username == null || username.isEmpty()) {
                            username = "Unknown User";
                        }

                        // Puanı güvenli çek (Null check)
                        Long pointsLong = doc.getLong("levelPoint");
                        int points = (pointsLong != null) ? pointsLong.intValue() : 0;

                        // Listeye satır ekle
                        addLeaderboardRow(rank + ". " + username, String.valueOf(points));
                        rank++;
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Leaderboard verisi çekilemedi: " + t.getMessage());
                t.printStackTrace();
            }
        }, Runnable::run);
    }
    private void addLeaderboardRow(String name, String score) {
        HBox row = new HBox(); row.setSpacing(10);
        Label nameLabel = new Label(name); nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label scoreLabel = new Label(score); scoreLabel.setStyle("-fx-text-fill: #1E3A5F; -fx-font-weight: bold;");
        row.getChildren().addAll(nameLabel, spacer, scoreLabel);
        leaderboardContainer.getChildren().add(row);
    }

    public void closeDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (detailsPopup != null) detailsPopup.setVisible(false);
    }
    private void openProfilePopup(String username, String imgName, int lvl) {
        if (profilePopup == null) return;
        popupProfileName.setText(username);
        popupProfileLevel.setText("Lvl. " + lvl);
        popupProfileBio.setText("Hi! I am " + username + ". Let's travel!");
        setCircleImage(popupProfileImage, imgName);
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        profilePopup.setVisible(true);
    }

    @FXML
    public void closeProfilePopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (profilePopup != null) profilePopup.setVisible(false);
    }
    @FXML public void handleHelpButton() {
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        if (helpPopup != null) helpPopup.setVisible(true);
    }
    @FXML public void closeHelpPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (helpPopup != null) helpPopup.setVisible(false);
    }
    @FXML public void handleHomeButton(ActionEvent event) {  }
    @FXML public void handleViewProfileButton(ActionEvent event) {}
    @FXML public void handleViewChannelButton(ActionEvent event) {}
}