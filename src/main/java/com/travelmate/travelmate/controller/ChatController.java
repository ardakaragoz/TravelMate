package com.travelmate.travelmate.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.ListenerRegistration;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.ChatList;
import com.travelmate.travelmate.session.UserList;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ChatController {

    @FXML private SidebarController sidebarController;
    @FXML private VBox chatListContainer;

    @FXML private Label currentChatNameLabel;
    @FXML private ImageView currentChatImage;

    @FXML private VBox messageBubbleContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane messageScrollPane;

    private List<HBox> chatItemNodes = new ArrayList<>();
    private ChatRoom currentChatRoom = null;
    private User currentUser;
    private User activeChatUser;

    private ListenerRegistration activeChatListener; // Stores the active connection
    private Set<String> loadedMessageIds = new HashSet<>();
    private int lastTotalMessageCount = 0;

    // Image Cache (Load once, use everywhere)
    private static Image defaultUserImage;

    private Map<String, List<Message>> localMessageCache = new HashMap<>();

    public void initialize() {
        if(sidebarController != null) sidebarController.setActivePage("Chat");


        try {
            InputStream is = getClass().getResourceAsStream("/images/user_icon.png");
            if (is != null) defaultUserImage = new Image(is);
        } catch (Exception e) { e.printStackTrace(); }


        messageScrollPane.setFitToHeight(true);
        messageBubbleContainer.setAlignment(Pos.BOTTOM_CENTER);
        messageBubbleContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            messageScrollPane.setVvalue(1.0);
        });

        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        loadChatsFromDatabase();
    }

    private void loadChatsFromDatabase() {

        chatListContainer.getChildren().clear();
        chatItemNodes.clear();

        List<String> chatIds = currentUser.getChatRooms();
        if (chatIds == null || chatIds.isEmpty()) return;

        Task<List<ChatData>> loadSidebarTask = new Task<>() {
            @Override
            protected List<ChatData> call() throws Exception {
                List<ChatData> loadedChats = new ArrayList<>();

                for (String chatId : chatIds) {
                    ChatRoom room = ChatList.getChat(chatId);
                    if (room != null) {
                        User otherUser = findOtherUser(room);
                        String displayName = (otherUser != null) ? otherUser.getName() : "Unknown Chat";
                        String lastMsg = "Click to view";
                        loadedChats.add(new ChatData(displayName, lastMsg, room, otherUser));
                    }
                }
                return loadedChats;
            }
        };

        loadSidebarTask.setOnSucceeded(event -> {
            List<ChatData> data = loadSidebarTask.getValue();
            boolean isFirst = true;

            for (ChatData chat : data) {
                addChatToSidebar(chat.name, chat.lastMsg, "", isFirst, chat.room, chat.otherUser);
                if (isFirst) {
                    if (!chatItemNodes.isEmpty()) {
                        handleChatClick(chatItemNodes.get(chatItemNodes.size() - 1), chat.name, chat.room, chat.otherUser);
                    }
                    isFirst = false;
                }
            }
        });

        new Thread(loadSidebarTask).start();
    }
    private static class ChatData {
        String name;
        String lastMsg;
        ChatRoom room;
        User otherUser;
        public ChatData(String name, String lastMsg, ChatRoom room, User otherUser) {
            this.name = name;
            this.lastMsg = lastMsg;
            this.room = room;
            this.otherUser = otherUser;
        }
    }

    private User findOtherUser(ChatRoom room) {
        for (String userId : room.getActiveUsers()) {
            if (!userId.equals(currentUser.getId())) {
                return UserList.getUser(userId);
            }
        }
        return null;
    }

    private void addChatToSidebar(String name, String lastMsg, String time, boolean isActive, ChatRoom room, User otherUser) {
        HBox chatItem = new HBox(15);
        chatItem.setAlignment(Pos.CENTER_LEFT);
        chatItem.setCursor(javafx.scene.Cursor.HAND);
        chatItem.setOnMouseClicked(e -> handleChatClick(chatItem, name, room, otherUser));

        Circle clip = new Circle(27.5, 27.5, 27.5);
        Circle profilePic = new Circle(27.5);


        setProfileImage(profilePic, otherUser);

        VBox content = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("League Spartan Bold", 18));
        nameLabel.setTextFill(Color.web("#1e2a47"));

        Label msgLabel = new Label(lastMsg);
        msgLabel.setFont(Font.font("League Spartan", 15));
        msgLabel.setTextFill(Color.web("#7c7c7c"));
        content.getChildren().addAll(nameLabel, msgLabel);

        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.TOP_RIGHT);
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("League Spartan Bold", 13));
        timeLabel.setTextFill(Color.web("#a0a0a0"));
        rightSide.getChildren().add(timeLabel);

        HBox.setHgrow(content, Priority.ALWAYS);
        chatItem.getChildren().addAll(profilePic, content, rightSide);

        updateChatItemStyle(chatItem, isActive);
        chatListContainer.getChildren().add(chatItem);
        chatItemNodes.add(chatItem);
    }

    private void setProfileImage(Circle circle, User user) {
        if (circle == null) return;
        new Thread(() -> {
            Image img = fetchImage(user);
            Platform.runLater(() -> circle.setFill(new ImagePattern(img)));
        }).start();
    }

    private void setImageForImageView(ImageView view, User user) {
        if (view == null) return;
        new Thread(() -> {
            Image img = fetchImage(user);
            Platform.runLater(() -> view.setImage(img));
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

    private void handleChatClick(HBox clickedItem, String name, ChatRoom room, User otherUser) {
        for (HBox item : chatItemNodes) updateChatItemStyle(item, false);
        updateChatItemStyle(clickedItem, true);

        if (currentChatNameLabel != null) currentChatNameLabel.setText(name);
        if (currentChatImage != null && defaultUserImage != null) currentChatImage.setImage(defaultUserImage);

        setImageForImageView(currentChatImage, otherUser);

        this.currentChatRoom = room;
        this.activeChatUser = otherUser;

        if (activeChatListener != null) {
            activeChatListener.remove();
            activeChatListener = null;
        if (localMessageCache.containsKey(room.getId())) {
            renderMessages(localMessageCache.get(room.getId()));
        } else {
            loadMessagesForChat(room);
        }

        // 1. Clear UI and Cache manually here ONE TIME
        messageBubbleContainer.getChildren().clear();
        loadedMessageIds.clear();
        lastTotalMessageCount = 0;

        // 2. Start Listener
        // automatically fetch the initial history (Last 25 messages).
        startChatListener(room.getId());

        // 3. (Optional) Instant load from local cache if available
        // This makes it feel instant while the listener connects
        if (localMessageCache.containsKey(room.getId())) {
            List<Message> cached = localMessageCache.get(room.getId());
            for(Message m : cached) loadedMessageIds.add(m.getId());
            renderMessages(cached);
        }
    }

    private void startChatListener(String chatRoomId) {
        activeChatListener = FirebaseService.getFirestore().collection("chatrooms").document(chatRoomId)
                .addSnapshotListener((snapshot, e) -> {
                    Platform.runLater(() -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            List<String> allMsgIds = (List<String>) snapshot.get("messages");
                            if (allMsgIds == null) allMsgIds = new ArrayList<>();

                            List<String> idsToFetch = new ArrayList<>();

                            if (loadedMessageIds.isEmpty()) {
                                // CASE 1: Initial Load (Last 25)
                                int start = Math.max(0, allMsgIds.size() - 25);
                                idsToFetch.addAll(allMsgIds.subList(start, allMsgIds.size()));
                            } else {
                                // CASE 2: Live Update (New IDs only)
                                // [FIX] Only look at messages added AFTER our last known count
                                if (allMsgIds.size() > lastTotalMessageCount) {
                                    int start = lastTotalMessageCount;
                                    if (start < allMsgIds.size()) {
                                        List<String> newMessages = allMsgIds.subList(start, allMsgIds.size());
                                        for (String id : newMessages) {
                                            // Only add if not already loaded
                                            if (!loadedMessageIds.contains(id)) {
                                                idsToFetch.add(id);
                                            }
                                        }
                                    }
                                }
                            }
                            lastTotalMessageCount = allMsgIds.size();

                            if (!idsToFetch.isEmpty()) {
                                loadedMessageIds.addAll(idsToFetch);
                                fetchAndRenderMessages(idsToFetch);
                            }
                        }
                    });
                });
    }



    private void renderMessages(List<Message> messages) {
        for (Message msg : messages) {
            boolean isSentByMe = false;
            if (msg.getSender() != null && currentUser != null) {
                isSentByMe = msg.getSender().getId().equals(currentUser.getId());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String time = (msg.getCreatedAt() != null) ? sdf.format(msg.getCreatedAt()) : "";
            addMessageBubble(msg.getMessage(), time, isSentByMe);
        }
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }

    @FXML
    private void handleSendMessage(ActionEvent event) {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && currentChatRoom != null) {
            messageInput.clear();

            String msgId = UUID.randomUUID().toString();

            // 1. Safe Add on UI Thread
            loadedMessageIds.add(msgId);

            addMessageBubble(text, "Just now", true);
            messageScrollPane.layout();
            messageScrollPane.setVvalue(1.0);

            User sender = currentUser;
            new Thread(() -> {
                try {
                    Message newMessage = new Message(msgId, text, sender);

                    // --- CHANGED: REMOVED "loadedMessageIds.add(msgId)" FROM HERE ---

                    currentChatRoom.addMessage(newMessage);

                    Platform.runLater(() -> {
                        if (localMessageCache.containsKey(currentChatRoom.getId())) {
                            localMessageCache.get(currentChatRoom.getId()).add(newMessage);
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }

    private void fetchAndRenderMessages(List<String> ids) {
        Task<List<Message>> task = new Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                List<DocumentReference> refs = new ArrayList<>();

                // --- FIX: Filter out null or empty IDs to prevent crash ---
                for (String id : ids) {
                    if (id != null && !id.trim().isEmpty()) {
                        refs.add(FirebaseService.getFirestore().collection("messages").document(id));
                    }
                }

                if (refs.isEmpty()) return new ArrayList<>();

                ApiFuture<List<DocumentSnapshot>> future = FirebaseService.getFirestore().getAll(refs.toArray(new DocumentReference[0]));
                List<DocumentSnapshot> snapshots = future.get();

                List<Message> messages = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    if (doc.exists()) messages.add(new Message(doc));
                }

                // Sort by time so they appear in order
                messages.sort(Comparator.comparing(m -> m.getCreatedAt() != null ? m.getCreatedAt() : new Date(0)));
                return messages;
            }
        };

        task.setOnSucceeded(e -> {
            renderMessages(task.getValue());
        });

        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void addMessageBubble(String text, String time, boolean isSentByMe) {
        HBox bubbleContainer = new HBox();
        bubbleContainer.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(6);
        bubble.setMaxWidth(450);
        bubble.setPadding(new Insets(15, 20, 15, 20));

        if (isSentByMe) {
            bubble.setStyle("-fx-background-color: #cbd45b; -fx-background-radius: 25 25 0 25;");
        } else {
            bubble.setStyle("-fx-background-color: #cbd45b; -fx-background-radius: 25 25 25 0;");
        }
        bubble.setEffect(new DropShadow(5, Color.rgb(0,0,0,0.1)));

        Label messageLabel = new Label(text);
        messageLabel.setFont(Font.font("League Spartan", 16));

        messageLabel.setTextFill(Color.BLACK);
        messageLabel.setStyle("-fx-text-fill: black;");

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        Label timeLabel = new Label(time);
        timeLabel.setMaxWidth(Double.MAX_VALUE);
        timeLabel.setAlignment(isSentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        timeLabel.setFont(Font.font("League Spartan", 12));
        timeLabel.setTextFill(Color.web("#333333"));

        bubble.getChildren().addAll(messageLabel, timeLabel);
        bubbleContainer.getChildren().add(bubble);
        messageBubbleContainer.getChildren().add(bubbleContainer);
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        if (activeChatUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
            Parent root = loader.load();
            OtherProfileController controller = loader.getController();
            Scene currentScene = ((Node) event.getSource()).getScene();
            controller.setProfileData(currentScene, activeChatUser.getId());
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateChatItemStyle(HBox item, boolean isActive) {
        if (isActive) {
            item.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 30 0 0 30;");
            item.setPadding(new Insets(15, 20, 15, 20));
            VBox.setMargin(item, new Insets(0, 0, 0, 0));
            DropShadow ds = new DropShadow(10, Color.rgb(0,0,0,0.1));
            ds.setOffsetX(-2); ds.setOffsetY(2);
            item.setEffect(ds);
        } else {
            item.setStyle("-fx-background-color: #FFE7C2; -fx-background-radius: 30;");
            item.setPadding(new Insets(15, 20, 15, 20));
            VBox.setMargin(item, new Insets(0, 15, 0, 0));
            item.setEffect(null);
        }
    }
}