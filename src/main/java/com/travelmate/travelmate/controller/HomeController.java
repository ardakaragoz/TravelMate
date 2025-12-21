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
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {

    // --- 1. CONTAINERS ---
    @FXML private BorderPane mainContainer;
    @FXML private VBox helpPopup;
    @FXML private VBox leaderboardContainer;
    @FXML private VBox tripsContainer;
    @FXML private SidebarController sidebarController;

    // --- 2. SEARCH ---
    @FXML private TextField searchField;

    // --- 3. PROMOTED CITY ---
    @FXML private ImageView promotedCitiesCityImage;
    @FXML private Label promotedCityNameLabel;
    @FXML private Label averageBudgetLabel;
    @FXML private ProgressBar compatibilityScoreBar;
    @FXML private Label compalibilityScoreLabel;

    // Slide Data
    private int currentCityIndex = 0;
    private final List<City> promotedCities = new ArrayList<>();

    // Thread Pool for Background Tasks
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();

    // --- 4. DETAILS POPUP ---
    @FXML private VBox detailsPopup;
    @FXML private Circle detailsProfilePic;
    @FXML private Label detailsOwnerName;
    @FXML private Label detailsDescription;
    @FXML private TextArea messageInputArea;

    private User currentUser;

    public void initialize() throws ExecutionException, InterruptedException {
        System.out.println("Home Page Initializing...");
        currentUser = UserSession.getCurrentUser();

        // 1. Set Sidebar Active State
        if (sidebarController != null) {
            sidebarController.setActivePage("Home");
        }

        // 2. Setup Leaderboard (Fast UI ops)
        setupLeaderboard();

        // --- SLAYT VERİLERİNİ HAZIRLA ---
        promotedCities.add(CityList.getCity("Amsterdam"));
        promotedCities.add(CityList.getCity("Berlin"));
        promotedCities.add(CityList.getCity("Barcelona"));
        promotedCities.add(CityList.getCity("Prague"));
        promotedCities.add(CityList.getCity("London"));

        // 4. Load Promoted City (NOW OPTIMIZED to be Non-Blocking)
        // This runs in background, allowing the scene to show immediately
        loadPromotedCity(currentCityIndex);

        // 5. Load Trips in Background
        if (tripsContainer != null) {
            loadRandomTrips();
        }
    }

    private void loadRandomTrips() {
        // Run in background thread
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Fetch documents

                List<Trip> allTrips = new ArrayList<>();
                for (Trip triplisted : TripList.trips.values()){
                    allTrips.add(triplisted);
                }
                Collections.shuffle(allTrips);
                List<Trip> randomTrips = allTrips.subList(0, Math.min(allTrips.size(), 10));

                for (Trip trip : randomTrips) {
                    // Pre-fetch owner data in background
                    User owner = null;
                    try { owner = UserList.getUser(trip.getUser()); } catch (Exception e) { continue; }

                    final User finalOwner = owner;

                    // Update UI on JavaFX Thread
                    Platform.runLater(() -> {
                        if (finalOwner == null) return;

                        addTripCard(
                                finalOwner.getUsername(),
                                1,
                                finalOwner.getUsername(),
                                trip.getDepartureLocation(),
                                (trip.getDepartureDate() != null ? trip.getDepartureDate().toString() : "TBD"),
                                trip.getDays(),
                                (trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0),
                                trip.getMateCount(),
                                trip.getAverageBudget(),
                                50, // Default compatibility to avoid blocking
                                trip.getDestinationName(),
                                trip.getAdditionalNotes(),
                                finalOwner.getId()
                        );
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, networkExecutor);
    }

    // ==========================================================
    //          PROMOTED CITY SLIDER (OPTIMIZED)
    // ==========================================================

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
        // OPTIMIZATION: Run calculation in background thread
        CompletableFuture.runAsync(() -> {
            City city = promotedCities.get(index);
            int compatibility = 0;
            try {
                if (currentUser != null) {
                    compatibility = currentUser.calculateCompatibility(city);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int finalScore = compatibility;

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> updatePromotedCity(city.getName(), finalScore));
        }, networkExecutor);
    }

    public void updatePromotedCity(String cityName, int score) {
        if (promotedCityNameLabel != null) promotedCityNameLabel.setText(cityName.toUpperCase());
        if (compalibilityScoreLabel != null) compalibilityScoreLabel.setText("%" + score);
        if (compatibilityScoreBar != null) compatibilityScoreBar.setProgress(score / 100.0);

        // This now uses async loading
        setSmartImage(promotedCitiesCityImage, cityName);
    }

    // --- FIX: ASYNCHRONOUS IMAGE LOADING ---
    private void setSmartImage(ImageView targetView, String name) {
        if (targetView == null || name == null) return;

        CompletableFuture.runAsync(() -> {
            String cleanName = name.toLowerCase().replace("ı", "i");
            Image image = null;
            try {
                String path = "/images/city photos/" + cleanName + ".jpg";
                URL url = getClass().getResource(path);

                if (url == null) {
                    path = "/images/city photos/" + cleanName + ".png";
                    url = getClass().getResource(path);
                }

                if (url != null) {
                    // 'true' = Load in Background! This prevents freezing.
                    image = new Image(url.toExternalForm(), true);
                } else {
                    URL fallbackUrl = getClass().getResource("/images/logoBlue.png");
                    if (fallbackUrl != null) {
                        image = new Image(fallbackUrl.toExternalForm(), true);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + name);
            }

            final Image finalImg = image;
            Platform.runLater(() -> {
                if (finalImg != null) {
                    targetView.setImage(finalImg);
                    Rectangle clip = new Rectangle(targetView.getFitWidth(), targetView.getFitHeight());
                    clip.setArcWidth(30.0);
                    clip.setArcHeight(30.0);
                    targetView.setClip(clip);
                }
            });
        }, networkExecutor);
    }

    private void setCircleImage(Circle targetCircle, String name) {
        if (targetCircle == null) return;

        CompletableFuture.runAsync(() -> {
            Image image = null;
            try {
                String cleanName = (name != null) ? name.toLowerCase().replace("ı", "i") : "user_icon";
                String path = "/images/" + cleanName + ".png";
                URL url = getClass().getResource(path);
                if (url == null) url = getClass().getResource("/images/user_icon.png");

                if (url != null) {
                    image = new Image(url.toExternalForm(), true); // Async load
                }
            } catch (Exception e) {}

            final Image finalImg = image;
            Platform.runLater(() -> {
                if (finalImg != null) targetCircle.setFill(new ImagePattern(finalImg));
            });
        }, networkExecutor);
    }

    private void addTripCard(String username, int lvl, String userImg, String from, String date, int days,
                             int found, int totalMate, int budget, int score, String destCity, String description, String ownerID) {
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

        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, ownerID));

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
        // Async Load
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
            System.out.println("Message: " + messageInputArea.getText());
            messageInputArea.clear();
        }
        closeDetailsPopup();
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

    @FXML public void handleHelpButton() {
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        if (helpPopup != null) helpPopup.setVisible(true);
    }
    @FXML public void closeHelpPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (helpPopup != null) helpPopup.setVisible(false);
    }
    private void switchToOtherProfile(javafx.event.ActionEvent event, String userID) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
            javafx.scene.Parent root = loader.load();
            OtherProfileController controller = loader.getController();
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            controller.setProfileData(currentScene, userID);
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.getScene().setRoot(root);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    @FXML public void handleHomeButton(ActionEvent event) {  }
    @FXML public void handleViewProfileButton(ActionEvent event) {}
    @FXML public void handleViewChannelButton(ActionEvent event) {}
    @FXML public void handleViewDetailsButton(ActionEvent event) {}
}