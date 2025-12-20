package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.ChatRoom;
import com.travelmate.travelmate.model.DirectMessage;
import com.travelmate.travelmate.model.Message;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatController implements Initializable {

    @FXML private VBox chatListContainer;
    @FXML private VBox messageBubbleContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private Label currentChatNameLabel;
    @FXML private TextField messageInput;
    @FXML private Label headerUsernameLabel;
    @FXML private ImageView headerProfileImage;
    @FXML private SidebarController sidebarController;

    // Use a cached thread pool for better performance with network tasks
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();

    private User currentUser;
    private ChatRoom activeChatRoom;

    private final List<ChatRoom> myChats = new CopyOnWriteArrayList<>();
    private final Map<String, User> chatFriends = new ConcurrentHashMap<>();

    private static final String DEFAULT_STYLE = "-fx-background-color: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: black; -fx-border-radius: 15; -fx-border-width: 2px;";
    private static final String SELECTED_STYLE = "-fx-background-color: #e0f7fa; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: #1E2A47; -fx-border-radius: 15; -fx-border-width: 3px;";

    private HBox selectedRow = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) {
            sidebarController.setActivePage("Chat");
        }
        // Load User in Background
        CompletableFuture.runAsync(() -> {
            try {

                currentUser = UserSession.getCurrentUser();
                if (currentUser == null) {
                    System.out.println("DEBUG: UserSession is null. Using Test ID.");
                    currentUser = new User("HuZUKiHoRQg7XRkRx5gq");
                }

                Platform.runLater(() -> {
                    if (headerUsernameLabel != null && currentUser.getUsername() != null) {
                        headerUsernameLabel.setText(currentUser.getUsername());
                    }
                    setProfileImage(headerProfileImage, currentUser);
                    loadChatRoomsFast();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, networkExecutor);
    }

    private void loadChatRoomsFast() {
        myChats.clear();
        chatFriends.clear();

        ArrayList<String> chatIds = currentUser.getChatRooms();
        if (chatIds == null || chatIds.isEmpty()) return;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String chatId : chatIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Fetch ChatRoom
                    DirectMessage room = new DirectMessage(chatId);
                    myChats.add(room);

                    // Fetch Friend
                    String friendId = getOtherUserId(room);
                    if (!chatFriends.containsKey(friendId)) {
                        User friendUser = new User(friendId);
                        chatFriends.put(chatId, friendUser);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading chat: " + chatId);
                }
            }, networkExecutor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(this::renderChatList));
    }

    private void renderChatList() {
        chatListContainer.getChildren().clear();

        for (ChatRoom room : myChats) {
            User friend = chatFriends.get(room.getId());
            String displayName = (friend != null) ? friend.getUsername() : "Unknown";

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setPrefHeight(60);
            row.setStyle(DEFAULT_STYLE);

            ImageView avatar = new ImageView();
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            setProfileImage(avatar, friend);

            Label nameLabel = new Label(displayName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E2A47;");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label arrow = new Label(">");
            arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            row.getChildren().addAll(avatar, nameLabel, spacer, arrow);

            row.setOnMouseClicked(e -> selectChat(room, row));
            chatListContainer.getChildren().add(row);
        }
    }

    private void selectChat(ChatRoom room, HBox clickedRow) {
        this.activeChatRoom = room;

        if (selectedRow != null) selectedRow.setStyle(DEFAULT_STYLE);
        selectedRow = clickedRow;
        if (selectedRow != null) selectedRow.setStyle(SELECTED_STYLE);

        User friend = chatFriends.get(room.getId());
        if (currentChatNameLabel != null && friend != null) {
            currentChatNameLabel.setText(friend.getUsername());
        }

        loadMessages(room);
    }

    private void loadMessages(ChatRoom room) {
        Platform.runLater(() -> {
            messageBubbleContainer.getChildren().clear();
            Label loading = new Label("Loading...");
            loading.setStyle("-fx-text-fill: gray; -fx-padding: 10;");
            messageBubbleContainer.getChildren().add(loading);
        });

        // Fetch ALL messages in Parallel
        CompletableFuture.supplyAsync(() -> {
            List<Message> loadedMessages = new ArrayList<>();
            if (room.getMessages() != null && !room.getMessages().isEmpty()) {

                List<CompletableFuture<Message>> msgFutures = new ArrayList<>();
                for (String msgId : room.getMessages()) {
                    msgFutures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            // This blocks, but it runs on a separate thread in the pool
                            return new Message(msgId);
                        } catch (Exception e) {
                            return null;
                        }
                    }, networkExecutor));
                }

                // Join all threads and collect results
                loadedMessages = msgFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(m -> m != null)
                        .sorted((m1, m2) -> {
                            if (m1.getCreatedAt() == null || m2.getCreatedAt() == null) return 0;
                            return m1.getCreatedAt().compareTo(m2.getCreatedAt());
                        })
                        .collect(Collectors.toList());
            }
            return loadedMessages;
        }, networkExecutor).thenAccept(messages ->
                Platform.runLater(() -> renderMessages(messages))
        );
    }

    private void renderMessages(List<Message> messages) {
        messageBubbleContainer.getChildren().clear();
        for (Message msg : messages) {
            renderSingleBubble(msg);
        }
        scrollToBottom();
    }

    private void renderSingleBubble(Message msg) {
        HBox bubbleRow = new HBox();
        Label text = new Label(msg.getMessage());
        text.setWrapText(true);
        text.setMaxWidth(350);
        text.setPadding(new Insets(10));

        boolean isMe = msg.getSender() != null && currentUser != null && msg.getSender().getId().equals(currentUser.getId());

        if (isMe) {
            bubbleRow.setAlignment(Pos.CENTER_RIGHT);
            text.setStyle("-fx-background-color: #1E2A47; -fx-text-fill: white; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
        } else {
            bubbleRow.setAlignment(Pos.CENTER_LEFT);
            text.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 15 15 15 0; -fx-border-color: #ccc; -fx-border-radius: 15 15 15 0; -fx-font-size: 14px;");
        }
        bubbleRow.getChildren().add(text);
        messageBubbleContainer.getChildren().add(bubbleRow);
    }

    private void scrollToBottom() {
        messageScrollPane.applyCss();
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || activeChatRoom == null) return;

        messageInput.clear();

        // --- OPTIMIZATION: Optimistic UI Update ---
        // 1. Create message locally
        String tempId = "msg_" + System.currentTimeMillis();
        // Use the fast constructor which saves to DB in background
        Message newMessage = new Message(tempId, text, currentUser);

        // 2. Add to UI immediately
        renderSingleBubble(newMessage);
        scrollToBottom();

        // 3. Update ChatRoom and User objects in background
        new Thread(() -> {
            try {
                activeChatRoom.addMessage(newMessage);
                currentUser.addMessage(newMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- Helper Methods ---

    private void setProfileImage(ImageView imageView, User user) {
        if (imageView == null) return;
        Image image = null;
        try {
            var resource = getClass().getResourceAsStream("/images/user_icon.png");
            if (resource == null) resource = getClass().getResourceAsStream("/images/logoBlue.png");
            if (resource != null) image = new Image(resource);
        } catch (Exception e) { }

        if (user != null && user.getProfile() != null) {
            String url = user.getProfile().getProfilePictureUrl();
            if (url != null && !url.isEmpty() && url.startsWith("http")) {
                try {
                    image = new Image(url, true); // true = load in background
                } catch (Exception e) { }
            }
        }
        if (image != null) {
            imageView.setImage(image);
            double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
            imageView.setClip(new Circle(radius, radius, radius));
        }
    }

    private String getOtherUserId(ChatRoom room) {
        if (room.getActiveUsers() == null) return "Unknown";
        return room.getActiveUsers().stream()
                .filter(id -> !id.equals(currentUser.getId()))
                .findFirst()
                .orElse("Unknown");
    }

    @FXML private void goToProfile(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Profile.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML private void goToHome(javafx.event.ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}