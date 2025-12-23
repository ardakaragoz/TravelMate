package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.*;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelsController {

    @FXML private SidebarController sidebarController;
    @FXML private BorderPane mainContainer;

    @FXML private VBox citySelectionView;
    @FXML private TextField searchField;

    @FXML private VBox channelDetailView;
    @FXML private Label channelTitleLabel;
    @FXML private VBox channelTripsContainer;

    @FXML private Button channelJoinButton;
    @FXML private Button channelChatButton;
    @FXML private Button channelRecsButton;

    @FXML private VBox tripDetailsPopup;
    @FXML private Circle detailsProfilePic;
    @FXML private Label detailsOwnerName;
    @FXML private Label detailsDescription;
    @FXML private TextArea messageInputArea;
    @FXML private Label ownTripLabel;
    @FXML private Button sendRequestBtn;

    private Trip currentDetailTrip;
    private User selectedTripOwnerForDetails;
    private User currentUser;
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();

    @FXML private VBox channelChatPopup;
    @FXML private Label chatPopupTitle;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInputPopup;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox recommendationsPopup;
    @FXML private VBox recommendationsContainer;
    @FXML private Label recsTitleLabel;
    @FXML private Button sendRecommendationButton;
    @FXML private VBox addRecPopup;
    @FXML private TextArea recCommentArea;
    @FXML private TextField recLinkField;

    public void initialize() {
        try {
            this.currentUser = UserSession.getCurrentUser();
        } catch (Exception e) {
            System.out.println("User session error: " + e.getMessage());
        }

        if (sidebarController != null) sidebarController.setActivePage("Channels");

        loadCityButtons();
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                loadCityButtons(newValue); // Her harf girişinde listeyi filtrele
            });
        }
        if (channelRecsButton != null) {
            channelRecsButton.setOnAction(e -> {
                try {
                    handleOpenRecommendations();
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
    public void openSpecificChannel(String cityName) {
        String fixedName = cityName;
        if (cityName != null && cityName.length() > 1) {
            fixedName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
        }
        try {
            openChannel(fixedName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenRecommendations() throws ExecutionException, InterruptedException {
        if (recommendationsPopup != null) {
            String city = (channelTitleLabel != null) ? channelTitleLabel.getText() : "City";
            if (recsTitleLabel != null) recsTitleLabel.setText(city + " Recommendations");

            loadRecommendations(city);

            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            recommendationsPopup.setVisible(true);
            recommendationsPopup.toFront();
        }
    }

    @FXML
    public void closeRecommendationsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (recommendationsPopup != null) recommendationsPopup.setVisible(false);
    }

    @FXML
    public void openAddRecPopup() {
        if (recommendationsPopup != null) recommendationsPopup.setVisible(false);
        if (addRecPopup != null) {
            addRecPopup.setVisible(true);
            addRecPopup.toFront();
        }
    }

    @FXML
    public void closeAddRecPopup() {
        if (addRecPopup != null) addRecPopup.setVisible(false);
        if (recommendationsPopup != null) recommendationsPopup.setVisible(true);
    }

    @FXML
    public void sendRecommendation(String city) {
        String comment = (recCommentArea != null) ? recCommentArea.getText() : "";
        String link = (recLinkField != null) ? recLinkField.getText() : "";
        if (!comment.isEmpty()) {
            Recommendation rec = new Recommendation("" + System.currentTimeMillis(), comment, currentUser, ChannelList.getChannel(city), link);
        }

        if (recCommentArea != null) recCommentArea.clear();
        if (recLinkField != null) recLinkField.clear();

        closeAddRecPopup();
    }

    private void loadRecommendations(String city) throws ExecutionException, InterruptedException {
        if (recommendationsContainer == null) return;
        recommendationsContainer.getChildren().clear();

        ArrayList<String> recommendationsList = ChannelList.getChannel(city).getRecommendations();
        for (String reco : recommendationsList){
            Recommendation rec = new Recommendation(reco);
            addRecItem(rec.getSender().getName(), rec.getMessage(), rec.getLink());
        }
    }

    private void addRecItem(String name, String comment, String link) {
        if (recommendationsContainer == null) return;

        VBox item = new VBox(5);
        item.setStyle("-fx-background-color: #F0F4F8; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F; -fx-font-size: 14px;");

        Label commentLbl = new Label(comment);
        commentLbl.setWrapText(true);
        commentLbl.setStyle("-fx-text-fill: #555;");

        item.getChildren().addAll(nameLbl, commentLbl);

        if (link != null && !link.trim().isEmpty()) {
            Hyperlink linkBtn = new Hyperlink("📍 View on Google Maps");
            linkBtn.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold; -fx-border-color: transparent;");

            linkBtn.setOnAction(e -> {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(link));
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            item.getChildren().add(linkBtn);
        }
        recommendationsContainer.getChildren().add(item);
    }


    @FXML
    public void handleOpenChatPopup() {
        if (channelChatPopup != null) {
            String currentCity = (channelTitleLabel != null) ? channelTitleLabel.getText() : "Chat";
            if (currentCity == null || currentCity.equals("Select a City")) return;

            if (chatPopupTitle != null) chatPopupTitle.setText(currentCity + " Chat");

            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            channelChatPopup.setVisible(true);
            channelChatPopup.toFront();

            loadDummyMessages();
        }
    }

    @FXML
    public void closeChatPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (channelChatPopup != null) channelChatPopup.setVisible(false);
    }

    @FXML
    public void handleSendPopupMessage() {
        if (chatInputPopup == null) return;
        String msg = chatInputPopup.getText();
        if (msg != null && !msg.isEmpty()) {
            addMessageBubble(msg, true);
            chatInputPopup.clear();
        }
    }

    private void addMessageBubble(String text, boolean isMe) {
        if (chatMessagesContainer == null) return;
        HBox bubble = new HBox();
        bubble.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(350);
        lbl.setPadding(new Insets(10));
        lbl.setFont(Font.font("System", 14));

        if (isMe) {
            lbl.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15 15 0 15; -fx-text-fill: #1E3A5F;");
        } else {
            lbl.setStyle("-fx-background-color: WHITE; -fx-background-radius: 15 15 15 0; -fx-text-fill: black; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        }

        bubble.getChildren().add(lbl);
        chatMessagesContainer.getChildren().add(bubble);
        if(chatScrollPane != null) chatScrollPane.setVvalue(1.0);
    }

    private void loadDummyMessages() {
        if(chatMessagesContainer == null) return;
        chatMessagesContainer.getChildren().clear();
        addMessageBubble("Hey! Is there anyone staying at Covent Garden?", false);
        addMessageBubble("I highly recommend the Gokyuzu restaurant!", false);
    }

    private void loadCityButtons(String filter) {
        if (filter.isEmpty()) filter = "";
        if(citySelectionView == null) return;
        citySelectionView.getChildren().clear();
        List<String> addedChannelNames = new ArrayList<>();
        for (String city : currentUser.getChannels()){
            if (city.toLowerCase().contains(filter.toLowerCase())){
                Button btn = new Button(city);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setPadding(new Insets(15, 20, 15, 20));
                btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
                addClickEffect(btn);

                btn.setOnAction(e -> {
                    updateActiveCityButton(btn);
                    openChannel(city);
                });


                citySelectionView.getChildren().add(btn);
                addedChannelNames.add(city);
            }

        }
        for (String city : CityList.cities.keySet()) {
            if (!addedChannelNames.contains(city) && city.toLowerCase().contains(filter.toLowerCase())) {
                Button btn = new Button(city);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setPadding(new Insets(15, 20, 15, 20));
                btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
                addClickEffect(btn);

                btn.setOnAction(e -> {
                    updateActiveCityButton(btn);
                    openChannel(city);
                });


                citySelectionView.getChildren().add(btn);
                addedChannelNames.add(city);
            }

        }
    }

    private void loadCityButtons() {
        if(citySelectionView == null) return;
        citySelectionView.getChildren().clear();
        List<String> addedChannelNames = new ArrayList<>();
        for (String city : currentUser.getChannels()){
            Button btn = new Button(city);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(15, 20, 15, 20));
            btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
            addClickEffect(btn);

            btn.setOnAction(e -> {
                updateActiveCityButton(btn);
                openChannel(city);
            });


            citySelectionView.getChildren().add(btn);
            addedChannelNames.add(city);
        }
        for (String city : CityList.cities.keySet()) {
            if (!addedChannelNames.contains(city)){
                Button btn = new Button(city);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setPadding(new Insets(15, 20, 15, 20));
                btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
                addClickEffect(btn);

                btn.setOnAction(e -> {
                    updateActiveCityButton(btn);
                    openChannel(city);
                });


                citySelectionView.getChildren().add(btn);
                addedChannelNames.add(city);
            }

        }
    }

    public void handleJoin(String city) throws ExecutionException, InterruptedException {
        ChannelList.getChannel(city).addParticipant(currentUser);
        loadCityButtons();
    }

    public void handleLeave(String city) throws ExecutionException, InterruptedException {
        ChannelList.getChannel(city).removeParticipant(currentUser);
        loadCityButtons();
    }

    public void openChannel(String cityName) {
        if (channelTitleLabel != null) channelTitleLabel.setText(cityName);
        if(chatPopupTitle != null) chatPopupTitle.setText(cityName + " Chat");
        if(recsTitleLabel != null) recsTitleLabel.setText(cityName + " Recommendations");

        if (channelJoinButton != null) {
            if (currentUser.getChannels().contains(cityName)) {
                channelJoinButton.setText("Leave Channel");
                channelJoinButton.setDisable(false);
                channelJoinButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");

                channelJoinButton.setOnAction(e -> {
                    try {
                        handleLeave(cityName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            }
            else {
                channelJoinButton.setText("Join Channel");
                channelJoinButton.setDisable(false);
                channelJoinButton.setStyle("-fx-background-color: #CCFF00; -fx-text-fill: #1E3A5F; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");

                channelJoinButton.setOnAction(e -> {
                    try {
                        handleJoin(cityName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
        sendRecommendationButton.setOnAction(e -> {
            sendRecommendation(cityName);
        });
        loadTripsForCity(cityName);
    }

    private void updateActiveCityButton(Button activeBtn) {
        for (javafx.scene.Node node : citySelectionView.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
            }
        }
        activeBtn.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
    }

    private void loadTripsForCity(String city) {
        if (channelTripsContainer == null) return;
        channelTripsContainer.getChildren().clear();

        try {
            Channel speChannel = ChannelList.getChannel(city);
            if (speChannel == null && city != null) {
                String formattedName = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();
                speChannel = ChannelList.getChannel(formattedName);
            }

            if (speChannel == null) {
                Label emptyLbl = new Label("No trips found for this city yet.");
                emptyLbl.setStyle("-fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold;");
                channelTripsContainer.getChildren().add(emptyLbl);
                return;
            }

            ArrayList<String> reqs = speChannel.getTripRequests();
            if (reqs != null) {
                for (String req : reqs) {
                    Trip t = TripList.getTrip(req);
                    if (t != null) {
                        User u = UserList.getUser(t.getUser());
                        if (u != null) addTripCard(t, u);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void addTripCard(Trip trip, User owner) {
        HBox card = new HBox();
        card.setPrefHeight(180); card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(5, Color.web("#00000033")));
        card.setPadding(new Insets(15));

        card.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button)) {
                openTripDetailsPopup(trip, owner);
            }
        });

        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox topRow = new HBox(10); topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(25, Color.LIGHTGRAY);
        setCircleImage(profilePic, owner.getProfile() != null ? owner.getProfile().getProfilePictureUrl() : null);

        VBox nameBox = new VBox();
        Label nameLbl = new Label(owner.getUsername());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setTextFill(Color.BLACK);

        Label lvlLbl = new Label("Lvl. " + owner.getLevel());
        lvlLbl.setTextFill(Color.GRAY);
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Button viewProfileBtn = new Button("View Profile");
        addClickEffect(viewProfileBtn);
        viewProfileBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-cursor: hand;");
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, owner.getId()));

        topRow.getChildren().addAll(profilePic, nameBox, r, viewProfileBtn);

        Label detailLbl = new Label("Departuring from: " + trip.getDepartureLocation() + "\nDates: " + trip.getDepartureDate());
        detailLbl.setFont(Font.font(14));
        detailLbl.setTextFill(Color.BLACK);

        HBox bottomRow = new HBox(20); bottomRow.setAlignment(Pos.CENTER_LEFT);
        int mateCount = trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0;
        Label mateLbl = new Label(mateCount + "/" + trip.getMateCount() + " mates");
        mateLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        mateLbl.setTextFill(Color.BLACK);

        Region r2 = new Region(); HBox.setHgrow(r2, Priority.ALWAYS);

        Button detailsBtn = new Button("Details");
        addClickEffect(detailsBtn);
        detailsBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> openTripDetailsPopup(trip, owner));

        bottomRow.getChildren().addAll(mateLbl, r2, detailsBtn);
        infoBox.getChildren().addAll(topRow, detailLbl, bottomRow);
        card.getChildren().add(infoBox);
        channelTripsContainer.getChildren().add(card);
    }

    @FXML
    public void closeTripDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (tripDetailsPopup != null) tripDetailsPopup.setVisible(false);
    }

    private void openTripDetailsPopup(Trip trip, User owner) {
        if (tripDetailsPopup == null) return;
        this.currentDetailTrip = trip;
        this.selectedTripOwnerForDetails = owner;

        if (detailsOwnerName != null) detailsOwnerName.setText(owner.getUsername());
        if (detailsDescription != null) detailsDescription.setText(trip.getAdditionalNotes() != null ? trip.getAdditionalNotes() : "No description.");
        setCircleImage(detailsProfilePic, owner.getProfile() != null ? owner.getProfile().getProfilePictureUrl() : null);

        boolean isOwn = currentUser != null && currentUser.getId().equals(owner.getId());

        if (isOwn) {
            if (sendRequestBtn != null) { sendRequestBtn.setVisible(false); sendRequestBtn.setManaged(false); }
            if (messageInputArea != null) { messageInputArea.setVisible(false); messageInputArea.setManaged(false); }
            if (ownTripLabel != null) { ownTripLabel.setVisible(true); ownTripLabel.setManaged(true); }
        } else {
            if (sendRequestBtn != null) { sendRequestBtn.setVisible(true); sendRequestBtn.setManaged(true); }
            if (messageInputArea != null) { messageInputArea.setVisible(true); messageInputArea.setManaged(true); }
            if (ownTripLabel != null) { ownTripLabel.setVisible(false); ownTripLabel.setManaged(false); }
        }

        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        tripDetailsPopup.setVisible(true);
        tripDetailsPopup.toFront();
    }

    @FXML
    public void handleSendRequestButton() {
        String message = (messageInputArea != null && !messageInputArea.getText().isEmpty())
                ? messageInputArea.getText()
                : "Hi! I'd like to join your trip.";

        if (currentDetailTrip != null && selectedTripOwnerForDetails != null && currentUser != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String reqId = UUID.randomUUID().toString();
                    JoinRequest req = new JoinRequest(reqId, currentUser, selectedTripOwnerForDetails, message, currentDetailTrip);
                    currentUser.addRequest(req);
                    currentDetailTrip.addPendingMate(currentUser);


                    Firestore db = FirebaseService.getFirestore();
                    db.collection("join_requests").add(req);

                    Platform.runLater(() -> {
                        if(messageInputArea != null) messageInputArea.clear();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Request sent successfully!");
                        alert.showAndWait();

                        closeTripDetailsPopup();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, networkExecutor);
        } else {
            closeTripDetailsPopup();
        }
    }

    private void setCircleImage(Circle targetCircle, String name) {
        if (targetCircle == null) return;
        try {
            String path = "/images/" + name + ".png";
            if (getClass().getResource(path) == null) path = "/images/logoBlue.png";
            targetCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addClickEffect(Button button) {
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.95); st.setToY(0.95); st.play();
        });
        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }
}