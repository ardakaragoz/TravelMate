package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URL;
import java.util.ArrayList;
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
    @FXML private Label detailsDescription;
    @FXML private VBox channelDetailView;
    @FXML private Label channelTitleLabel;
    @FXML private VBox channelTripsContainer;
    @FXML private Circle detailsProfilePic;

    @FXML private Button channelJoinButton;
    @FXML private Button channelChatButton;
    @FXML private Button channelRecsButton;

    @FXML private VBox tripDetailsPopup;
    @FXML private Label detailDestinationLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailBudgetLabel;
    @FXML private Label detailDescLabel;
    @FXML private Label detailItineraryLabel;
    @FXML private Label detailsOwnerName;
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

    private Channel selectedChannel;

    public void initialize() {
        try {
            this.currentUser = UserSession.getCurrentUser();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sidebarController != null) sidebarController.setActivePage("Channels");

        loadCityButtons();
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                loadCityButtons(newValue); // Her harf girişinde listeyi filtrele
            });
        }
        if (channelRecsButton != null) channelRecsButton.setOnAction(e -> handleOpenRecommendations());

        if(sendRecommendationButton != null) {
            sendRecommendationButton.setOnAction(e -> {
                if(channelTitleLabel != null) sendRecommendation(channelTitleLabel.getText());
            });
        }
    }

//    public void openSpecificChannel(String cityName) {
//        String fixedName = cityName;
//        if (cityName != null && cityName.length() > 1) {
//            fixedName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
//        }
//        try {
//            openChannel(fixedName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @FXML
    public void handleOpenRecommendations() {
        if (recommendationsPopup != null) {
            String city = (channelTitleLabel != null) ? channelTitleLabel.getText() : "City";
            if (recsTitleLabel != null) recsTitleLabel.setText(city + " Recommendations");

            loadRecommendations(city);

            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            recommendationsPopup.setVisible(true);
            recommendationsPopup.toFront();
        }
    }

    @FXML public void closeRecommendationsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (recommendationsPopup != null) recommendationsPopup.setVisible(false);
    }

    @FXML public void openAddRecPopup() {
        if (recommendationsPopup != null) recommendationsPopup.setVisible(false);
        if (addRecPopup != null) {
            addRecPopup.setVisible(true);
            addRecPopup.toFront();
        }
    }

    @FXML public void closeAddRecPopup() {
        if (addRecPopup != null) addRecPopup.setVisible(false);
        if (recommendationsPopup != null) recommendationsPopup.setVisible(true);
    }

    public void sendRecommendation(String city) {
        String comment = (recCommentArea != null) ? recCommentArea.getText() : "";
        String link = (recLinkField != null) ? recLinkField.getText() : "";

        if (!comment.isEmpty()) {
            System.out.println("Sending recommendation: " + comment);
        }

        if (recCommentArea != null) recCommentArea.clear();
        if (recLinkField != null) recLinkField.clear();

        closeAddRecPopup();
    }

    private void loadRecommendations(String city) {
        if (recommendationsContainer == null) return;

        Platform.runLater(() -> recommendationsContainer.getChildren().clear());

        CompletableFuture.runAsync(() -> {
            try {
                Channel ch = ChannelList.getChannel(city);
                if (ch != null && ch.getRecommendations() != null) {
                    ArrayList<String> recommendationsList = ch.getRecommendations();
                    for (String reco : recommendationsList){
                        Recommendation rec = new Recommendation(reco);

                        String senderKey = rec.getSender();
                        String finalSenderName = "Unknown User";

                        if (senderKey != null) {
                            try {
                                User u = UserList.getUser(senderKey);
                                if (u != null) {
                                    finalSenderName = u.getUsername();
                                } else {
                                    finalSenderName = senderKey;
                                }
                            } catch (Exception e) {
                                finalSenderName = senderKey;
                            }
                        }

                        final String nameToShow = finalSenderName;
                        Platform.runLater(() -> addRecItem(nameToShow, rec.getMessage(), rec.getLink()));
                    }
                }
            } catch(Exception e) { e.printStackTrace(); }
        }, networkExecutor);
    }

    private void addRecItem(String name, String comment, String link) {
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
    public void closeChatPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (channelChatPopup != null) channelChatPopup.setVisible(false);
    }

    @FXML
    public void handleOpenChatPopup() {
        if (channelChatPopup != null) {
            String currentCity = (channelTitleLabel != null) ? channelTitleLabel.getText() : "Chat";
            if (chatPopupTitle != null) chatPopupTitle.setText(currentCity + " Chat");

            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            channelChatPopup.setVisible(true);
            channelChatPopup.toFront();

            // 1. Ensure Channel Object is Found
            if (selectedChannel == null) {
                selectedChannel = ChannelList.getChannel(currentCity);
            }

            // 2. Load Messages Safely
            if (selectedChannel != null) {
                String chatId = selectedChannel.getChannelChat();
                if (chatId != null && !chatId.isEmpty()) {
                    loadChatMessages(chatId);
                } else {
                    // Handle channels without a chat ID yet
                    chatMessagesContainer.getChildren().clear();
                    chatMessagesContainer.getChildren().add(new Label("Chat not active for this channel."));
                }
            } else {
                chatMessagesContainer.getChildren().clear();
                chatMessagesContainer.getChildren().add(new Label("Channel data not found."));
            }
        }
    }

    private void loadChatMessages(String chatRoomId) {
        if (chatMessagesContainer == null) return;

        // Reset UI
        chatMessagesContainer.getChildren().clear();
        Label loadingLabel = new Label("Loading messages...");
        loadingLabel.setStyle("-fx-text-fill: #555;");
        chatMessagesContainer.getChildren().add(loadingLabel);

        Task<List<Message>> loadTask = new Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                if (chatRoomId == null) return new ArrayList<>();

                DocumentSnapshot roomDoc = FirebaseService.getFirestore().collection("chatrooms").document(chatRoomId).get().get();

                // Fallback for legacy channels
                if (!roomDoc.exists()) {
                    DocumentSnapshot chanDoc = FirebaseService.getFirestore().collection("channels").document(chatRoomId).get().get();
                    if (chanDoc.exists() && chanDoc.contains("messages")) {
                        roomDoc = chanDoc;
                    } else {
                        return new ArrayList<>();
                    }
                }

                List<String> messageIds = (List<String>) roomDoc.get("messages");
                if (messageIds == null || messageIds.isEmpty()) return new ArrayList<>();

                // Load last 50 messages
                int total = messageIds.size();
                int start = Math.max(0, total - 50);
                List<String> recentIds = messageIds.subList(start, total);

                List<DocumentReference> refs = new ArrayList<>();
                for (String msgId : recentIds) {
                    if (msgId != null && !msgId.trim().isEmpty()) {
                        refs.add(FirebaseService.getFirestore().collection("messages").document(msgId));
                    }
                }

                if (refs.isEmpty()) return new ArrayList<>();

                List<DocumentSnapshot> snapshots = FirebaseService.getFirestore().getAll(refs.toArray(new DocumentReference[0])).get();

                List<Message> loadedMessages = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    if (doc.exists()) {
                        try {
                            loadedMessages.add(new Message(doc));
                        } catch (Exception e) { }
                    }
                }

                // SORT: Oldest at Top, Newest at Bottom (Standard Chat)
                loadedMessages.sort(Comparator.comparing(m -> m.getCreatedAt() != null ? m.getCreatedAt() : new Date(0)));

                return loadedMessages;
            }
        };

        loadTask.setOnSucceeded(event -> {
            chatMessagesContainer.getChildren().clear();
            List<Message> messages = loadTask.getValue();

            if (messages == null || messages.isEmpty()) {
                Label emptyLabel = new Label("No messages yet.");
                emptyLabel.setStyle("-fx-text-fill: #888; -fx-padding: 10;");
                chatMessagesContainer.getChildren().add(emptyLabel);
            } else {
                for (Message msg : messages) {
                    User sender = msg.getSender();
                    boolean isSelf = (sender != null && currentUser != null) && sender.getId().equals(currentUser.getId());
                    String senderName = (sender != null) ? sender.getName() : "Unknown";

                    // Add bubble
                    addForumBubble(msg.getMessage(), senderName, isSelf, sender);
                }
            }

            // --- SCROLL FIX ---
            // We run this TWICE: Once immediately, and once after a tiny delay
            // to ensure the VBox height calculation is complete.
            scrollToBottom();

            // Extra insurance delay to force scroll to bottom after layout render
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> scrollToBottom());
                }
            }, 100);
        });

        loadTask.setOnFailed(e -> {
            chatMessagesContainer.getChildren().clear();
            chatMessagesContainer.getChildren().add(new Label("Unable to load messages."));
            e.getSource().getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    // Helper method to force scroll to bottom
    private void scrollToBottom() {
        if (chatScrollPane != null) {
            chatMessagesContainer.applyCss();
            chatMessagesContainer.layout();
            chatScrollPane.layout();
            chatScrollPane.setVvalue(1.0); // 1.0 = Bottom
        }
    }

    @FXML public void handleSendPopupMessage() {
        if (chatInputPopup == null) return;
        String text = chatInputPopup.getText().trim();

        if (!text.isEmpty() && selectedChannel != null) {
            // 1. Instant UI update
            String myName = (currentUser != null && currentUser.getName() != null) ? currentUser.getName() : "Me";
            addForumBubble(text, myName, true, currentUser);

            chatInputPopup.clear();

            // 2. Send to Firebase in background
            new Thread(() -> {
                try {
                    String msgId = UUID.randomUUID().toString();
                    Message msg = new Message(msgId, text, currentUser);

                    String chatRoomId = selectedChannel.getChannelChat();
                    DocumentReference roomRef = FirebaseService.getFirestore().collection("chatrooms").document(chatRoomId);

                    if (roomRef.get().get().exists()) {
                        roomRef.update("messages", FieldValue.arrayUnion(msgId));
                    } else {
                        // Create chatroom if missing
                        Map<String, Object> data = new HashMap<>();
                        data.put("messages", Arrays.asList(msgId));
                        data.put("type", "channelChat");
                        roomRef.set(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void addForumBubble(String text, String senderName, boolean isSelf, User user) {
        HBox row = new HBox(10);
        row.setAlignment(isSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        bubble.setPadding(new Insets(10, 15, 10, 15));

        // Background Colors
        if (isSelf) {
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        } else {
            bubble.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        }

        // Sender Name
        Label nameLbl = new Label(senderName != null ? senderName : "User");
        // Use "System" font to ensure visibility if custom font fails
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLbl.setTextFill(Color.BLACK);
        nameLbl.setStyle("-fx-text-fill: black;");

        // Message Text
        Label msgLbl = new Label(text != null ? text : "");
        msgLbl.setWrapText(true);
        msgLbl.setTextFill(Color.BLACK);
        msgLbl.setFont(Font.font("System", 14));
        msgLbl.setStyle("-fx-text-fill: black;");

        bubble.getChildren().addAll(nameLbl, msgLbl);

        // Profile Picture
        Circle pic = new Circle(18, isSelf ? Color.LIGHTBLUE : Color.LIGHTGRAY);
        pic.setStroke(Color.BLACK);
        setProfileImage(pic, user);

        if (isSelf) row.getChildren().addAll(bubble, pic); else row.getChildren().addAll(pic, bubble);

        // Add to UI on correct thread
        Platform.runLater(() -> {
            if (chatMessagesContainer != null) {
                chatMessagesContainer.getChildren().add(row);
                // Force scroll to bottom whenever a single message is added (like when sending)
                scrollToBottom();
            }
        });
    }

    private void setProfileImage(Circle circle, User user) {
        if (circle == null) return;
        new Thread(() -> {
            Image img = fetchImage(user);
            Platform.runLater(() -> circle.setFill(new ImagePattern(img)));
        }).start();
    }

    private Image fetchImage(User user) {
        Image imageToSet = null;
        try {
            if (user != null && user.getProfile() != null) {
                String secureUrl = formatToHttps(user.getProfile().getProfilePictureUrl());
                if (secureUrl != null && !secureUrl.isEmpty()) {
                    imageToSet = new Image(secureUrl, false);
                }
            }
            if (imageToSet == null || imageToSet.isError()) {
                var resource = getClass().getResourceAsStream("/images/user_icons/img.png");
                if (resource != null) imageToSet = new Image(resource);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return imageToSet;
    }

    private String formatToHttps(String gsUrl) {
        if (gsUrl == null || gsUrl.isEmpty()) return null;
        if (gsUrl.startsWith("http")) return gsUrl;
        try {
            if (gsUrl.startsWith("gs://")) {
                String cleanPath = gsUrl.substring(5);
                int bucketSeparator = cleanPath.indexOf('/');
                if (bucketSeparator != -1) {
                    String bucket = cleanPath.substring(0, bucketSeparator);
                    String path = cleanPath.substring(bucketSeparator + 1);
                    String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                    return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encodedPath + "?alt=media";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
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

        if (currentUser != null && currentUser.getChannels() != null) {
            for (String city : currentUser.getChannels()){
                addCityButton(city);
                addedChannelNames.add(city);
            }
        }

        try {
            if(CityList.cities != null) {
                for (String city : CityList.cities.keySet()) {
                    if (!addedChannelNames.contains(city)){
                        addCityButton(city);
                        addedChannelNames.add(city);
                    }
                }
            }
        } catch(Exception e) {}
    }

    private void addCityButton(String city) {
        Button btn = new Button(city);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(15, 20, 15, 20));
        addClickEffect(btn);

        btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px;");

        btn.setOnAction(e -> {
            updateActiveCityButton(btn);
            openChannel(city);
        });
        citySelectionView.getChildren().add(btn);
    }

    public void handleJoin(String city) {
        try {
            ChannelList.getChannel(city).addParticipant(currentUser);
            loadCityButtons();
            openChannel(city);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void handleLeave(String city) {
        try {
            ChannelList.getChannel(city).removeParticipant(currentUser);
            loadCityButtons();
            openChannel(city);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void openChannel(String cityName) {
        if (channelTitleLabel != null) channelTitleLabel.setText(cityName);
        if(chatPopupTitle != null) chatPopupTitle.setText(cityName + " Chat");
        if(recsTitleLabel != null) recsTitleLabel.setText(cityName + " Recommendations");
        selectedChannel = ChannelList.getChannel(cityName);

        if (channelJoinButton != null) {
            boolean isParticipant = currentUser != null && currentUser.getChannels() != null && currentUser.getChannels().contains(cityName);
            if (isParticipant) {
                channelJoinButton.setText("Leave Channel");
                channelJoinButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-weight: bold;");
                channelJoinButton.setOnAction(e -> handleLeave(cityName));
                channelChatButton.setDisable(false);
            } else {
                channelJoinButton.setText("Join Channel");
                channelJoinButton.setStyle("-fx-background-color: #CCFF00; -fx-text-fill: #1E3A5F; -fx-background-radius: 15; -fx-font-weight: bold;");
                channelJoinButton.setOnAction(e -> handleJoin(cityName));
                channelChatButton.setDisable(true);
            }
            addClickEffect(channelJoinButton);
        }
        loadTripsForCity(cityName);
    }

    private void updateActiveCityButton(Button activeBtn) {
        for (javafx.scene.Node node : citySelectionView.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px;");
            }
        }
        activeBtn.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
    }

    private void loadTripsForCity(String city) {
        if (channelTripsContainer == null) return;
        channelTripsContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                Channel speChannel = ChannelList.getChannel(city);
                if (speChannel == null && city != null) {
                    String formattedName = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();
                    speChannel = ChannelList.getChannel(formattedName);
                }

                if (speChannel == null) {
                    Platform.runLater(() -> {
                        Label emptyLbl = new Label("No trips found for this city yet.");
                        emptyLbl.setStyle("-fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold;");
                        channelTripsContainer.getChildren().add(emptyLbl);
                    });
                    return;
                }

                ArrayList<String> reqs = speChannel.getTripRequests();
                if (reqs != null) {
                    for (String req : reqs) {
                        Trip t = TripList.getTrip(req);
                        if (!t.isFinished()) {
                            if (t != null) {
                                User u = UserList.getUser(t.getUser());
                                if (u != null) {
                                    Platform.runLater(() -> addTripCard(t, u));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, networkExecutor);
    }


    // COMPLETE FIXED addTripCard method for ChannelsController.java
// Replace your existing addTripCard method with this:

    private void addTripCard(Trip trip, User owner) {
        HBox card = new HBox();
        card.setPrefHeight(220);
        card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(10, Color.web("#00000033")));

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(12, 8, 12, 15));
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Top Row - Profile and Name
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(20, Color.LIGHTGRAY);
        profilePic.setStroke(Color.BLACK);
        // FIXED: Now properly loads profile pictures
        setProfileImage(profilePic, owner);

        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(owner.getUsername());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLbl.setStyle("-fx-text-fill: black;");

        Label lvlLbl = new Label("Lvl. " + owner.getLevel());
        lvlLbl.setStyle("-fx-text-fill: black; -fx-font-style: italic;");
        lvlLbl.setFont(Font.font("System", 12));
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);

        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        viewProfileBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        addClickEffect(viewProfileBtn); // Only button gets effect
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, owner.getId()));

        topRow.getChildren().addAll(profilePic, nameBox, r1, viewProfileBtn);

        // Info Label - Compact format
        String dateStr = (trip.getDepartureDate() != null ? trip.getDepartureDate().toString() : "TBD");
        String depLocation = trip.getDepartureLocation();
        if (depLocation != null && depLocation.length() > 12) {
            depLocation = depLocation.substring(0, 12);
        }
        Label infoLbl = new Label("From: " + depLocation + " | " + dateStr + " | " + trip.getDays() + " Days");
        infoLbl.setFont(Font.font(13));
        infoLbl.setStyle("-fx-text-fill: black;");
        infoLbl.setWrapText(false);

        // Middle Row - Mates and Budget
        int joined = (trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0);
        HBox midRow = new HBox(15);
        midRow.setAlignment(Pos.CENTER_LEFT);

        Label matesLbl = new Label(joined + "/" + trip.getMateCount() + " mates");
        matesLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        matesLbl.setStyle("-fx-text-fill: black;");

        Region midSpacer = new Region();
        HBox.setHgrow(midSpacer, Priority.ALWAYS);

        Label budgetLbl = new Label(trip.getAverageBudget() + " " + trip.getCurrency());
        budgetLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        budgetLbl.setStyle("-fx-text-fill: black;");

        midRow.getChildren().addAll(matesLbl, midSpacer, budgetLbl);

        // Score Row - Compatibility
        HBox scoreRow = new HBox(8);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        int compatibility = 70; // Default
        try {
            compatibility = (currentUser.calculateCompatibility(owner) + currentUser.calculateCompatibility(CityList.getCity(trip.getDestinationName()))) / 2;
        } catch (Exception e) {}

        Label compatLbl = new Label("Compatibility: %" + compatibility);
        compatLbl.setStyle("-fx-text-fill: black;");
        compatLbl.setFont(Font.font(13));

        ProgressBar pBar = new ProgressBar((double) compatibility / 100);
        pBar.setPrefWidth(90);
        pBar.setPrefHeight(15);
        pBar.setStyle("-fx-accent: #1E3A5F;");

        scoreRow.getChildren().addAll(compatLbl, pBar);

        // Bottom Row - City Name and Details Button
        HBox bottomRow = new HBox(12);
        bottomRow.setAlignment(Pos.CENTER);

        String cityName = trip.getDestinationName();
        if (cityName != null && cityName.length() > 13) {
            cityName = cityName.substring(0, 13);
        }
        Label cityLbl = new Label(cityName.toUpperCase());
        cityLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        cityLbl.setStyle("-fx-text-fill: black;");
        // FIXED: NO click effect on label

        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        detailsBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        addClickEffect(detailsBtn); // Only button gets effect
        detailsBtn.setOnAction(e -> {
            try {
                openTripDetailsPopup(trip, owner);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        bottomRow.getChildren().addAll(s1, cityLbl, s2, detailsBtn);

        infoBox.getChildren().addAll(topRow, infoLbl, midRow, scoreRow, bottomRow);

        // Image Pane - Perfectly centered and sized
        StackPane imagePane = new StackPane();
        imagePane.setMinSize(250, 206);
        imagePane.setMaxSize(250, 206);
        imagePane.setAlignment(Pos.CENTER);
        imagePane.setStyle("-fx-background-color: #a4c2f2; -fx-background-radius: 20;");

        ImageView cityImg = new ImageView();
        cityImg.setFitWidth(250);
        cityImg.setFitHeight(206);
        cityImg.setPreserveRatio(false);
        StackPane.setAlignment(cityImg, Pos.CENTER);
        setSmartImage(cityImg, trip.getDestinationName());

        Rectangle clip = new Rectangle(250, 206);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imagePane.setClip(clip);
        imagePane.getChildren().add(cityImg);

        card.getChildren().addAll(infoBox, imagePane);
        if(channelTripsContainer != null) channelTripsContainer.getChildren().add(card);
    }


    @FXML public void closeTripDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (tripDetailsPopup != null) tripDetailsPopup.setVisible(false);
    }

    private void openTripDetailsPopup(Trip trip, User owner) throws ExecutionException, InterruptedException {
        if (tripDetailsPopup == null) return;
        this.currentDetailTrip = trip;
        this.selectedTripOwnerForDetails = owner;

        if (detailsOwnerName != null) detailsOwnerName.setText(owner.getUsername());
        if (detailsProfilePic != null) setCircleImage(detailsProfilePic, owner.getUsername());
        if (detailsDescription != null) detailsDescription.setText(trip.getAdditionalNotes());

        boolean isMyTrip = currentUser != null && currentUser.getId().equals(owner.getId());

        if (sendRequestBtn != null) {
            sendRequestBtn.setVisible(!isMyTrip);
            sendRequestBtn.setManaged(!isMyTrip);
        }

        if (ownTripLabel != null) {
            ownTripLabel.setVisible(isMyTrip);
            ownTripLabel.setManaged(isMyTrip);
        }

        boolean already = false;
        for (String reqID: currentUser.getJoinRequests()){
            JoinRequest req = new JoinRequest(reqID);
            if (req.getTrip().getId().equals(trip.getId())) {
                already = true;
                break;
            }
        }

        if (already) {
            if (sendRequestBtn != null) {
                sendRequestBtn.setVisible(false);
                sendRequestBtn.setManaged(false);
            }

            if (ownTripLabel != null) {
                ownTripLabel.setVisible(true);
                ownTripLabel.setManaged(true);
                ownTripLabel.setText("You've already sent a request!");
            }
        }
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        tripDetailsPopup.setVisible(true);
        tripDetailsPopup.toFront();
    }

    @FXML public void handleSendRequestButton() {
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

                    Platform.runLater(() -> {
                        if(messageInputArea != null) messageInputArea.clear();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Request sent successfully!");
                        alert.showAndWait();
                        closeTripDetailsPopup();
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }, networkExecutor);
        } else {
            closeTripDetailsPopup();
        }
    }

    // --- HOME ILE AYNI RESİM YÜKLEME MANTIĞI ---
    private void setCircleImage(Circle targetCircle, String imageIdentifier) {
        if (targetCircle == null) return;
        CompletableFuture.runAsync(() -> {
            Image image = null;
            try {
                if (imageIdentifier != null && (imageIdentifier.startsWith("http") || imageIdentifier.startsWith("gs://"))) {
                    image = new Image(imageIdentifier, true);
                } else {
                    // Boşlukları sil ve küçük harfe çevir (Home mantığı)
                    String cleanName = (imageIdentifier != null)
                            ? imageIdentifier.toLowerCase().replace("ı", "i").replaceAll("\\s+", "")
                            : "user_icon";
                    String path = "/images/" + cleanName + ".png";
                    URL url = getClass().getResource(path);
                    if (url == null) url = getClass().getResource("/images/user_icon.png");
                    if (url != null) image = new Image(url.toExternalForm(), true);
                }
            } catch (Exception e) {}

            final Image finalImg = image;
            Platform.runLater(() -> { if (finalImg != null) targetCircle.setFill(new ImagePattern(finalImg)); });
        }, networkExecutor);
    }

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
                if (url != null) image = new Image(url.toExternalForm(), true);
                else {
                    URL fallback = getClass().getResource("/images/logoBlue.png");
                    if(fallback!=null) image = new Image(fallback.toExternalForm(), true);
                }
            } catch (Exception e) {}

            final Image finalImg = image;
            Platform.runLater(() -> {
                if (finalImg != null) targetView.setImage(finalImg);
            });
        }, networkExecutor);
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
    }
}