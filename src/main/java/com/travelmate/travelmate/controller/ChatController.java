package com.travelmate.travelmate.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatController {

    @FXML private SidebarController sidebarController;
    @FXML private VBox chatListContainer;

    @FXML private Label currentChatNameLabel;
    @FXML private ImageView currentChatImage;

    @FXML private VBox messageBubbleContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane messageScrollPane;

    // --- STATE ---
    private List<HBox> chatItemNodes = new ArrayList<>();
    private ChatRoom currentChatRoom = null;
    private User currentUser;
    private User activeChatUser;

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

        // Auto-scroll
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

        boolean isFirst = true;

        for (String chatId : chatIds) {
            ChatRoom room = ChatList.getChat(chatId);
            if (room != null) {
                User otherUser = findOtherUser(room);
                String displayName = (otherUser != null) ? otherUser.getName() : "Unknown Chat";
                String lastMsg = "Click to view";

                addChatToSidebar(displayName, lastMsg, "", isFirst, room, otherUser);

                if (isFirst) {
                    handleChatClick(chatItemNodes.get(0), displayName, room, otherUser);
                    isFirst = false;
                }
            }
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
        ImageView profilePic = new ImageView();
        if (defaultUserImage != null) profilePic.setImage(defaultUserImage);
        profilePic.setFitWidth(55); profilePic.setFitHeight(55); profilePic.setClip(clip);

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

    private void handleChatClick(HBox clickedItem, String name, ChatRoom room, User otherUser) {
        for (HBox item : chatItemNodes) updateChatItemStyle(item, false);
        updateChatItemStyle(clickedItem, true);

        if (currentChatNameLabel != null) currentChatNameLabel.setText(name);
        if (currentChatImage != null && defaultUserImage != null) currentChatImage.setImage(defaultUserImage);

        this.currentChatRoom = room;
        this.activeChatUser = otherUser;

        if (localMessageCache.containsKey(room.getId())) {
            renderMessages(localMessageCache.get(room.getId()));
        } else {
            loadMessagesForChat(room);
        }
    }

    private void loadMessagesForChat(ChatRoom room) {
        messageBubbleContainer.getChildren().clear();
        if (room == null || room.getMessages() == null) return;

        Task<List<Message>> loadMessagesTask = new Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                List<String> messageIds = room.getMessages();
                if (messageIds.isEmpty()) return new ArrayList<>();

                int total = messageIds.size();
                int limit = 25;
                int start = Math.max(0, total - limit);
                List<String> recentIds = messageIds.subList(start, total);

                List<DocumentReference> refs = new ArrayList<>();
                for (String msgId : recentIds) {
                    if (msgId != null && !msgId.trim().isEmpty()) {
                        refs.add(FirebaseService.getFirestore().collection("messages").document(msgId));
                    }
                }
                if (refs.isEmpty()) return new ArrayList<>();

                ApiFuture<List<DocumentSnapshot>> future = FirebaseService.getFirestore().getAll(refs.toArray(new DocumentReference[0]));
                List<DocumentSnapshot> snapshots = future.get();

                List<Message> loadedMessages = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    if (doc.exists()) {
                        loadedMessages.add(new Message(doc));
                    }
                }

                loadedMessages.sort(Comparator.comparing(m -> m.getCreatedAt() != null ? m.getCreatedAt() : new Date(0)));
                return loadedMessages;
            }
        };

        loadMessagesTask.setOnSucceeded(event -> {
            List<Message> messages = loadMessagesTask.getValue();
            localMessageCache.put(room.getId(), messages);
            renderMessages(messages);
        });

        loadMessagesTask.setOnFailed(e -> {
            System.err.println("Safe load failed");
        });

        new Thread(loadMessagesTask).start();
    }

    private void renderMessages(List<Message> messages) {
        messageBubbleContainer.getChildren().clear();
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

            // 1. UI UPDATE FIRST
            messageInput.clear();
            addMessageBubble(text, "Just now", true);
            messageScrollPane.layout();
            messageScrollPane.setVvalue(1.0);

            // 2. BACKGROUND PROCESSING
            String chatId = currentChatRoom.getId();
            User sender = currentUser;

            new Thread(() -> {
                try {
                    String msgId = UUID.randomUUID().toString();
                    Message newMessage = new Message(msgId, text, sender);
                    currentChatRoom.addMessage(newMessage);

                    Platform.runLater(() -> {
                        if (localMessageCache.containsKey(chatId)) {
                            localMessageCache.get(chatId).add(newMessage);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
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

        // --- TEXT WRAPPING FIX ---
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350); // Ensure this is smaller than bubble max width
        messageLabel.setMinHeight(Region.USE_PREF_SIZE); // Ensure it grows

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