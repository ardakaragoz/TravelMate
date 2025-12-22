package com.travelmate.travelmate.controller;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.City;
import com.travelmate.travelmate.model.JoinRequest;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {


    @FXML private BorderPane mainContainer;
    @FXML private VBox helpPopup;
    @FXML private VBox leaderboardContainer;
    @FXML private VBox tripsContainer;
    @FXML private SidebarController sidebarController;
    @FXML private TextField searchField;
    @FXML private VBox filterPopup;
    @FXML private Slider budgetSlider;
    @FXML private Slider daysSlider;
    @FXML private DatePicker filterStartDate;
    @FXML private DatePicker filterEndDate;

    @FXML private ImageView promotedCitiesCityImage;
    @FXML private Label promotedCityNameLabel;
    @FXML private Label averageBudgetLabel;
    @FXML private ProgressBar compatibilityScoreBar;
    @FXML private Label compalibilityScoreLabel;

    // --- DETAILS POPUP ---
    @FXML private VBox detailsPopup;
    @FXML private Circle detailsProfilePic;
    @FXML private Label detailsOwnerName;
    @FXML private Label detailsDescription;
    @FXML private TextArea messageInputArea;

    // --- NEW: Elements for handling own trip logic ---
    @FXML private Label ownTripLabel;
    @FXML private Button sendRequestBtn;

    private int currentCityIndex = 0;
    private final List<City> promotedCities = new ArrayList<>();
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private User currentUser;

    private Trip selectedTripForDetails;
    private User selectedTripOwnerForDetails;

    public void initialize() throws ExecutionException, InterruptedException {
        currentUser = UserSession.getCurrentUser();
        if (sidebarController != null) sidebarController.setActivePage("Home");

        setupLeaderboard();

        promotedCities.add(CityList.getCity("Amsterdam"));
        promotedCities.add(CityList.getCity("Dubai"));
        promotedCities.add(CityList.getCity("Barcelona"));
        promotedCities.add(CityList.getCity("Prague"));
        promotedCities.add(CityList.getCity("London"));

        loadPromotedCity(currentCityIndex);

        if (tripsContainer != null) loadRandomTrips();

        promotedCityNameLabel.setOnMouseClicked(event -> {
            String city = promotedCityNameLabel.getText();
            if (city != null && !city.isEmpty()) openChannelPage(event, city);
        });
        promotedCityNameLabel.setStyle("-fx-cursor: hand; -fx-background-color: #253A63; -fx-background-radius: 10; -fx-padding: 5 20 5 20; -fx-text-fill: #CCFF00;");
    }
    @FXML
    public void handleOpenFilters() {
        if (filterPopup != null) {
            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            filterPopup.setVisible(true);
            filterPopup.toFront();
        }
    }

    @FXML
    public void closeFilterPopup() {
        // Bulanıklığı kaldır ve kapat
        if (mainContainer != null) mainContainer.setEffect(null);
        if (filterPopup != null) filterPopup.setVisible(false);
    }

    @FXML
    public void applyFilters() {
        double maxBudget = (budgetSlider != null) ? budgetSlider.getValue() : 0;
        double days = (daysSlider != null) ? daysSlider.getValue() : 0;

        System.out.println("Filtre Uygulandı -> Bütçe: " + maxBudget + ", Gün: " + days);
        closeFilterPopup();

        // 3. İleride buraya listeyi filtreleme kodunu ekleyeceğiz:
        // loadTripsWithFilter(maxBudget, days, ...);
    }
    private void loadRandomTrips() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Trip> allTrips = new ArrayList<>();
                for (String tripID : TripList.trips.keySet()){
                    if (!TripList.getTrip(tripID).isFinished() && !TripList.getTrip(tripID).getUser().equals(currentUser.getId())) allTrips.add(TripList.getTrip(tripID));
                }
                Collections.shuffle(allTrips);
                List<Trip> randomTrips = allTrips.subList(0, Math.min(allTrips.size(), 10));

                for (Trip trip : randomTrips) {
                    User owner = null;
                    try { owner = UserList.getUser(trip.getUser()); } catch (Exception e) { continue; }
                    final User finalOwner = owner;

                    Platform.runLater(() -> {
                        if (finalOwner != null) addTripCard(trip, finalOwner);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, networkExecutor);
    }

    private void addTripCard(Trip trip, User owner) {
        HBox card = new HBox();
        card.setPrefHeight(220); card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(10, Color.web("#00000033")));

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15, 10, 15, 20));
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox topRow = new HBox(10); topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(22, Color.LIGHTGRAY); profilePic.setStroke(Color.BLACK);
        setCircleImage(profilePic, owner.getUsername());

        VBox nameBox = new VBox();
        nameBox.getChildren().addAll(createBoldLabel(owner.getUsername(), 16), createGrayLabel("Lvl. " + owner.getLevel(), 14));

        Region r1 = new Region(); HBox.setHgrow(r1, Priority.ALWAYS);
        Button viewProfileBtn = createStyledButton("View Profile", 13);
        addClickEffect(viewProfileBtn);
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, owner.getId()));

        topRow.getChildren().addAll(profilePic, nameBox, r1, viewProfileBtn);

        String dateStr = (trip.getDepartureDate() != null ? trip.getDepartureDate().toString() : "TBD");
        Label infoLbl = new Label("Departuring from: " + trip.getDepartureLocation() + " at " + dateStr + " for " + trip.getDays() + " Days!");
        infoLbl.setFont(Font.font(16));

        int joined = (trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0);
        HBox midRow = new HBox(20); midRow.setAlignment(Pos.CENTER_LEFT);
        midRow.getChildren().addAll(createBoldLabel(joined + "/" + trip.getMateCount() + " mate found", 16), new Region(), createBoldLabel(trip.getAverageBudget() + " " + trip.getCurrency(), 24));
        ((Region) midRow.getChildren().get(1)).prefWidthProperty().bind(infoBox.widthProperty().divide(3));

        HBox scoreRow = new HBox(10); scoreRow.setAlignment(Pos.CENTER_LEFT);
        ProgressBar pBar = new ProgressBar(0.5); pBar.setPrefWidth(120); pBar.setStyle("-fx-accent: #1E3A5F;");
        scoreRow.getChildren().addAll(new Label("Compatibility Score: %50"), pBar);

        HBox bottomRow = new HBox(15); bottomRow.setAlignment(Pos.CENTER);
        Button viewChannelBtn = createStyledButton("View Channel", 14);
        addClickEffect(viewChannelBtn);
        viewChannelBtn.setOnAction(e -> {
            if (trip.getDestinationName() != null && !trip.getDestinationName().isEmpty()) openChannelPage(e, trip.getDestinationName());
        });
        Label cityLbl = createBoldLabel(trip.getDestinationName().toUpperCase(), 28); cityLbl.setTextFill(Color.web("#1e3a5f"));
        Button detailsBtn = createStyledButton("View Details", 14);
        addClickEffect(detailsBtn);
        detailsBtn.setOnAction(e -> openDetailsPopup(trip, owner));

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        bottomRow.getChildren().addAll(viewChannelBtn, s1, cityLbl, s2, detailsBtn);

        infoBox.getChildren().addAll(topRow, infoLbl, midRow, scoreRow, bottomRow);

        StackPane imagePane = new StackPane();
        imagePane.setMinSize(280, 214); imagePane.setMaxSize(280, 214);
        imagePane.setStyle("-fx-background-color: #a4c2f2; -fx-background-radius: 20;");

        ImageView cityImg = new ImageView();
        cityImg.setFitWidth(420); cityImg.setFitHeight(320); cityImg.setPreserveRatio(true);
        setSmartImage(cityImg, trip.getDestinationName());

        Rectangle clip = new Rectangle(280, 214); clip.setArcWidth(20); clip.setArcHeight(20);
        imagePane.setClip(clip);
        imagePane.getChildren().add(cityImg);

        card.getChildren().addAll(infoBox, imagePane);
        if(tripsContainer != null) tripsContainer.getChildren().add(card);
    }

    private void openDetailsPopup(Trip trip, User owner) {
        if (detailsPopup == null) return;

        this.selectedTripForDetails = trip;
        this.selectedTripOwnerForDetails = owner;

        if (detailsOwnerName != null) detailsOwnerName.setText(owner.getUsername());
        if (detailsProfilePic != null) setCircleImage(detailsProfilePic, owner.getUsername());
        if (detailsDescription != null) detailsDescription.setText(trip.getAdditionalNotes());

        // --- LOGIC: Check if it's my own trip ---
        boolean isMyTrip = currentUser != null && currentUser.getId().equals(owner.getId());

        if (sendRequestBtn != null) {
            sendRequestBtn.setVisible(!isMyTrip);
            sendRequestBtn.setManaged(!isMyTrip);
        }

        if (ownTripLabel != null) {
            ownTripLabel.setVisible(isMyTrip);
            ownTripLabel.setManaged(isMyTrip);
        }

        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        detailsPopup.setVisible(true);
    }

    @FXML public void closeDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (detailsPopup != null) detailsPopup.setVisible(false);
    }

    @FXML public void handleSendRequestButton(ActionEvent event) {
        String message = (messageInputArea != null && !messageInputArea.getText().isEmpty())
                ? messageInputArea.getText()
                : "Hi! I'd like to join your trip.";

        if (selectedTripForDetails != null && selectedTripOwnerForDetails != null && currentUser != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String reqId = UUID.randomUUID().toString();
                    JoinRequest req = new JoinRequest(reqId, currentUser, selectedTripOwnerForDetails, message, selectedTripForDetails);
                    currentUser.addRequest(req);
                    selectedTripForDetails.addPendingMate(currentUser);

                    Platform.runLater(() -> {
                        if(messageInputArea != null) messageInputArea.clear();
                        closeDetailsPopup();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, networkExecutor);
        } else {
            closeDetailsPopup();
        }
    }

    private void setupLeaderboard() {
        if (leaderboardContainer == null) return;
        Firestore db = FirebaseService.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .orderBy("levelPoint", Query.Direction.DESCENDING)
                .limit(5)
                .get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Platform.runLater(() -> {
                    leaderboardContainer.getChildren().clear();
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String username = doc.getString("username");
                        if (username == null || username.isEmpty()) username = "Unknown User";
                        Long pointsLong = doc.getLong("levelPoint");
                        int points = (pointsLong != null) ? pointsLong.intValue() : 0;
                        addLeaderboardRow(rank + ". " + username, String.valueOf(points));
                        rank++;
                    }
                });
            }
            @Override public void onFailure(Throwable t) {}
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

    private Label createBoldLabel(String text, int size) { Label l = new Label(text); l.setFont(Font.font("System", FontWeight.BOLD, size)); return l; }
    private Label createGrayLabel(String text, int size) { Label l = new Label(text); l.setTextFill(Color.web("#5e5e5e")); l.setFont(Font.font("System", javafx.scene.text.FontPosture.ITALIC, size)); return l; }
    private Button createStyledButton(String text, int fontSize) { Button btn = new Button(text); btn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 20;"); btn.setFont(Font.font("System", FontWeight.BOLD, fontSize)); return btn; }

    @FXML public void handleHelpButton() { if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10)); if (helpPopup != null) helpPopup.setVisible(true); }
    @FXML public void closeHelpPopup() { if (mainContainer != null) mainContainer.setEffect(null); if (helpPopup != null) helpPopup.setVisible(false); }
    @FXML public void handleNextPromoted(ActionEvent event) { currentCityIndex++; if (currentCityIndex >= promotedCities.size()) currentCityIndex = 0; loadPromotedCity(currentCityIndex); }
    @FXML public void handlePrevPromoted(ActionEvent event) { currentCityIndex--; if (currentCityIndex < 0) currentCityIndex = promotedCities.size() - 1; loadPromotedCity(currentCityIndex); }

    private void loadPromotedCity(int index) {
        CompletableFuture.runAsync(() -> {
            City city = promotedCities.get(index);
            int compatibility = 0;
            try { if (currentUser != null) compatibility = currentUser.calculateCompatibility(city); } catch (Exception e) {}
            final int finalScore = compatibility;
            Platform.runLater(() -> updatePromotedCity(city.getName(), finalScore));
        }, networkExecutor);
    }

    public void updatePromotedCity(String cityName, int score) {
        if (promotedCityNameLabel != null) promotedCityNameLabel.setText(cityName.toUpperCase());
        if (compalibilityScoreLabel != null) compalibilityScoreLabel.setText("%" + score);
        if (compatibilityScoreBar != null) compatibilityScoreBar.setProgress(score / 100.0);
        setSmartImage(promotedCitiesCityImage, cityName);
    }

    private void setSmartImage(ImageView targetView, String name) {
        if (targetView == null || name == null) return;
        CompletableFuture.runAsync(() -> {
            String cleanName = name.toLowerCase().replace("ı", "i");
            Image image = null;
            try {
                String path = "/images/city photos/" + cleanName + ".jpg";
                URL url = getClass().getResource(path);
                if (url == null) { path = "/images/city photos/" + cleanName + ".png"; url = getClass().getResource(path); }
                if (url != null) image = new Image(url.toExternalForm(), true);
                else { URL fallback = getClass().getResource("/images/logoBlue.png"); if(fallback!=null) image = new Image(fallback.toExternalForm(), true); }
            } catch (Exception e) {}
            final Image finalImg = image;
            Platform.runLater(() -> {
                if (finalImg != null) {
                    targetView.setImage(finalImg);
                    Rectangle clip = new Rectangle(targetView.getFitWidth(), targetView.getFitHeight());
                    clip.setArcWidth(30.0); clip.setArcHeight(30.0);
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
                if (url != null) image = new Image(url.toExternalForm(), true);
            } catch (Exception e) {}
            final Image finalImg = image;
            Platform.runLater(() -> { if (finalImg != null) targetCircle.setFill(new ImagePattern(finalImg)); });
        }, networkExecutor);
    }

    private void switchToOtherProfile(javafx.event.ActionEvent event, String userID) {
        try {
            if (userID.equals(currentUser.getId())){
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Profile.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
                javafx.scene.Parent root = loader.load();
                OtherProfileController controller = loader.getController();
                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                javafx.scene.Scene currentScene = source.getScene();
                controller.setProfileData(currentScene, userID);
                javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
                stage.setScene(new javafx.scene.Scene(root));
            }

        } catch (java.io.IOException e) { e.printStackTrace(); } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void openChannelPage(javafx.event.Event event, String cityName) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Channels.fxml"));
            javafx.scene.Parent root = loader.load();
            ChannelsController controller = loader.getController();
            String fixedName = cityName;
            if (cityName != null && cityName.length() > 1) fixedName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
            try { controller.openChannel(fixedName); } catch (Exception e) {}
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }
    private void addClickEffect(Button button) {
        button.setCursor(javafx.scene.Cursor.HAND);

        button.setOnMousePressed(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(0.90);
            st.setToY(0.90);
            st.play();
        });

        button.setOnMouseReleased(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(1.0); // %100'e geri dön
            st.setToY(1.0);
            st.play();
        });

        button.setOnMouseExited(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }
}

